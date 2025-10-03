package com.piandphi.model;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

@Serdeable.Serializable
public record UserProfile(
    String username,
    String githubUrl,
    String twitterUrl,
    String linkedinUrl,
    Profile profile,
    SubmitStats submitStats,
    ContestBadge contestBadge
) {

    @Serdeable.Serializable
    public record Profile(
        String userAvatar,
        String realName,
        List<String> websites,
        String countryName,
        String company,
        String jobTitle,
        List<String> skillTags,
        String school,
        String aboutMe,
        Integer postViewCount,
        Integer postViewCountDiff,
        Integer reputation,
        Integer ranking,
        Integer reputationDiff,
        Integer solutionCount,
        Integer solutionCountDiff,
        Integer categoryDiscussCount,
        Integer categoryDiscussCountDiff,
        String certificationLevel
    ) {}

    @Serdeable.Serializable
    public record SubmitStats(
        List<SubmissionCount> acSubmissionNum,
        List<SubmissionCount> totalSubmissionNum
    ) {}

    @Serdeable.Serializable
    public record SubmissionCount(
        String difficulty,
        Integer count,
        Integer submissions
    ) {}

    @Serdeable.Serializable
    public record ContestBadge(
        String name,
        Boolean expired,
        String hoverText,
        String icon
    ) {}
}
