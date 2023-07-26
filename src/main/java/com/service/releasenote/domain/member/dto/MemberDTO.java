package com.service.releasenote.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberLoginType;
import com.service.releasenote.domain.member.model.MemberProject;
import com.service.releasenote.domain.project.dto.ProjectDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

public class MemberDTO {
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SignUpRequest {
        @NotBlank(message = "이름을 입력해주세요.")
        private String username;

        @NotBlank(message = "이메일을 입력해주세요.")
        @Pattern(regexp = "^\\s*$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "이메일 형식이 유효하지 않습니다.")
        private String email;

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // API 사용자가 이 객체를 통해 패스워드 값을 직접 변경할 수 없게함.
        @NotBlank(message = "비밀번호를 입력해주세요.")
        // 대문자 혹은 소문자 영어 1개 이상, 특수문자 1개 이상, 길이 8 이상
        @Pattern(regexp = "^\\s*$|^(?=.*[a-zA-Z])(?=.*[\\W])(?=.*[0-9]).{8,}$", message = "비밀번호가 조건에 부합하지 않습니다.")
        private String password;

        @Enumerated(value = EnumType.STRING)
        private MemberLoginType memberLoginType;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginDTO {
        @NotBlank(message = "이메일을 입력해주세요.")
        @Pattern(regexp = "^\\s*$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "이메일 형식이 유효하지 않습니다.")
        private String email;

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // API 사용자가 이 객체를 통해 패스워드 값을 직접 변경할 수 없게함.
        @NotBlank(message = "비밀번호를 입력해주세요.")
        // 대문자 혹은 소문자 영어 1개 이상, 특수문자 1개 이상, 길이 8 이상
        @Pattern(regexp = "^\\s*$|^(?=.*[a-zA-Z])(?=.*[\\W])(?=.*[0-9]).{8,}$", message = "비밀번호가 조건에 부합하지 않습니다.")
        private String password;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WithDrawalDTO{
        private String inputPassword;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdatePasswordRequest {
        @NotBlank(message = "비밀번호를 입력해주세요.")
        // 대문자 혹은 소문자 영어 1개 이상, 특수문자 1개 이상, 길이 8 이상
        @Pattern(regexp = "^\\s*$|^(?=.*[a-zA-Z])(?=.*[\\W])(?=.*[0-9]).{8,}$", message = "비밀번호가 조건에 부합하지 않습니다.")
        private String inputNewPassword;

        private String inputOldPassword;

        private String inputEmail;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberListDTO {

        private Long id;
        private String name;
        private String email;

        public void setId(Long id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public MemberDTO.MemberListDTO toResponseDto(Member member) {
            return MemberListDTO.builder()
                    .id(member.getId())
                    .name(member.getUserName())
                    .email(member.getEmail())
                    .build();
        }
    }

}
