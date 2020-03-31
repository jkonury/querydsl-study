package com.github.querydslstudy.controller;

import com.github.querydslstudy.dto.MemberSearchCondition;
import com.github.querydslstudy.dto.MemberTeamDto;
import com.github.querydslstudy.repository.MemberJpaRepository;
import com.github.querydslstudy.repository.MemberQuerydslSupportRepository;
import com.github.querydslstudy.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

  private final MemberJpaRepository memberJpaRepository;
  private final MemberRepository memberRepository;
  private final MemberQuerydslSupportRepository memberQuerydslSupportRepository;

  @GetMapping("/v1/members")
  public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
    return memberJpaRepository.search(condition);
  }

  @GetMapping("/v2/members")
  public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
    return memberRepository.searchSimple(condition, pageable);
  }

  @GetMapping("/v3/members")
  public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
    return memberRepository.searchComplex(condition, pageable);
  }

  @GetMapping("/v4/members")
  public Page<MemberTeamDto> searchMemberV4(MemberSearchCondition condition, Pageable pageable) {
    return memberQuerydslSupportRepository.applyPagination(condition, pageable)
      .map(member -> new MemberTeamDto(member.getId(), member.getUsername(), member.getAge(), member.getTeam().getId(),
        member.getTeam().getName()));
  }

  @GetMapping("/v5/members")
  public Page<MemberTeamDto> searchMemberV5(MemberSearchCondition condition, Pageable pageable) {
    return memberQuerydslSupportRepository.applyPagination2(condition, pageable)
      .map(member -> new MemberTeamDto(member.getId(), member.getUsername(), member.getAge(), member.getTeam().getId(),
        member.getTeam().getName()));
  }
}
