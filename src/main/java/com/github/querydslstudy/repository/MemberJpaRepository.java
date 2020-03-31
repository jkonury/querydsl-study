package com.github.querydslstudy.repository;

import static com.github.querydslstudy.entity.QMember.*;
import static com.github.querydslstudy.entity.QTeam.team;
import static org.springframework.util.StringUtils.*;

import com.github.querydslstudy.dto.MemberSearchCondition;
import com.github.querydslstudy.dto.MemberTeamDto;
import com.github.querydslstudy.dto.QMemberTeamDto;
import com.github.querydslstudy.entity.Member;
import com.github.querydslstudy.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

  private final EntityManager em;
  private final JPAQueryFactory queryFactory;

//  public MemberJpaRepository(EntityManager em) {
//    this.em = em;
//    this.queryFactory = new JPAQueryFactory(em);
//  }

  public void save(Member member) {
    em.persist(member);
  }

  public Optional<Member> findById(Long id) {
    final Member member = em.find(Member.class, id);
    return Optional.ofNullable(member);
  }

  public List<Member> findAll() {
    return em.createQuery("select m from Member m", Member.class)
      .getResultList();
  }

  public List<Member> findAll_Querydsl() {
    return queryFactory
      .selectFrom(member)
      .fetch();
  }

  public List<Member> findByUsername(String username) {
    return em.createQuery("select m from Member m where m.username = :username", Member.class)
      .setParameter("username", username)
      .getResultList();
  }

  public List<Member> findByUsername_Querydsl(String username) {
    return queryFactory
      .selectFrom(member)
      .where(member.username.eq(username))
      .fetch();
  }

  public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {

    BooleanBuilder builder = new BooleanBuilder();
    if (hasText(condition.getUsername())) {
      builder.and(member.username.eq(condition.getUsername()));
    }

    if (hasText(condition.getTeamName())) {
      builder.and(team.name.eq(condition.getTeamName()));
    }

    if (condition.getAgeGoe() != null) {
      builder.and(member.age.goe(condition.getAgeGoe()));
    }

    if (condition.getAgeLoe() != null) {
      builder.and(member.age.goe(condition.getAgeLoe()));
    }

    return queryFactory
      .select(new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name))
      .from(member)
      .leftJoin(member.team, team)
      .where(builder)
      .fetch();
  }
}
