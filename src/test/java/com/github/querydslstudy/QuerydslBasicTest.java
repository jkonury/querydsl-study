package com.github.querydslstudy;

import static com.github.querydslstudy.entity.QMember.member;
import static com.github.querydslstudy.entity.QTeam.team;
import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.iterable;

import com.github.querydslstudy.dto.MemberDto;
import com.github.querydslstudy.dto.QMemberDto;
import com.github.querydslstudy.dto.UserDto;
import com.github.querydslstudy.entity.Member;
import com.github.querydslstudy.entity.QMember;
import com.github.querydslstudy.entity.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

  @Autowired
  EntityManager em;

  JPAQueryFactory queryFactory;

  @PersistenceUnit
  EntityManagerFactory emf;

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
      .selectFrom(member)
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

  @Test
  public void aggregation() {
    final List<Tuple> result = queryFactory
      .select(
        member.count(),
        member.age.sum(),
        member.age.avg(),
        member.age.min(),
        member.age.max())
      .from(member)
      .fetch();

    final Tuple tuple = result.get(0);
    assertThat(tuple.get(member.count())).isEqualTo(4);
    assertThat(tuple.get(member.age.sum())).isEqualTo(100);
    assertThat(tuple.get(member.age.avg())).isEqualTo(25);
    assertThat(tuple.get(member.age.min())).isEqualTo(10);
    assertThat(tuple.get(member.age.max())).isEqualTo(40);
  }

  @Test
  public void groupBy() {
    final List<Tuple> result = queryFactory
      .select(team.name, member.age.avg())
      .from(member)
      .join(member.team, team)
      .groupBy(team.name)
      .fetch();

    final Tuple teamA = result.get(0);
    final Tuple teamB = result.get(1);

    assertThat(teamA.get(team.name)).isEqualTo("teamA");
    assertThat(teamB.get(team.name)).isEqualTo("teamB");

    assertThat(teamA.get(member.age.avg())).isEqualTo(15);
    assertThat(teamB.get(member.age.avg())).isEqualTo(35);
  }

  @Test
  public void join() {
    final List<Member> members = queryFactory
      .selectFrom(member)
      .join(member.team, team)
      .on(team.name.eq("teamA"))
      .fetch();

    assertThat(members)
      .extracting("username")
      .containsExactly("member1", "member2");
  }
  @Test
  public void join2() {
    final List<Member> members = queryFactory
      .selectFrom(member)
      .join(member.team, team)
      .where(team.name.eq("teamA"))
      .fetch();

    assertThat(members)
      .extracting("username")
      .containsExactly("member1", "member2");
  }

  // 세타 조인(연관 관계가 없는 필드로 조인)
  @Test
  public void thetaJoin() {
    em.persist(new Member("teamA"));
    em.persist(new Member("teamB"));

    final List<Member> members = queryFactory
      .select(member)
      .from(member, team)
      .where(member.username.eq(team.name))
      .fetch();

    assertThat(members)
      .extracting("username")
      .containsExactly("teamA", "teamB");
  }

  /**
   * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
   * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
   * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='teamA'
   */
  @Test
  public void join_on_filtering() {
    final List<Tuple> result = queryFactory
      .select(member, team)
      .from(member)
      .leftJoin(member.team, team).on(team.name.eq("teamA"))
      .fetch();

    for (Tuple tuple : result) {
      System.out.println("tuple = " + tuple);
    }

    assertThat(result.size()).isEqualTo(4);
  }

  // 연관 관계가 없는 엔티티 외부조인
  @Test
  public void join_on_no_relation() {
    em.persist(new Member("teamA"));
    em.persist(new Member("teamB"));
    em.persist(new Member("teamC"));

    final List<Tuple> result = queryFactory
      .select(member, team)
      .from(member)
      .leftJoin(team).on(member.username.eq(team.name))
      .fetch();

    for (Tuple tuple : result) {
      System.out.println("tuple = " + tuple);
    }

    assertThat(result.size()).isEqualTo(7);
  }

  @Test
  public void noFetchJoin() {
    em.flush();
    em.clear();

    final Member findMember = queryFactory
      .selectFrom(member)
      .where(member.username.eq("member1"))
      .fetchOne();

    final boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
    assertThat(loaded).as("페치 조인 미적용").isFalse();
  }

  @Test
  public void fetchJoin() {
    em.flush();
    em.clear();

    final Member findMember = queryFactory
      .selectFrom(member)
      .join(member.team, team).fetchJoin()
      .where(member.username.eq("member1"))
      .fetchOne();

    final boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
    assertThat(loaded).isTrue();
  }

  @Test
  public void subQuery() {
    QMember memberSub = new QMember("memberSub");

    final List<Member> result = queryFactory
      .selectFrom(member)
      .where(member.age.eq(
        select(memberSub.age.max())
          .from(memberSub)
      ))
      .fetch();

    assertThat(result).extracting("age")
      .containsExactly(40);
  }

  @Test
  public void subQueryGoe() {
    QMember memberSub = new QMember("memberSub");

    final List<Member> result = queryFactory
      .selectFrom(member)
      .where(member.age.goe(
        select(memberSub.age.avg())
          .from(memberSub)
      ))
      .fetch();

    assertThat(result).extracting("age")
      .containsExactly(30, 40);
  }

  @Test
  public void subQueryIn() {
    QMember memberSub = new QMember("memberSub");

    final List<Member> result = queryFactory
      .selectFrom(member)
      .where(member.age.in(
        select(memberSub.age)
          .from(memberSub)
          .where(memberSub.age.gt(10))
      ))
      .fetch();

    assertThat(result).extracting("age")
      .containsExactly(20, 30, 40);
  }

  @Test
  public void selectSubQuery() {
    QMember memberSub = new QMember("memberSub");

    final List<Tuple> result = queryFactory
      .select(
        member.username,
        select(memberSub.age.avg())
          .from(memberSub)
      )
      .from(member)
      .fetch();

    for (Tuple tuple : result) {
      System.out.println("tuple = " + tuple);
    }

    assertThat(result.size()).isEqualTo(4);
  }

  @Test
  public void basicCase() {
    final List<String> result = queryFactory
      .select(
        member.age
          .when(10).then("10살")
          .when(20).then("20살")
          .otherwise("기타"))
      .from(member)
      .fetch();

    for (String s : result) {
      System.out.println("s = " + s);
    }

    assertThat(result.size()).isEqualTo(4);
  }

  @Test
  public void complexCase() {
    final List<Tuple> result = queryFactory
      .select(
        member.age,
        new CaseBuilder()
          .when(member.age.between(0, 20)).then("0~20살")
          .when(member.age.between(21, 30)).then("21~30살")
          .otherwise("기타"))
      .from(member)
      .fetch();

    for (Tuple tuple : result) {
      System.out.println("tuple = " + tuple);
    }

    assertThat(result.size()).isEqualTo(4);
  }

  @Test
  public void constant() {
    final List<Tuple> result = queryFactory
      .select(member.username, Expressions.constant("A"))
      .from(member)
      .fetch();

    for (Tuple tuple : result) {
      System.out.println("tuple = " + tuple);
    }
    assertThat(result.size()).isEqualTo(4);
  }

  @Test
  public void concat() {
    final String s = queryFactory
      .select(member.username.concat("_").concat(member.age.stringValue()))
      .from(member)
      .where(member.username.eq("member1"))
      .fetchOne();

    System.out.println("s = " + s);
    assertThat(s).isEqualTo("member1_10");

  }

  @Test
  public void simpleProjection() {
    final List<String> result = queryFactory
      .select(member.username)
      .from(member)
      .fetch();

    for (String s : result) {
      System.out.println("s = " + s);
    }
    assertThat(result.size()).isEqualTo(4);
  }

  @Test
  public void tupleProjection() {
    final List<Tuple> result = queryFactory
      .select(member.username, member.age)
      .from(member)
      .fetch();

    for (Tuple tuple : result) {
      final String username = tuple.get(member.username);
      final Integer age = tuple.get(member.age);

      System.out.println("username = " + username);
      System.out.println("age = " + age);
    }
    assertThat(result.size()).isEqualTo(4);
  }

  @Test
  public void findDtoByJPQL() {
    final List<MemberDto> result = em
      .createQuery("select new com.github.querydslstudy.dto.MemberDto(m.username, m.age) from Member m",
        MemberDto.class)
      .getResultList();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }

    assertThat(result.size()).isEqualTo(4);
  }

  @Test
  public void findDtoBySetter() {
    final List<MemberDto> result = queryFactory
      .select(Projections.bean(MemberDto.class,
        member.username,
        member.age))
      .from(member)
      .fetch();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }

    assertThat(result.size()).isEqualTo(4);
  }

  @Test
  public void findDtoByField() {
    final List<MemberDto> result = queryFactory
      .select(Projections.fields(MemberDto.class,
        member.username,
        member.age))
      .from(member)
      .fetch();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }

    assertThat(result.size()).isEqualTo(4);
  }

  @Test
  public void findDtoByConstructor() {
    final List<MemberDto> result = queryFactory
      .select(Projections.constructor(MemberDto.class,
        member.username,
        member.age))
      .from(member)
      .fetch();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }

    assertThat(result.size()).isEqualTo(4);
  }

  @Test
  public void findUserDtoByField() {
    QMember memberSub = new QMember("memberSub");

    final List<UserDto> result = queryFactory
      .select(Projections.fields(UserDto.class,
        member.username.as("name"),
        ExpressionUtils.as(
          select(memberSub.age.max())
          .from(memberSub), "age"
        )))
      .from(member)
      .fetch();

    for (UserDto userDto : result) {
      System.out.println("userDto = " + userDto);
    }

    assertThat(result.size()).isEqualTo(4);
  }

  @Test
  public void findUserDtoByConstructor() {

    final List<UserDto> result = queryFactory
      .select(Projections.constructor(UserDto.class,
        member.username,
        member.age))
      .from(member)
      .fetch();

    for (UserDto userDto : result) {
      System.out.println("userDto = " + userDto);
    }

    assertThat(result.size()).isEqualTo(4);
  }

  @Test
  public void findDtoByQueryProjection() {
    final List<MemberDto> result = queryFactory
      .select(new QMemberDto(member.username, member.age))
      .from(member)
      .fetch();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }

    assertThat(result.size()).isEqualTo(4);
  }

  @Test
  public void distinct() {
    em.persist(new Member("member"));
    em.persist(new Member("member"));
    em.persist(new Member("member"));

    final List<MemberDto> result = queryFactory
      .select(new QMemberDto(member.username, member.age)).distinct()
      .from(member)
      .fetch();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }

    assertThat(result.size()).isEqualTo(5);
  }

  @Test
  public void dynamicQuery_BooleanBuilder() {
    final String username = "member1";
    final int age = 10;

    final List<Member> result = searchMember1(username, age);

    assertThat(result.size()).isEqualTo(1);
  }

  @Test
  public void dynamicQuery_WhereParam() {
    final String username = "member1";
    final int age = 10;

    final List<Member> result = searchMember2(username, age);

    assertThat(result.size()).isEqualTo(1);
  }

  @Test
  public void dynamicQuery_Where() {
    final String username = "member1";
    final int age = 10;


    final List<Member> result = queryFactory
      .selectFrom(member)
      .where(searchMember3(username, age))
      .fetch();

    assertThat(result.size()).isEqualTo(1);
  }

  private Predicate[] searchMember3(String username, int age) {
    final List<Predicate> predicates = new ArrayList<>();

    if (StringUtils.isNotBlank(username)) {
      predicates.add(member.username.eq(username));
    }

    if (age > 0) {
      predicates.add(member.age.eq(age));
    }
    return predicates.toArray(new Predicate[0]);
  }

  private List<Member> searchMember1(String username, int age) {
    BooleanBuilder builder = new BooleanBuilder();

    if (StringUtils.isNotBlank(username)) {
      builder.and(member.username.eq(username));
    }

    if (age > 0) {
      builder.and(member.age.eq(age));
    }

    return queryFactory
      .selectFrom(member)
      .where(builder)
      .fetch();
  }

  private List<Member> searchMember2(String username, int age) {

    return queryFactory
      .selectFrom(member)
//      .where(usernameEq(username), ageEq(age))
      .where(allEq(username, age))
      .fetch();
  }

  private Predicate allEq(String username, int age) {
    return usernameEq(username).and(ageEq(age));
  }

  private BooleanExpression usernameEq(String username) {
    if (StringUtils.isBlank(username)) {
      return null;
    }
    return member.username.eq(username);
  }

  private BooleanExpression ageEq(int age) {
    if (age <= 0) {
      return null;
    }
    return member.age.eq(age);
  }

  @Test
  public void bulkUpdate() {
    final long count = queryFactory
      .update(member)
      .set(member.username, "비회원")
      .where(member.age.lt(28))
      .execute();

    assertThat(count).isEqualTo(2);

    System.out.println("========================================");
    final Member persistenceFindMember = queryFactory
      .selectFrom(member)
      .where(member.age.eq(10))
      .fetchOne();
    assertThat(persistenceFindMember.getUsername()).isEqualTo("member1");
    System.out.println(persistenceFindMember);

    em.flush();
    em.clear();
    System.out.println("========================================");
    final Member dbFindMember = queryFactory
      .selectFrom(member)
      .where(member.age.eq(10))
      .fetchOne();

    assertThat(dbFindMember.getUsername()).isEqualTo("비회원");
    System.out.println(dbFindMember);
  }

  @Test
  public void bulkAdd() {
    queryFactory
      .update(member)
      .set(member.age, member.age.add(1))
      .execute();

    queryFactory
      .update(member)
      .set(member.age, member.age.add(-10))
      .execute();

    queryFactory
      .update(member)
      .set(member.age, member.age.multiply(2))
      .execute();

  }

  @Test
  public void bulkDelete() {
    queryFactory
      .delete(member)
      .where(member.age.gt(20))
      .execute();

  }

  @Test
  public void sqlFunction() {
    final List<String> result = queryFactory
      .select(
        Expressions.stringTemplate(
          "function('replace', {0}, {1}, {2})",
          member.username, "member", "M"))
      .from(member)
      .fetch();

    for (String s : result) {
      System.out.println("s = " + s);
    }

    assertThat(result.size()).isEqualTo(4);
  }

  @Test
  public void sqlFunction2() {
    final List<String> result = queryFactory
      .select(member.username)
      .from(member)
      .where(member.username.eq(
        Expressions.stringTemplate(
          "function('lower', {0})", member.username)))
      .fetch();

    for (String s : result) {
      System.out.println("s = " + s);
    }

    assertThat(result.size()).isEqualTo(4);
  }

  @Test
  public void sqlFunction3() {
    final List<String> result = queryFactory
      .select(member.username)
      .from(member)
      .where(member.username.eq(member.username.lower()))
      .fetch();

    for (String s : result) {
      System.out.println("s = " + s);
    }

    assertThat(result.size()).isEqualTo(4);
  }
}
