package com.piandphi.controller;

import com.piandphi.model.HackerRankProfile;
import com.piandphi.scraper.HackerRankScraper;
import com.piandphi.service.ProfileCacheService;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/hackerrank")
public class HackerrankController {

    private static final Logger LOG = LoggerFactory.getLogger(HackerrankController.class);

    private final HackerRankScraper hackerRankScraper;
    private final ProfileCacheService cacheService;

    public HackerrankController(HackerRankScraper hackerRankScraper, ProfileCacheService cacheService) {
        this.hackerRankScraper = hackerRankScraper;
        this.cacheService = cacheService;
    }

    @Get("/{username}")
    public HackerRankProfile getHackerRank(@PathVariable String username) {
        LOG.info("Fetching HackerRank profile for: {}", username);

        // Try to get from cache first
        HackerRankProfile cachedProfile = cacheService.getHackerRankProfile(username);
        if (cachedProfile != null) {
            LOG.info("Returning cached HackerRank profile for: {}", username);
            return cachedProfile;
        }

        // Cache miss - scrape fresh data
        LOG.info("Cache miss - scraping fresh HackerRank profile for: {}", username);
        HackerRankProfile profile = hackerRankScraper.scrape(username);

        // Cache the result
        cacheService.cacheHackerRankProfile(username, profile);

        return profile;
    }
}
