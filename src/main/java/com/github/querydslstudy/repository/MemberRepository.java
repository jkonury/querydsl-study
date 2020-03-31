package com.github.querydslstudy.repository;

import com.github.querydslstudy.entity.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

  List<Member> findByUsername(String username);
}
