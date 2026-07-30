package dev.david.private_club.dto;

import java.time.LocalDate;

public record MemberUpdateDto(
        String firstName,
        String lastName,
        String email,
        LocalDate dateOfBirth
) {
}
