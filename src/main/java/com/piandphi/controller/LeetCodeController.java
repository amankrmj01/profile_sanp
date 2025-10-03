package com.piandphi.controller;

import com.piandphi.model.UserProfile;
import com.piandphi.model.UserContestHistory;
import com.piandphi.model.UserSubmissions;
import com.piandphi.scraper.LeetCodeScraper;
import com.piandphi.service.ProfileCacheService;
import io.micronaut.http.annotation.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Controller("/leetcode")
@ExecuteOn(TaskExecutors.BLOCKING)
public class LeetCodeController {

    private static final Logger LOG = LoggerFactory.getLogger(LeetCodeController.class);

    private final LeetCodeScraper leetCodeScraper;
    private final ProfileCacheService cacheService;

    public LeetCodeController(LeetCodeScraper leetCodeScraper, ProfileCacheService cacheService) {
        this.leetCodeScraper = leetCodeScraper;
        this.cacheService = cacheService;
    }

    @Get("/user/{username}")
    public UserProfile getUserProfile(@PathVariable String username) {
        try {
            LOG.info("Fetching user profile for: {}", username);

            // Try to get from cache first
            UserProfile cachedProfile = cacheService.getUserProfile(username);
            if (cachedProfile != null) {
                LOG.info("Returning cached user profile for: {}", username);
                return cachedProfile;
            }

            // Cache miss - scrape fresh data
            LOG.info("Cache miss - scraping fresh user profile for: {}", username);
            UserProfile profile = leetCodeScraper.scrapeUserProfile(username);

            // Cache the result
            cacheService.cacheUserProfile(username, profile);

            return profile;
        } catch (Exception e) {
            LOG.error("Error fetching user profile for {}: {}", username, e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching user profile: " + e.getMessage());
        }
    }

    @Get("/user/{username}/contests")
    public UserContestHistory getUserContestHistory(@PathVariable String username) {
        try {
            LOG.info("Fetching contest history for: {}", username);

            // Try to get from cache first
            UserContestHistory cachedHistory = cacheService.getContestHistory(username);
            if (cachedHistory != null) {
                LOG.info("Returning cached contest history for: {}", username);
                return cachedHistory;
            }

            // Cache miss - scrape fresh data
            LOG.info("Cache miss - scraping fresh contest history for: {}", username);
            UserContestHistory history = leetCodeScraper.scrapeContestHistory(username);

            // Cache the result
            cacheService.cacheContestHistory(username, history);

            return history;
        } catch (Exception e) {
            LOG.error("Error fetching contest history for {}: {}", username, e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching contest history: " + e.getMessage());
        }
    }

    @Get("/user/{username}/submissions")
    public List<UserSubmissions.RecentSubmission> getRecentSubmissions(
            @PathVariable String username,
            @QueryValue Optional<Integer> limit) {
        try {
            int submissionLimit = limit.orElse(20);
            String cacheKey = username + "_" + submissionLimit; // Include limit in cache key

            LOG.info("Fetching recent submissions for: {} (limit: {})", username, submissionLimit);

            // Try to get from cache first
            List<UserSubmissions.RecentSubmission> cachedSubmissions = cacheService.getSubmissions(cacheKey);
            if (cachedSubmissions != null) {
                LOG.info("Returning cached submissions for: {} (count: {})", username, cachedSubmissions.size());
                return cachedSubmissions;
            }

            // Cache miss - scrape fresh data
            LOG.info("Cache miss - scraping fresh submissions for: {} (limit: {})", username, submissionLimit);
            List<UserSubmissions.RecentSubmission> submissions = leetCodeScraper.scrapeRecentSubmissions(username, submissionLimit);

            // Cache the result
            cacheService.cacheSubmissions(cacheKey, submissions);

            return submissions;
        } catch (Exception e) {
            LOG.error("Error fetching submissions for {}: {}", username, e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching submissions: " + e.getMessage());
        }
    }
}
