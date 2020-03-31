package com.github.querydslstudy.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.github.querydslstudy.entity.Member;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

  @Autowired
  MemberRepository memberRepository;

  @Test
  public void basicTest() {
    Member member = new Member("member1", 10);
    memberRepository.save(member);

    final Member findMember = memberRepository.findById(member.getId()).orElse(null);
    assertThat(findMember).isEqualTo(member);

    final List<Member> members = memberRepository.findAll();
    assertThat(members).containsExactly(member);

    final List<Member> result = memberRepository.findByUsername(member.getUsername());
    assertThat(result).containsExactly(member);
  }
}