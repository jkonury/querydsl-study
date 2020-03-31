package com.github.querydslstudy.repository;

import static com.github.querydslstudy.entity.QMember.member;
import static com.github.querydslstudy.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

import com.github.querydslstudy.dto.MemberSearchCondition;
import com.github.querydslstudy.entity.Member;
import com.github.querydslstudy.repository.support.Querydsl4RepositorySupport;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class MemberQuerydslSupportRepository extends Querydsl4RepositorySupport {

  public MemberQuerydslSupportRepository() {
    super(Member.class);
  }

  public List<Member> basicSelect() {
    return select(member)
      .from(member)
      .fetch();
  }

  public List<Member> basicSelectFrom() {
    return selectFrom(member)
      .fetch();
  }

  public Page<Member> searchPageByApplyPage(MemberSearchCondition condition, Pageable pageable) {
    final JPAQuery<Member> query = selectFrom(member)
      .from(member)
      .where(
        usernameEq(condition.getUsername()),
        teamNameEq(condition.getTeamName()),
        ageGoe(condition.getAgeGoe()),
        ageLoe(condition.getAgeLoe()));

    final List<Member> content = getQuerydsl().applyPagination(pageable, query)
      .fetch();

    return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
  }

  public Page<Member> applyPagination(MemberSearchCondition condition, Pageable pageable) {
    return applyPagination(pageable, query -> query
      .selectFrom(member)
      .leftJoin(member.team, team).fetchJoin()
      .where(
        usernameEq(condition.getUsername()),
        teamNameEq(condition.getTeamName()),
        ageGoe(condition.getAgeGoe()),
        ageLoe(condition.getAgeLoe()))
    );
  }

  public Page<Member> applyPagination2(MemberSearchCondition condition,
    Pageable pageable) {
    return applyPagination(pageable, contentQuery -> contentQuery
        .selectFrom(member)
        .leftJoin(member.team, team).fetchJoin()
        .where(usernameEq(condition.getUsername()),
          teamNameEq(condition.getTeamName()),
          ageGoe(condition.getAgeGoe()),
          ageLoe(condition.getAgeLoe())),
      countQuery -> countQuery
        .selectFrom(member)
        .leftJoin(member.team, team)
        .where(usernameEq(condition.getUsername()),
          teamNameEq(condition.getTeamName()),
          ageGoe(condition.getAgeGoe()),
          ageLoe(condition.getAgeLoe()))
    );
  }

  private BooleanExpression usernameEq(String username) {
    return hasText(username) ? member.username.eq(username) : null;
  }

  private BooleanExpression teamNameEq(String teamName) {
    return hasText(teamName) ? team.name.eq(teamName) : null;
  }

  private BooleanExpression ageGoe(Integer ageGoe) {
    return ageGoe != null ? member.age.goe(ageGoe) : null;
  }

  private BooleanExpression ageLoe(Integer ageLoe) {
    return ageLoe != null ? member.age.loe(ageLoe) : null;
  }
}
