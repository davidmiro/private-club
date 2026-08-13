package dev.david.private_club.repository;

import dev.david.private_club.exception.QrCodeNotFoundException;
import dev.david.private_club.model.Member;
import dev.david.private_club.model.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {
    Optional<QrCode> findByCode(UUID code);

    default QrCode findByCodeOrThrow(UUID code) {
        return findByCode(code).
                orElseThrow(() -> new QrCodeNotFoundException("Invalid or expired QR code!"));
    }
}