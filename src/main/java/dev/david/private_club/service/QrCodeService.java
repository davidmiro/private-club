package dev.david.private_club.service;

import dev.david.private_club.dto.QrCodeResponseDto;
import dev.david.private_club.model.Member;
import dev.david.private_club.model.QrCode;
import dev.david.private_club.repository.MemberRepository;
import dev.david.private_club.repository.QrCodeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QrCodeService {
    private final QrCodeRepository qrCodeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public QrCodeResponseDto checkIn(UUID code) {
        QrCode qrCode = qrCodeRepository.findByCode(code).
                orElseThrow(() -> new IllegalArgumentException("Invalid or expired QR code!"));
        Member member = qrCode.getMember();

        qrCodeRepository.delete(qrCode);
        QrCode newQrCode = new QrCode();
        UUID newCode = UUID.randomUUID();
        newQrCode.setCode(newCode);
        newQrCode.setMember(member);

        qrCodeRepository.save(newQrCode);

        return new QrCodeResponseDto(
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                newCode
        );
    }

    public QrCodeResponseDto generateFirstQrCode(Long memberId) {
        Member member = memberRepository.findById(memberId).
                orElseThrow(() -> new IllegalArgumentException("User not found"));

        QrCode newMemberQrCode = new QrCode();
        UUID newCode = UUID.randomUUID();
        newMemberQrCode.setCode(newCode);
        newMemberQrCode.setMember(member);

        qrCodeRepository.save(newMemberQrCode);

        return new QrCodeResponseDto(
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                newCode);
    }
}
