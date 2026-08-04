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
        QrCode qrCode = qrCodeRepository.findByCodeOrThrow(code);
        Member member = qrCode.getMember();

        QrCode newQrCode = new QrCode();
        qrCodeRepository.delete(qrCode);
        newQrCode.setCode(UUID.randomUUID());
        newQrCode.setMember(member);

        qrCodeRepository.save(newQrCode);

        return new QrCodeResponseDto(
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                newQrCode.getCode()
        );
    }

    public QrCodeResponseDto generateFirstQrCode(Long id) {
        Member member = memberRepository.findMemberByIdOrThrow(id);

        QrCode newMemberQrCode = new QrCode();
        newMemberQrCode.setCode(UUID.randomUUID());
        
        newMemberQrCode.setMember(member);

        qrCodeRepository.save(newMemberQrCode);

        return new QrCodeResponseDto(
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                newMemberQrCode.getCode()
        );
    }
}