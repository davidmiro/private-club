package dev.david.private_club.controller;

import dev.david.private_club.AbstractIntegrationTest;
import dev.david.private_club.model.Member;
import dev.david.private_club.model.QrCode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class QrCodeControllerTest extends AbstractIntegrationTest {

    @Test
    @SneakyThrows
    void shouldGenerateFirstQrCodeSuccessfully() {

        Member member = new Member();
        member.setFirstName("John");
        member.setLastName("Doe");
        member.setEmail("john@mail.com");
        member.setDateOfBirth(LocalDate.of(1991, 1, 1));

        Member savedMember = memberRepository.save(member);

        mockMvc.perform(post("/api/qr-codes/generate/{memberId}", savedMember.getId()))
                .andExpect(jsonPath("$.qrCode").exists())
                .andExpect(jsonPath("$.memberId").value(savedMember.getId()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @SneakyThrows
    void shouldReturn404WhenGenerateQrCodeForNonExistentMember() {

        Long nonExistentId = 999999L;

        mockMvc.perform(post("/api/qr-codes/generate/{memberId}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldScanQrCodeSuccessfully() {

        Member member = new Member();
        member.setFirstName("Jane");
        member.setLastName("Smith");
        member.setEmail("jane.smith@mail.com");
        member.setDateOfBirth(LocalDate.of(1995, 5, 5));
        Member savedMember = memberRepository.save(member);

        QrCode qrCode = new QrCode();
        UUID validUuid = UUID.randomUUID();
        qrCode.setCode(validUuid);
        qrCode.setMember(savedMember);
        qrCodeRepository.save(qrCode);

        mockMvc.perform(post("/api/qr-codes/scanner/{code}", validUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCode").exists())
                .andExpect(jsonPath("$.qrCode").value(org.hamcrest.Matchers.not(validUuid.toString())))
                .andExpect(jsonPath("$.memberId").value(savedMember.getId()))
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    @SneakyThrows
    void shouldReturn404WhenScanNonExistentQrCode() {

        UUID nonExistentUuid = UUID.randomUUID();

        mockMvc.perform(post("/api/qr-codes/scanner/{code}", nonExistentUuid))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldReturn400WhenScanWithInvalidUuidFormat() {

        String invalidQrCode = "not-qr-code";

        mockMvc.perform(post("/api/qr-codes/scanner/{code}", invalidQrCode))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void shouldReturn404WhenScanningQrCodeSecondTime() {
        Member member = new Member();
        member.setFirstName("Jane");
        member.setLastName("Smit");
        member.setEmail("jane.smit@mail.com");
        member.setDateOfBirth(LocalDate.of(1995, 5, 5));
        Member savedMember = memberRepository.save(member);

        QrCode qrCode = new QrCode();
        UUID code = UUID.randomUUID();
        qrCode.setCode(code);
        qrCode.setMember(savedMember);
        qrCodeRepository.save(qrCode);

        mockMvc.perform(post("/api/qr-codes/scanner/{code}", code))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/qr-codes/scanner/{code}", code))
                .andExpect(status().isNotFound());
    }
}