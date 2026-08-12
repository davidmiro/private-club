package dev.david.private_club.controller;

import dev.david.private_club.dto.MemberCreateDto;
import dev.david.private_club.dto.MemberResponseDto;
import dev.david.private_club.dto.MemberUpdateDto;
import dev.david.private_club.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberResponseDto> createMember(@Valid @RequestBody MemberCreateDto memberCreateDto) {
        MemberResponseDto responseDto = memberService.createMember(memberCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDto> getMember(@PathVariable Long id) {
        MemberResponseDto responseDto = memberService.getMemberById(id);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    ResponseEntity<MemberResponseDto> updateMember(@PathVariable Long id,
                                                 @Valid @RequestBody MemberUpdateDto memberUpdateDto) {
        MemberResponseDto responseDto = memberService.updateMember(id, memberUpdateDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        memberService.deleteMemberById(id);
        return ResponseEntity.noContent().build();
    }
}