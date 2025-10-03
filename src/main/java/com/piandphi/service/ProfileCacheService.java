package com.piandphi.service;

import com.piandphi.model.HackerRankProfile;
import com.piandphi.model.UserProfile;
import com.piandphi.model.UserContestHistory;
import com.piandphi.model.UserSubmissions;
import jakarta.inject.Singleton;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.List;

@Singleton
public class ProfileCacheService {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileCacheService.class);

    // Cache TTL settings
    private static final Duration USER_PROFILE_TTL = Duration.ofHours(1);
    private static final Duration HACKERRANK_PROFILE_TTL = Duration.ofHours(2);
    private static final Duration CONTEST_HISTORY_TTL = Duration.ofMinutes(30);
    private static final Duration SUBMISSIONS_TTL = Duration.ofMinutes(15);

    // Caffeine caches
    private Cache<String, UserProfile> userProfiles;
    private Cache<String, HackerRankProfile> hackerRankProfiles;
    private Cache<String, UserContestHistory> contestHistories;
    private Cache<String, List<UserSubmissions.RecentSubmission>> submissions;

    @PostConstruct
    public void init() {
        LOG.info("Initializing Caffeine caches...");

        userProfiles = Caffeine.newBuilder()
                .expireAfterWrite(USER_PROFILE_TTL)
                .maximumSize(1000)
                .recordStats()
                .build();

        hackerRankProfiles = Caffeine.newBuilder()
                .expireAfterWrite(HACKERRANK_PROFILE_TTL)
                .maximumSize(1000)
                .recordStats()
                .build();

        contestHistories = Caffeine.newBuilder()
                .expireAfterWrite(CONTEST_HISTORY_TTL)
                .maximumSize(500)
                .recordStats()
                .build();

        submissions = Caffeine.newBuilder()
                .expireAfterWrite(SUBMISSIONS_TTL)
                .maximumSize(2000)
                .recordStats()
                .build();

        LOG.info("Caffeine caches initialized successfully");
    }

    @PreDestroy
    public void shutdown() {
        LOG.info("Shutting down caches...");
        if (userProfiles != null) userProfiles.invalidateAll();
        if (hackerRankProfiles != null) hackerRankProfiles.invalidateAll();
        if (contestHistories != null) contestHistories.invalidateAll();
        if (submissions != null) submissions.invalidateAll();
        LOG.info("Cache shutdown completed");
    }

    // UserProfile caching
    public UserProfile getUserProfile(String username) {
        UserProfile profile = userProfiles.getIfPresent(username);
        if (profile != null) {
            LOG.debug("Cache HIT for user profile: {}", username);
        } else {
            LOG.debug("Cache MISS for user profile: {}", username);
        }
        return profile;
    }

    public void cacheUserProfile(String username, UserProfile profile) {
        userProfiles.put(username, profile);
        LOG.debug("Cached user profile: {}", username);
    }

    // HackerRankProfile caching
    public HackerRankProfile getHackerRankProfile(String username) {
        HackerRankProfile profile = hackerRankProfiles.getIfPresent(username);
        if (profile != null) {
            LOG.debug("Cache HIT for HackerRank profile: {}", username);
        } else {
            LOG.debug("Cache MISS for HackerRank profile: {}", username);
        }
        return profile;
    }

    public void cacheHackerRankProfile(String username, HackerRankProfile profile) {
        hackerRankProfiles.put(username, profile);
        LOG.debug("Cached HackerRank profile: {}", username);
    }

    // ContestHistory caching
    public UserContestHistory getContestHistory(String username) {
        UserContestHistory history = contestHistories.getIfPresent(username);
        if (history != null) {
            LOG.debug("Cache HIT for contest history: {}", username);
        } else {
            LOG.debug("Cache MISS for contest history: {}", username);
        }
        return history;
    }

    public void cacheContestHistory(String username, UserContestHistory history) {
        contestHistories.put(username, history);
        LOG.debug("Cached contest history: {}", username);
    }

    // Submissions caching
    public List<UserSubmissions.RecentSubmission> getSubmissions(String cacheKey) {
        List<UserSubmissions.RecentSubmission> submissionsList = submissions.getIfPresent(cacheKey);
        if (submissionsList != null) {
            LOG.debug("Cache HIT for submissions: {}", cacheKey);
        } else {
            LOG.debug("Cache MISS for submissions: {}", cacheKey);
        }
        return submissionsList;
    }

    public void cacheSubmissions(String cacheKey, List<UserSubmissions.RecentSubmission> submissionsList) {
        submissions.put(cacheKey, submissionsList);
        LOG.debug("Cached submissions: {} (count: {})", cacheKey, submissionsList.size());
    }

    // Cache management
    public CacheStats getCacheStats() {
        var userProfileStats = userProfiles.stats();
        var hackerRankStats = hackerRankProfiles.stats();
        var contestHistoryStats = contestHistories.stats();
        var submissionsStats = submissions.stats();

        return new CacheStats(
                (int) userProfiles.estimatedSize(),
                (int) hackerRankProfiles.estimatedSize(),
                (int) contestHistories.estimatedSize(),
                (int) submissions.estimatedSize(),
                userProfileStats.hitRate(),
                userProfileStats.missRate(),
                userProfileStats.evictionCount()
        );
    }

    public void clearExpiredEntries() {
        LOG.info("Triggering cache cleanup (expired entries are automatically removed by Caffeine)...");
        userProfiles.cleanUp();
        hackerRankProfiles.cleanUp();
        contestHistories.cleanUp();
        submissions.cleanUp();
        LOG.info("Cache cleanup completed");
    }

    public void clearAllCache() {
        LOG.warn("Clearing all cache data...");
        userProfiles.invalidateAll();
        hackerRankProfiles.invalidateAll();
        contestHistories.invalidateAll();
        submissions.invalidateAll();
        LOG.warn("All cache data cleared");
    }

    // Updated CacheStats class with additional Caffeine metrics
    public static class CacheStats {
        private final int userProfilesCount;
        private final int hackerRankProfilesCount;
        private final int contestHistoriesCount;
        private final int submissionsCount;
        private final double hitRate;
        private final double missRate;
        private final long evictionCount;

        public CacheStats(int userProfilesCount, int hackerRankProfilesCount,
                          int contestHistoriesCount, int submissionsCount,
                          double hitRate, double missRate, long evictionCount) {
            this.userProfilesCount = userProfilesCount;
            this.hackerRankProfilesCount = hackerRankProfilesCount;
            this.contestHistoriesCount = contestHistoriesCount;
            this.submissionsCount = submissionsCount;
            this.hitRate = hitRate;
            this.missRate = missRate;
            this.evictionCount = evictionCount;
        }

        public int getUserProfilesCount() {
            return userProfilesCount;
        }

        public int getHackerRankProfilesCount() {
            return hackerRankProfilesCount;
        }

        public int getContestHistoriesCount() {
            return contestHistoriesCount;
        }

        public int getSubmissionsCount() {
            return submissionsCount;
        }

        public int getTotalCachedItems() {
            return userProfilesCount + hackerRankProfilesCount + contestHistoriesCount + submissionsCount;
        }

        public double getHitRate() {
            return hitRate;
        }

        public double getMissRate() {
            return missRate;
        }

        public long getEvictionCount() {
            return evictionCount;
        }
    }
}
