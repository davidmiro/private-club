package dev.david.private_club.repository;

import dev.david.private_club.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);

}
