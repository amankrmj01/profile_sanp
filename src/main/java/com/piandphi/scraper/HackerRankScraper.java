package com.piandphi.scraper;

import com.piandphi.model.HackerRankProfile;
import com.piandphi.resilience.ResilientScraper;
import jakarta.inject.Singleton;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

@Singleton
public class HackerRankScraper {

    private final ResilientScraper<HackerRankProfile> resilient = new ResilientScraper<>("hackerrank");

    public HackerRankProfile scrape(String username) {
        return resilient.execute(
                () -> {
                    try {
                        return fetchProfile(username);
                    } catch (IOException e) {
                        return fallbackProfile(username);
                    }
                },
                () -> fallbackProfile(username)
        );
    }

    private HackerRankProfile fetchProfile(String username) throws IOException {
        String url = "https://www.hackerrank.com/profile/" + username;
        Document doc = Jsoup.connect(url).get();

        String fullName = doc.select("h1.hr-heading-02.profile-title.ellipsis").text();
        String profilePictureUrl = doc.select("img.hr-m-t-0\\.25").attr("src");

        return new HackerRankProfile(
                username,
                fullName,
                0, // rank - default value
                0, // problemsSolved - default value
                profilePictureUrl,
                List.of(), // badges - empty list
                "" // bio - empty string
        );
    }

    private HackerRankProfile fallbackProfile(String username) {
        return new HackerRankProfile(
                username,
                "Unknown",
                0,
                0,
                "",
                List.of(),
                "Fallback profile due to scraping failure"
        );
    }

    private int parseInt(String text) {
        try {
            return Integer.parseInt(text.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
