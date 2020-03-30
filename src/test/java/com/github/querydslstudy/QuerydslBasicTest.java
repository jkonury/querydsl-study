package com.github.querydslstudy;

import static com.github.querydslstudy.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.querydslstudy.entity.Member;
import com.github.querydslstudy.entity.QMember;
import com.github.querydslstudy.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
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

  @Test
  public void searchAnd() {

    final Member findMember = queryFactory
      .select(member)
      .from(member)
      .where(member.username.eq("member1")
        .and(member.age.eq(10)))
      .fetchOne();

    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

  @Test
  public void searchAnd2() {

    final Member findMember = queryFactory
      .select(member)
      .from(member)
      .where(member.username.eq("member1"),
        member.age.eq(10))
      .fetchOne();

    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

  @Test
  public void searchExam() {

    final BooleanExpression equal = member.username.eq("member1"); // username = 'member1'
    final BooleanExpression notEqual = member.username.ne("member1"); // username != 'member1'
    final BooleanExpression notEqual2 = member.username.eq("member1").not(); // username != 'member1'

    final BooleanExpression isNotNull = member.username.isNotNull(); // username is not null

    final BooleanExpression in = member.age.in(10, 20); // age in (10, 20)
    final BooleanExpression notIn = member.age.notIn(10, 20); // age not in (10, 20)
    final BooleanExpression between = member.age.between(10, 20); // age between 10 and 20

    final BooleanExpression greaterThanOrEqual = member.age.goe(30); // age >= 30
    final BooleanExpression greaterThan = member.age.gt(30); // age > 30
    final BooleanExpression lessThanOrEqual = member.age.loe(30); // age <= 30
    final BooleanExpression lessThan = member.age.lt(30); // agt < 30

    final BooleanExpression like = member.username.like("member%"); // username like 'member%'
    final BooleanExpression contains = member.username.contains("member"); // username like '%member%'
    final BooleanExpression startsWith = member.username.startsWith("member%"); // username like 'member%'
  }

  @Test
  public void resultFetch() {

    final List<Member> fetch = queryFactory
      .selectFrom(member)
      .fetch();

    final Member fetchOne = queryFactory
      .selectFrom(member)
      .where(member.username.eq("member1"))
      .fetchOne();

    final Member fetchFirst = queryFactory
      .selectFrom(QMember.member)
      .fetchFirst();

    final QueryResults<Member> results = queryFactory
      .selectFrom(member)
      .fetchResults();

    final long total = results.getTotal();
    final List<Member> contents = results.getResults();

    final long fetchCount = queryFactory
      .selectFrom(member)
      .fetchCount();
  }

  @Test
  public void sort() {
    em.persist(new Member(null, 100));
    em.persist(new Member("member5", 100));
    em.persist(new Member("member6", 100));

    final List<Member> members = queryFactory
      .selectFrom(member)
      .where(member.age.eq(100))
      .orderBy(member.age.desc(), member.username.asc().nullsLast())
      .fetch();

    final Member member5 = members.get(0);
    final Member member6 = members.get(1);
    final Member memberNull = members.get(2);

    assertThat(member5.getUsername()).isEqualTo("member5");
    assertThat(member6.getUsername()).isEqualTo("member6");
    assertThat(memberNull.getUsername()).isNull();
  }

  @Test
  public void paging1() {
    final List<Member> members = queryFactory
      .selectFrom(member)
      .orderBy(member.username.desc())
      .offset(1)
      .limit(2)
      .fetch();

    assertThat(members.size()).isEqualTo(2);
  }

  @Test
  public void paging2() {
    final QueryResults<Member> results = queryFactory
      .selectFrom(member)
      .orderBy(member.username.desc())
      .offset(1)
      .limit(2)
      .fetchResults();

    assertThat(results.getTotal()).isEqualTo(4);
    assertThat(results.getLimit()).isEqualTo(2);
    assertThat(results.getOffset()).isEqualTo(1);
    assertThat(results.getResults().size()).isEqualTo(2);
  }
}