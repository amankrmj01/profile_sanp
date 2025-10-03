package com.piandphi.scraper;

import com.piandphi.model.UserProfile;
import com.piandphi.model.UserContestHistory;
import com.piandphi.model.UserSubmissions;
import com.piandphi.resilience.ResilientScraper;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class LeetCodeScraper {

    private final ResilientScraper<UserProfile> resilientUserProfile = new ResilientScraper<>("leetcode-user-profile");
    private final ResilientScraper<UserContestHistory> resilientContestHistory = new ResilientScraper<>("leetcode-contest-history");
    private final ResilientScraper<List<UserSubmissions.RecentSubmission>> resilientSubmissions = new ResilientScraper<>("leetcode-submissions");

    // New method for user profile endpoint
    public UserProfile scrapeUserProfile(String username) {
        return resilientUserProfile.execute(
                () -> {
                    try {
                        return fetchUserProfile(username);
                    } catch (IOException e) {
                        return createFallbackUserProfile(username);
                    }
                },
                () -> createFallbackUserProfile(username)
        );
    }

    // New method for contest history endpoint
    public UserContestHistory scrapeContestHistory(String username) {
        return resilientContestHistory.execute(
                () -> {
                    try {
                        return fetchContestHistory(username);
                    } catch (IOException e) {
                        return createFallbackContestHistory();
                    }
                },
                this::createFallbackContestHistory
        );
    }

    // New method for recent submissions endpoint
    public List<UserSubmissions.RecentSubmission> scrapeRecentSubmissions(String username, int limit) {
        return resilientSubmissions.execute(
                () -> {
                    try {
                        return fetchRecentSubmissions(username, limit);
                    } catch (IOException e) {
                        return List.of();
                    }
                },
                List::of
        );
    }

    // Separate fetch method for user profile
    private UserProfile fetchUserProfile(String username) throws IOException {
        String query = """
                query userPublicProfile($username: String!) {
                    matchedUser(username: $username) {
                        username
                        githubUrl
                        twitterUrl
                        linkedinUrl
                        profile {
                            userAvatar
                            realName
                            websites
                            countryName
                            company
                            jobTitle
                            skillTags
                            school
                            aboutMe
                            postViewCount
                            postViewCountDiff
                            reputation
                            ranking
                            reputationDiff
                            solutionCount
                            solutionCountDiff
                            categoryDiscussCount
                            categoryDiscussCountDiff
                            certificationLevel
                        }
                        submitStats {
                            acSubmissionNum {
                                difficulty
                                count
                                submissions
                            }
                            totalSubmissionNum {
                                difficulty
                                count
                                submissions
                            }
                        }
                        contestBadge {
                            name
                            expired
                            hoverText
                            icon
                        }
                    }
                }
                """;

        return executeGraphQLQuery(query, Map.of("username", username), "userPublicProfile", this::parseUserProfileResponse);
    }

    // Separate fetch method for contest history
    private UserContestHistory fetchContestHistory(String username) throws IOException {
        String query = """
                query userContestRankingInfo($username: String!) {
                    userContestRanking(username: $username) {
                        attendedContestsCount
                        rating
                        globalRanking
                        totalParticipants
                        topPercentage
                        badge {
                            name
                        }
                    }
                    userContestRankingHistory(username: $username) {
                        attended
                        trendDirection
                        problemsSolved
                        totalProblems
                        finishTimeInSeconds
                        rating
                        ranking
                        contest {
                            title
                            startTime
                        }
                    }
                }
                """;

        return executeGraphQLQuery(query, Map.of("username", username), "userContestRankingInfo", this::parseContestHistoryResponse);
    }

    // Separate fetch method for recent submissions
    private List<UserSubmissions.RecentSubmission> fetchRecentSubmissions(String username, int limit) throws IOException {
        String query = """
                query recentSubmissions($username: String!, $limit: Int) {
                    recentSubmissionList(username: $username, limit: $limit) {
                        id
                        title
                        titleSlug
                        timestamp
                        status
                        statusDisplay
                        lang
                        url
                        langName
                        runtime
                        isPending
                        memory
                        hasNotes
                        notes
                        flagType
                        frontendId
                        topicTags {
                            id
                        }
                    }
                }
                """;

        return executeGraphQLQuery(query, Map.of("username", username, "limit", limit), "recentSubmissions", this::parseRecentSubmissionsResponse);
    }

    // Generic GraphQL execution method
    private <T> T executeGraphQLQuery(String query, Map<String, Object> variables, String operationName, GraphQLResponseParser<T> parser) throws IOException {
        try {
            Map<String, Object> payload = Map.of(
                    "query", query,
                    "variables", variables,
                    "operationName", operationName
            );

            System.out.println("Making GraphQL API call for operation: " + operationName);

            try (var httpClient = io.micronaut.http.client.HttpClient.create(java.net.URI.create("https://leetcode.com").toURL())) {
                var request = io.micronaut.http.HttpRequest.POST("/graphql/", payload)
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .header("Accept", "*/*")
                        .header("Origin", "https://leetcode.com")
                        .header("Referer", "https://leetcode.com/u/" + variables.get("username") + "/");

                var response = httpClient.toBlocking().exchange(request, String.class);

                if (response.getStatus().getCode() == 200 && response.getBody().isPresent()) {
                    String responseBody = response.getBody().get();

                    return parser.parse(responseBody);
                } else {
                    System.out.println("GraphQL API call failed with status: " + response.getStatus().getCode());
                    if (response.getBody().isPresent()) {
                        System.out.println("Response body: " + response.getBody().get());
                    }
                    throw new IOException("GraphQL API call failed with status: " + response.getStatus().getCode());
                }
            }
        } catch (Exception e) {
            System.out.println("Error in GraphQL API call: " + e.getMessage());
            throw new IOException("Failed to fetch data via GraphQL API", e);
        }
    }

    // Parser for user profile response
    private UserProfile parseUserProfileResponse(String responseBody) throws IOException {
        try {
            System.out.println("Parsing user profile response...");

            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonResponse = objectMapper.readTree(responseBody);

            // Check for GraphQL errors first
            if (jsonResponse.has("errors") && jsonResponse.get("errors").isArray()) {
                System.out.println("GraphQL errors found: " + jsonResponse.get("errors"));
                throw new IOException("GraphQL query returned errors: " + jsonResponse.get("errors"));
            }

            com.fasterxml.jackson.databind.JsonNode matchedUser = jsonResponse.path("data").path("matchedUser");

            if (matchedUser.isMissingNode() || matchedUser.isNull()) {
                throw new IOException("User not found");
            }

            // Parse profile data
            com.fasterxml.jackson.databind.JsonNode profileNode = matchedUser.path("profile");
            UserProfile.Profile profile = new UserProfile.Profile(
                    getTextValue(profileNode, "userAvatar"),
                    getTextValue(profileNode, "realName"),
                    parseStringList(profileNode, "websites"),
                    getTextValue(profileNode, "countryName"),
                    getTextValue(profileNode, "company"),
                    getTextValue(profileNode, "jobTitle"),
                    parseStringList(profileNode, "skillTags"),
                    getTextValue(profileNode, "school"),
                    getTextValue(profileNode, "aboutMe"),
                    getIntegerValue(profileNode, "postViewCount").orElse(null),
                    getIntegerValue(profileNode, "postViewCountDiff").orElse(null),
                    getIntegerValue(profileNode, "reputation").orElse(null),
                    getIntegerValue(profileNode, "ranking").orElse(null),
                    getIntegerValue(profileNode, "reputationDiff").orElse(null),
                    getIntegerValue(profileNode, "solutionCount").orElse(null),
                    getIntegerValue(profileNode, "solutionCountDiff").orElse(null),
                    getIntegerValue(profileNode, "categoryDiscussCount").orElse(null),
                    getIntegerValue(profileNode, "categoryDiscussCountDiff").orElse(null),
                    getTextValue(profileNode, "certificationLevel")
            );

            // Parse submit stats
            UserProfile.SubmitStats submitStats = null;
            com.fasterxml.jackson.databind.JsonNode submitStatsNode = matchedUser.path("submitStats");
            if (!submitStatsNode.isMissingNode() && !submitStatsNode.isNull()) {
                List<UserProfile.SubmissionCount> acSubmissions = parseUserProfileSubmissionCounts(submitStatsNode.path("acSubmissionNum"));
                List<UserProfile.SubmissionCount> totalSubmissions = parseUserProfileSubmissionCounts(submitStatsNode.path("totalSubmissionNum"));
                submitStats = new UserProfile.SubmitStats(acSubmissions, totalSubmissions);
            }

            // Parse contest badge
            UserProfile.ContestBadge contestBadge = null;
            com.fasterxml.jackson.databind.JsonNode contestBadgeNode = matchedUser.path("contestBadge");
            if (!contestBadgeNode.isMissingNode() && !contestBadgeNode.isNull()) {
                contestBadge = new UserProfile.ContestBadge(
                        getTextValue(contestBadgeNode, "name"),
                        contestBadgeNode.path("expired").isBoolean() ? contestBadgeNode.path("expired").asBoolean() : null,
                        getTextValue(contestBadgeNode, "hoverText"),
                        getTextValue(contestBadgeNode, "icon")
                );
            }

            return new UserProfile(
                    getTextValue(matchedUser, "username"),
                    getTextValue(matchedUser, "githubUrl"),
                    getTextValue(matchedUser, "twitterUrl"),
                    getTextValue(matchedUser, "linkedinUrl"),
                    profile,
                    submitStats,
                    contestBadge
            );

        } catch (Exception e) {
            System.out.println("Exception in parseUserProfileResponse: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to parse user profile response: " + e.getMessage(), e);
        }
    }

    // Parser for contest history response
    private UserContestHistory parseContestHistoryResponse(String responseBody) throws IOException {
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonResponse = objectMapper.readTree(responseBody);

            // Check for GraphQL errors
            if (jsonResponse.has("errors") && jsonResponse.get("errors").isArray()) {
                throw new IOException("GraphQL query returned errors: " + jsonResponse.get("errors"));
            }

            com.fasterxml.jackson.databind.JsonNode contestRankingNode = jsonResponse.path("data").path("userContestRanking");
            com.fasterxml.jackson.databind.JsonNode contestHistoryNode = jsonResponse.path("data").path("userContestRankingHistory");

            // Parse user contest ranking
            UserContestHistory.UserContestRanking userContestRanking = null;
            if (!contestRankingNode.isMissingNode() && !contestRankingNode.isNull()) {
                UserContestHistory.Badge badge = null;
                com.fasterxml.jackson.databind.JsonNode badgeNode = contestRankingNode.path("badge");
                if (!badgeNode.isMissingNode() && !badgeNode.isNull()) {
                    badge = new UserContestHistory.Badge(getTextValue(badgeNode, "name"));
                }

                userContestRanking = new UserContestHistory.UserContestRanking(
                        getIntegerValue(contestRankingNode, "attendedContestsCount").orElse(null),
                        contestRankingNode.path("rating").isNumber() ? contestRankingNode.path("rating").asDouble() : null,
                        getIntegerValue(contestRankingNode, "globalRanking").orElse(null),
                        getIntegerValue(contestRankingNode, "totalParticipants").orElse(null),
                        contestRankingNode.path("topPercentage").isNumber() ? contestRankingNode.path("topPercentage").asDouble() : null,
                        badge
                );
            }

            // Parse contest history
            List<UserContestHistory.ContestHistoryEntry> contestHistoryList = new ArrayList<>();
            if (contestHistoryNode.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode item : contestHistoryNode) {
                    UserContestHistory.Contest contest = null;
                    com.fasterxml.jackson.databind.JsonNode contestNode = item.path("contest");
                    if (!contestNode.isMissingNode() && !contestNode.isNull()) {
                        contest = new UserContestHistory.Contest(
                                getTextValue(contestNode, "title"),
                                getTextValue(contestNode, "startTime")
                        );
                    }

                    contestHistoryList.add(new UserContestHistory.ContestHistoryEntry(
                            item.path("attended").isBoolean() ? item.path("attended").asBoolean() : null,
                            getTextValue(item, "trendDirection"),
                            getIntegerValue(item, "problemsSolved").orElse(null),
                            getIntegerValue(item, "totalProblems").orElse(null),
                            getIntegerValue(item, "finishTimeInSeconds").orElse(null),
                            item.path("rating").isNumber() ? item.path("rating").asDouble() : null,
                            getIntegerValue(item, "ranking").orElse(null),
                            contest
                    ));
                }
            }

            return new UserContestHistory(userContestRanking, contestHistoryList);

        } catch (Exception e) {
            throw new IOException("Failed to parse contest history response: " + e.getMessage(), e);
        }
    }

    // Parser for recent submissions response
    private List<UserSubmissions.RecentSubmission> parseRecentSubmissionsResponse(String responseBody) throws IOException {
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonResponse = objectMapper.readTree(responseBody);

            // Check for GraphQL errors
            if (jsonResponse.has("errors") && jsonResponse.get("errors").isArray()) {
                throw new IOException("GraphQL query returned errors: " + jsonResponse.get("errors"));
            }

            com.fasterxml.jackson.databind.JsonNode recentSubmissionsNode = jsonResponse.path("data").path("recentSubmissionList");

            List<UserSubmissions.RecentSubmission> result = new ArrayList<>();
            if (recentSubmissionsNode.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode item : recentSubmissionsNode) {
                    List<UserSubmissions.TopicTag> topicTags = new ArrayList<>();
                    com.fasterxml.jackson.databind.JsonNode topicTagsNode = item.path("topicTags");
                    if (topicTagsNode.isArray()) {
                        for (com.fasterxml.jackson.databind.JsonNode tagNode : topicTagsNode) {
                            topicTags.add(new UserSubmissions.TopicTag(getTextValue(tagNode, "id")));
                        }
                    }

                    result.add(new UserSubmissions.RecentSubmission(
                            getTextValue(item, "id"),
                            getTextValue(item, "title"),
                            getTextValue(item, "titleSlug"),
                            getTextValue(item, "timestamp"),
                            getTextValue(item, "status"),
                            getTextValue(item, "statusDisplay"),
                            getTextValue(item, "lang"),
                            getTextValue(item, "url"),
                            getTextValue(item, "langName"),
                            getTextValue(item, "runtime"),
                            item.path("isPending").isBoolean() ? item.path("isPending").asBoolean() : null,
                            getTextValue(item, "memory"),
                            item.path("hasNotes").isBoolean() ? item.path("hasNotes").asBoolean() : null,
                            getTextValue(item, "notes"),
                            getTextValue(item, "flagType"),
                            getTextValue(item, "frontendId"),
                            topicTags
                    ));
                }
            }

            return result;

        } catch (Exception e) {
            throw new IOException("Failed to parse recent submissions response: " + e.getMessage(), e);
        }
    }

    // Helper method for parsing submission counts for UserProfile
    private List<UserProfile.SubmissionCount> parseUserProfileSubmissionCounts(com.fasterxml.jackson.databind.JsonNode arrayNode) {
        List<UserProfile.SubmissionCount> result = new ArrayList<>();
        if (arrayNode.isArray()) {
            for (com.fasterxml.jackson.databind.JsonNode item : arrayNode) {
                String difficulty = getTextValue(item, "difficulty");
                Integer count = item.path("count").isInt() ? item.path("count").asInt() : null;
                Integer submissions = item.path("submissions").isInt() ? item.path("submissions").asInt() : null;

                if (difficulty != null) {
                    result.add(new UserProfile.SubmissionCount(difficulty, count, submissions));
                }
            }
        }
        return result;
    }

    // Fallback methods
    private UserProfile createFallbackUserProfile(String username) {
        UserProfile.Profile profile = new UserProfile.Profile(
                null, "Unknown", List.of(), null, null, null, List.of(), null, null,
                0, 0, 0, 0, 0, 0, 0, 0, 0, "NORMAL"
        );

        return new UserProfile(username, null, null, null, profile, null, null);
    }

    private UserContestHistory createFallbackContestHistory() {
        return new UserContestHistory(null, List.of());
    }

    // Helper methods for parsing GraphQL response
    private String getTextValue(com.fasterxml.jackson.databind.JsonNode node, String fieldName) {
        com.fasterxml.jackson.databind.JsonNode field = node.path(fieldName);
        return field.isMissingNode() || field.isNull() ? null : field.asText();
    }

    private Optional<Integer> getIntegerValue(com.fasterxml.jackson.databind.JsonNode node, String fieldName) {
        com.fasterxml.jackson.databind.JsonNode field = node.path(fieldName);
        return field.isMissingNode() || field.isNull() ? Optional.empty() : Optional.of(field.asInt());
    }

    private List<String> parseStringList(com.fasterxml.jackson.databind.JsonNode node, String fieldName) {
        com.fasterxml.jackson.databind.JsonNode arrayNode = node.path(fieldName);
        if (arrayNode.isMissingNode() || arrayNode.isNull() || !arrayNode.isArray()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        for (com.fasterxml.jackson.databind.JsonNode item : arrayNode) {
            if (!item.isNull()) {
                result.add(item.asText());
            }
        }
        return result;
    }

    // Functional interface for parsing responses
    @FunctionalInterface
    private interface GraphQLResponseParser<T> {
        T parse(String responseBody) throws IOException;
    }
}
