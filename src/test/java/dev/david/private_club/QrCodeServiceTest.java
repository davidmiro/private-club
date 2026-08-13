package dev.david.private_club;

import ch.qos.logback.core.testUtil.MockInitialContext;
import dev.david.private_club.dto.QrCodeResponseDto;
import dev.david.private_club.model.Member;
import dev.david.private_club.model.QrCode;
import dev.david.private_club.repository.MemberRepository;
import dev.david.private_club.repository.QrCodeRepository;
import dev.david.private_club.service.QrCodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;


import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class QrCodeServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private QrCodeRepository qrCodeRepository;

    @InjectMocks
    private QrCodeService qrCodeService;

    @Test
    void checkIn_shouldDeleteOldAndCreateNewQrCode() {
        UUID oldCode = UUID.randomUUID();
        Member member = new Member();
        member.setId(1L);
        member.setFirstName("John");
        member.setLastName("Doe");

        QrCode oldQrCode = new QrCode();
        oldQrCode.setId(10L);
        oldQrCode.setCode(oldCode);
        oldQrCode.setMember(member);

        Mockito.when(qrCodeRepository.findByCodeOrThrow(oldCode)).thenReturn(oldQrCode);
        Mockito.when(qrCodeRepository.save(any(QrCode.class))).thenAnswer(inv -> inv.getArgument(0));

        QrCodeResponseDto result = qrCodeService.checkIn(oldCode);

        Mockito.verify(qrCodeRepository).delete(oldQrCode);

        ArgumentCaptor<QrCode> qrCodeCaptor = ArgumentCaptor.forClass(QrCode.class);
        Mockito.verify(qrCodeRepository).save(qrCodeCaptor.capture());

        QrCode saved = qrCodeCaptor.getValue();
        assertThat(saved.getCode()).isNotNull().isNotEqualTo(oldCode);
        assertThat(saved.getMember()).isEqualTo(member);

        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.qrCode()).isEqualTo(saved.getCode());
    }


    @Test
    void generateFirstQrCode_shouldCreateNewQrCode() {

        Long memberId = 1L;
        Member member = new Member();
        member.setId(memberId);
        member.setFirstName("John");
        member.setLastName("Doe");

        Mockito.when(memberRepository.findMemberByIdOrThrow(memberId)).thenReturn(member);
        Mockito.when(qrCodeRepository.save(any(QrCode.class))).thenAnswer(inv -> inv.getArgument(0));

        QrCodeResponseDto result = qrCodeService.generateFirstQrCode(memberId);

        ArgumentCaptor<QrCode> qrCaptor = ArgumentCaptor.forClass(QrCode.class);
        Mockito.verify(qrCodeRepository).save(qrCaptor.capture());

        QrCode saved = qrCaptor.getValue();
        assertThat(saved.getCode()).isNotNull();
        assertThat(saved.getMember()).isEqualTo(member);

        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.qrCode()).isEqualTo(saved.getCode());

    }

    @Test
    void checkIn_shouldThrow_whenQrCodeNotFound() {
        UUID code = UUID.randomUUID();

        Mockito.when(qrCodeRepository.findByCodeOrThrow(code))
                .thenThrow(new IllegalArgumentException("Invalid or expired QR code!"));

        assertThatThrownBy(() -> qrCodeService.checkIn(code))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired QR code!");
    }

    @Test
    void generateFirstQrCode_shouldThrow_whenMemberNotFound() {
        Long memberId = 999L;
        Mockito.when(memberRepository.findMemberByIdOrThrow(memberId))
                .thenThrow(new IllegalArgumentException("User not found"));

        assertThatThrownBy(() -> qrCodeService.generateFirstQrCode(memberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }
}
