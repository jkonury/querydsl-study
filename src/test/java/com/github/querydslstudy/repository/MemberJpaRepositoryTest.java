package com.github.querydslstudy.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.github.querydslstudy.entity.Member;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

  @Autowired
  EntityManager em;

  @Autowired
  MemberJpaRepository memberJpaRepository;

  @Test
  public void basicTest() {
    Member member = new Member("member1", 10);
    memberJpaRepository.save(member);

    final Member findMember = memberJpaRepository.findById(member.getId()).orElse(null);
    assertThat(findMember).isEqualTo(member);

    final List<Member> members = memberJpaRepository.findAll();
    assertThat(members).containsExactly(member);

    final List<Member> result = memberJpaRepository.findByUsername(member.getUsername());
    assertThat(result).containsExactly(member);
  }

  @Test
  public void basicQuerydslTest() {
    Member member = new Member("member1", 10);
    memberJpaRepository.save(member);

    final Member findMember = memberJpaRepository.findById(member.getId()).orElse(null);
    assertThat(findMember).isEqualTo(member);

    final List<Member> members = memberJpaRepository.findAll_Querydsl();
    assertThat(members).containsExactly(member);

    final List<Member> result = memberJpaRepository.findByUsername_Querydsl(member.getUsername());
    assertThat(result).containsExactly(member);
  }
}