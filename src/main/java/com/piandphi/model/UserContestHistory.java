package com.piandphi.model;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable.Serializable
public record UserContestHistory(
        UserContestRanking userContestRanking,
        List<ContestHistoryEntry> userContestRankingHistory
) {

    @Serdeable.Serializable
    public record UserContestRanking(
            Integer attendedContestsCount,
            Double rating,
            Integer globalRanking,
            Integer totalParticipants,
            Double topPercentage,
            Badge badge
    ) {
    }

    @Serdeable.Serializable
    public record Badge(
            String name
    ) {
    }

    @Serdeable.Serializable
    public record ContestHistoryEntry(
            Boolean attended,
            String trendDirection,
            Integer problemsSolved,
            Integer totalProblems,
            Integer finishTimeInSeconds,
            Double rating,
            Integer ranking,
            Contest contest
    ) {
    }

    @Serdeable.Serializable
    public record Contest(
            String title,
            String startTime
    ) {
    }
}
