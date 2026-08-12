package dev.david.private_club.controller;

import dev.david.private_club.model.Member;
import dev.david.private_club.model.QrCode;
import dev.david.private_club.repository.MemberRepository;
import dev.david.private_club.repository.QrCodeRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class QrCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QrCodeRepository qrCodeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.xml");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

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