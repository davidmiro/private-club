package dev.david.private_club.dto;


import java.util.UUID;

public record QrCodeResponseDto(
        Long memberId,
        String firstName,
        String lastName,
        UUID qrCode
) {
}