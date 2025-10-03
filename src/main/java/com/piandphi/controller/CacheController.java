package com.piandphi.controller;

import com.piandphi.service.ProfileCacheService;
import io.micronaut.http.annotation.*;
import io.micronaut.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/cache")
public class CacheController {

    private static final Logger LOG = LoggerFactory.getLogger(CacheController.class);

    private final ProfileCacheService cacheService;

    public CacheController(ProfileCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Get("/stats")
    public ProfileCacheService.CacheStats getCacheStats() {
        LOG.info("Returning cache statistics");
        return cacheService.getCacheStats();
    }

    @Post("/clear")
    public HttpResponse<String> clearAllCache() {
        LOG.warn("Clearing all cache data via API request");
        cacheService.clearAllCache();
        return HttpResponse.ok("All cache data cleared successfully");
    }

    @Post("/cleanup")
    public HttpResponse<String> cleanupExpiredEntries() {
        LOG.info("Manual cleanup of expired cache entries requested");
        cacheService.clearExpiredEntries();
        return HttpResponse.ok("Expired cache entries cleaned up successfully");
    }

    @Get("/health")
    public HttpResponse<String> getCacheHealth() {
        ProfileCacheService.CacheStats stats = cacheService.getCacheStats();
        return HttpResponse.ok(String.format("Cache is healthy - Total items: %d", stats.getTotalCachedItems()));
    }
}
