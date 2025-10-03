package com.piandphi.model;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable.Serializable
public record HackerRankProfile(
        String username,
        String fullName,
        int rank,
        int problemsSolved,
        String profilePictureUrl,
        List<String> badges,
        String bio
) {
}
