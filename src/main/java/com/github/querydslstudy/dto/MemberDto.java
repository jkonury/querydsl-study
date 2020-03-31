package com.github.querydslstudy.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MemberDto {

  private String username;
  private int age;

  public MemberDto(String username, int age) {
    this.username = username;
    this.age = age;
  }
}
