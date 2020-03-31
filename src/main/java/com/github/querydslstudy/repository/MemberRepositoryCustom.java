package com.github.querydslstudy.repository;

import com.github.querydslstudy.dto.MemberSearchCondition;
import com.github.querydslstudy.dto.MemberTeamDto;
import java.util.List;

public interface MemberRepositoryCustom {

  List<MemberTeamDto> search(MemberSearchCondition condition);
}
