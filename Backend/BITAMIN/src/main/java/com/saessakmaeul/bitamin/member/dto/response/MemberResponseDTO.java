package com.saessakmaeul.bitamin.member.dto.response;

import com.saessakmaeul.bitamin.member.entity.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
public class MemberResponseDTO {
    private long id;
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String dongCode;
    private Date birthday;
    private String profileKey;
    private String profileUrl;
    private Role role;
}