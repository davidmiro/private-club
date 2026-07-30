package dev.david.private_club.controller;

import dev.david.private_club.dto.QrCodeResponseDto;
import dev.david.private_club.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/qr-codes")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @PostMapping("/generate/{memberId}")
    public ResponseEntity<QrCodeResponseDto> generateFirstQrCode(@PathVariable Long memberId) {
        QrCodeResponseDto responseDto = qrCodeService.generateFirstQrCode(memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/scanner/{code}")
    public ResponseEntity<QrCodeResponseDto> scanQrCode(@PathVariable UUID code) {
        QrCodeResponseDto responseDto = qrCodeService.checkIn(code);
        return ResponseEntity.ok(responseDto);
    }

}
