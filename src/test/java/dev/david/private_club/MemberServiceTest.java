package dev.david.private_club;

import dev.david.private_club.dto.MemberCreateDto;
import dev.david.private_club.dto.MemberResponseDto;
import dev.david.private_club.dto.MemberUpdateDto;
import dev.david.private_club.model.Member;
import dev.david.private_club.repository.MemberRepository;
import dev.david.private_club.service.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)

public class MemberServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void getMemberById_shouldReturnDto() {
        Member member = createMember(1L, "John", "Doe",
                "johndoe@mail.com", LocalDate.of(1991, 1, 1));
        Mockito.when(memberRepository.findMemberByIdOrThrow(1L)).thenReturn(member);

        MemberResponseDto memberResponseDto = memberService.getMemberById(1L);

        assertEquals(1L, memberResponseDto.id());
        assertEquals("John", memberResponseDto.firstName());
        assertEquals("Doe", memberResponseDto.lastName());
        assertEquals("johndoe@mail.com", memberResponseDto.email());
        assertEquals(LocalDate.of(1991, 1, 1), memberResponseDto.dateOfBirth());
    }

    @Test
    void createMember_shouldThrowException_whenEmailAlreadyExists() {
        MemberCreateDto memberCreateDto = new MemberCreateDto("John", "Doe",
                "johndoe@mail.com", LocalDate.of(1991, 1, 1)
        );

        Mockito.when(memberRepository.existsByEmail("johndoe@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> memberService.createMember(memberCreateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already registered.");
    }

    @Test
    void createMember_shouldSaveAndReturnDto() {
        MemberCreateDto memberCreateDto = new MemberCreateDto("John", "Doe",
                "johndoe@mail.com", LocalDate.of(1991, 1, 1));
        Mockito.when(memberRepository.existsByEmail("johndoe@mail.com")).thenReturn(false);
        Mockito.when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            member.setId(1L);
            return member;
        });

        MemberResponseDto result = memberService.createMember(memberCreateDto);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        Mockito.verify(memberRepository).save(captor.capture());

        Member savedMember = captor.getValue();
        assertThat(savedMember.getFirstName()).isEqualTo("John");
        assertThat(savedMember.getLastName()).isEqualTo("Doe");
        assertThat(savedMember.getEmail()).isEqualTo("johndoe@mail.com");
        assertThat(savedMember.getDateOfBirth()).isEqualTo(LocalDate.of(1991, 1, 1));

        assertThat(result.id()).isEqualTo(1);
        assertThat(result.email()).isEqualTo("johndoe@mail.com");
    }

    @Test
    void editMember_shouldUpdateAndReturnDto() {
        Member existingMember = createMember(1L, "John", "Doe",
                "johndoe@mail.com", LocalDate.of(1991, 1, 1));

        MemberUpdateDto memberUpdateDto = new MemberUpdateDto("Joe", "Blow",
                "joeblow@mail.com", LocalDate.of(1993, 12, 12));

        Mockito.when(memberRepository.findMemberByIdOrThrow(1L)).thenReturn(existingMember);
        Mockito.when(memberRepository.save(any(Member.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        MemberResponseDto result = memberService.updateMember(1L, memberUpdateDto);

        assertThat(existingMember.getFirstName()).isEqualTo("Joe");
        assertThat(existingMember.getLastName()).isEqualTo("Blow");
        assertThat(existingMember.getEmail()).isEqualTo("joeblow@mail.com");
        assertThat(existingMember.getDateOfBirth()).isEqualTo(LocalDate.of(1993, 12, 12));

        assertThat(result.firstName()).isEqualTo("Joe");
        assertThat(result.lastName()).isEqualTo("Blow");
        assertThat(result.email()).isEqualTo("joeblow@mail.com");
        assertThat(result.dateOfBirth()).isEqualTo(LocalDate.of(1993, 12, 12));
    }

    @Test
    void deleteMember_shouldRemoveMember_whenIdExists() {

        memberService.deleteMemberById(1L);

        Mockito.verify(memberRepository).deleteMemberByIdOrThrow(1L);
    }

    @Test
    void getMemberById_shouldThrow_whenMemberNotFound() {
        Mockito.when(memberRepository.findMemberByIdOrThrow(1L))
                .thenThrow(new IllegalArgumentException("User not found"));

        assertThatThrownBy(() -> memberService.getMemberById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    @Test
    void editMember_shouldThrow_whenMemberNotFound() {
        Long memberId = 999L;
        MemberUpdateDto memberUpdateDto = new MemberUpdateDto("Joe", "Blow", "joe@mail.com",
                LocalDate.of(1993, 12, 12));

        Mockito.when(memberRepository.findMemberByIdOrThrow(memberId))
                .thenThrow(new IllegalArgumentException("User not found"));

        assertThatThrownBy(() -> memberService.updateMember(memberId, memberUpdateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    @Test
    void deleteMember_shouldThrow_whenMemberNotFound() {
        Long memberId = 999L;

        Mockito.doThrow(new IllegalArgumentException("Member not found with id: " + memberId))
                .when(memberRepository).deleteMemberByIdOrThrow(memberId);

        assertThatThrownBy(() -> memberService.deleteMemberById(memberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Member not found with id: " + memberId);
    }

    private Member createMember(Long id, String firstName, String lastName, String email, LocalDate dateOfBirth) {
        Member member = new Member();
        member.setId(id);
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setEmail(email);
        member.setDateOfBirth(dateOfBirth);
        return member;
    }
}