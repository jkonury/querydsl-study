package com.github.querydslstudy.repository;

import com.github.querydslstudy.dto.MemberSearchCondition;
import com.github.querydslstudy.dto.MemberTeamDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

  List<MemberTeamDto> search(MemberSearchCondition condition);
  Page<MemberTeamDto> searchSimple(MemberSearchCondition condition, Pageable pageable);
  Page<MemberTeamDto> searchComplex(MemberSearchCondition condition, Pageable pageable);
}
