package dev.david.private_club.service;

import dev.david.private_club.dto.MemberCreateDto;
import dev.david.private_club.dto.MemberResponseDto;
import dev.david.private_club.dto.MemberUpdateDto;
import dev.david.private_club.model.Member;
import dev.david.private_club.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponseDto getMemberById(Long id) {

        return mapToResponseDto(memberRepository.findMemberByIdOrThrow(id));
    }

    public MemberResponseDto createMember(@NonNull MemberCreateDto memberCreateDto) {
        if (memberRepository.existsByEmail(memberCreateDto.email())) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        Member newMember = new Member();

        newMember.setFirstName(memberCreateDto.firstName());
        newMember.setLastName(memberCreateDto.lastName());
        newMember.setEmail(memberCreateDto.email());
        newMember.setDateOfBirth(memberCreateDto.dateOfBirth());

        Member savedMember = memberRepository.save(newMember);

        return mapToResponseDto(savedMember);
    }

    public MemberResponseDto updateMember(Long id, MemberUpdateDto memberUpdateDto) {

        Member existingMember = memberRepository.findMemberByIdOrThrow(id);
        existingMember.setFirstName(memberUpdateDto.firstName());
        existingMember.setLastName(memberUpdateDto.lastName());
        existingMember.setEmail(memberUpdateDto.email());
        existingMember.setDateOfBirth(memberUpdateDto.dateOfBirth());

        Member updateMember = memberRepository.save(existingMember);

        return mapToResponseDto(updateMember);
    }

    public void deleteMemberById(Long id) {
        memberRepository.deleteMemberByIdOrThrow(id);
    }

    private MemberResponseDto mapToResponseDto(Member member) {
        return new MemberResponseDto(
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                member.getEmail(),
                member.getDateOfBirth()
        );
    }
}