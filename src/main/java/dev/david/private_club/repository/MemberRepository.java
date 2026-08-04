package dev.david.private_club.repository;

import dev.david.private_club.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);

    default void deleteMemberByIdOrThrow(Long id) {
        findById(id).
                orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + id));
    }
    default Member findMemberByIdOrThrow(Long id) {
        return findById(id).
                orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}