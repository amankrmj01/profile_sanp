package com.piandphi.model;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable.Serializable
public record UserSubmissions(
        List<RecentSubmission> submissions
) {

    @Serdeable.Serializable
    public record RecentSubmission(
            String id,
            String title,
            String titleSlug,
            String timestamp,
            String status,
            String statusDisplay,
            String lang,
            String url,
            String langName,
            String runtime,
            Boolean isPending,
            String memory,
            Boolean hasNotes,
            String notes,
            String flagType,
            String frontendId,
            List<TopicTag> topicTags
    ) {
    }

    @Serdeable.Serializable
    public record TopicTag(
            String id
    ) {
    }
}
