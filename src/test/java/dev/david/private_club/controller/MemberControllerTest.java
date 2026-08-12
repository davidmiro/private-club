package dev.david.private_club.controller;

import com.fasterxml.jackson.databind.SerializationFeature;
import dev.david.private_club.dto.MemberCreateDto;
import dev.david.private_club.dto.MemberUpdateDto;
import dev.david.private_club.model.Member;
import dev.david.private_club.repository.MemberRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void shouldCreateMemberSuccessfully() {
        MemberCreateDto memberCreateDto = new MemberCreateDto(
                "John", "Doe", "john@mail.com", LocalDate.of(1991, 1, 1)
        );
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberCreateDto)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@mail.com"))
                .andExpect(jsonPath("$.dateOfBirth").value("1991-01-01"));
    }

    @Test
    @SneakyThrows
    void shouldReturnMemberByIdWhenExists() {

        Member member = new Member();
        member.setFirstName("John");
        member.setLastName("Doe");
        member.setEmail("john.doe@mail.com");
        member.setDateOfBirth(LocalDate.of(1991, 1, 1));

        Member savedMember = memberRepository.save(member);

        mockMvc.perform(get("/api/members/{id}", savedMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedMember.getId()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@mail.com"));
    }

    @Test
    @SneakyThrows
    void shouldUpdateMemberSuccessfully() {

        Member member = new Member();
        member.setFirstName("John");
        member.setLastName("Doe");
        member.setEmail("john.update@mail.com");
        member.setDateOfBirth(LocalDate.of(1991, 1, 1));
        Member savedMember = memberRepository.save(member);

        MemberUpdateDto memberUpdateDto = new MemberUpdateDto("Johnny", "Updated",
                "johnny.updated@mail.com", LocalDate.of(1991, 1, 1));

        mockMvc.perform(put("/api/members/{id}", savedMember.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.lastName").value("Updated"))
                .andExpect(jsonPath("$.email").value("johnny.updated@mail.com"));

    }

    @Test
    @SneakyThrows
    void shouldDeleteMemberWhenIdExists() {

        Member member = new Member();
        member.setFirstName("John");
        member.setLastName("Doe");
        member.setEmail("john.delete@mail.com");
        member.setDateOfBirth(LocalDate.of(1991, 1, 1));

        Member savedMember = memberRepository.save(member);

        mockMvc.perform(delete("/api/members/{id}", savedMember.getId()))
                .andExpect(status().isNoContent());

        assertThat(memberRepository.findById(savedMember.getId())).isEmpty();
    }

    @Test
    @SneakyThrows
    void shouldReturn404WhenMemberNotFound() {
        Long nonExistentId = 999999L;

        mockMvc.perform(get("/api/members/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldReturn400WhenCreateMemberWithInvalidEmail() {
        MemberCreateDto invalidDto = new MemberCreateDto(
                "John", "Doe", "not-an-email", LocalDate.of(1991, 1, 1)
        );

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void shouldReturn400WhenCreateMemberWithBlankFields() {

        MemberCreateDto invalidDto = new MemberCreateDto(
                "", "Doe", "john@mail.com", null);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void shouldReturn404WhenUpdateNonExistentMember() {

        Long nonExistentId = 999999L;
        MemberUpdateDto memberUpdateDto = new MemberUpdateDto(
                "Johnny", "Updated",
                "johnny.updated@mail.com", LocalDate.of(1991, 1, 1)
        );

        mockMvc.perform(put("/api/members/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberUpdateDto)))
                .andExpect(status().isNotFound());
    }
}