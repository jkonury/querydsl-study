package com.github.querydslstudy.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.github.querydslstudy.dto.MemberSearchCondition;
import com.github.querydslstudy.dto.MemberTeamDto;
import com.github.querydslstudy.entity.Member;
import com.github.querydslstudy.entity.Team;
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

  @Test
  public void searchByBuilderTest() {
    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");
    em.persist(teamA);
    em.persist(teamB);

    Member member1 = new Member("member1", 10, teamA);
    Member member2 = new Member("member2", 20, teamA);
    Member member3 = new Member("member3", 30, teamB);
    Member member4 = new Member("member4", 40, teamB);

    em.persist(member1);
    em.persist(member2);
    em.persist(member3);
    em.persist(member4);

    MemberSearchCondition condition = new MemberSearchCondition();
    condition.setAgeGoe(35);
    condition.setAgeLoe(40);
    condition.setTeamName("teamB");

    final List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);
    assertThat(result).extracting("username").containsExactly("member4");
  }
  @Test
  public void searchTest() {
    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");
    em.persist(teamA);
    em.persist(teamB);

    Member member1 = new Member("member1", 10, teamA);
    Member member2 = new Member("member2", 20, teamA);
    Member member3 = new Member("member3", 30, teamB);
    Member member4 = new Member("member4", 40, teamB);

    em.persist(member1);
    em.persist(member2);
    em.persist(member3);
    em.persist(member4);

    MemberSearchCondition condition = new MemberSearchCondition();
    condition.setAgeGoe(35);
    condition.setAgeLoe(40);
    condition.setTeamName("teamB");

    final List<MemberTeamDto> result = memberJpaRepository.search(condition);
    assertThat(result).extracting("username").containsExactly("member4");
  }
}