package com.github.querydslstudy;

import static com.github.querydslstudy.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.querydslstudy.entity.Member;
import com.github.querydslstudy.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

  @Autowired
  EntityManager em;

  JPAQueryFactory queryFactory;

  @BeforeEach
  public void before() {
    queryFactory = new JPAQueryFactory(em);
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
  }

  @Test
  public void startJPQL() {
    // find member1
    final String query = "select m from Member m where m.username = :username";
    final Member findMember = em
      .createQuery(query, Member.class)
      .setParameter("username", "member1")
      .getSingleResult();

    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

  @Test
  public void startQuerydsl() {


    final Member findMember = queryFactory
      .select(member)
      .from(member)
      .where(member.username.eq("member1"))
      .fetchOne();

    assertThat(findMember.getUsername()).isEqualTo("member1");
  }
}
