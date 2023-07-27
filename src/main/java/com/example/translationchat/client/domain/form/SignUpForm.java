package com.example.translationchat.client.domain.form;

import com.example.translationchat.client.domain.type.Language;
import com.example.translationchat.client.domain.type.Nationality;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpForm {
    @NotBlank(message = "필수 입력")
    @Pattern(regexp = "^.+@.+\\..+$", message = "이메일 형식에 맞게 입력해 주세요.")
    private String email;

    @Pattern(regexp = "^[^\\s]+$", message = "이름에는 공백이 들어갈 수 없습니다.")
    @NotBlank(message = "필수 입력")
    @Size(min = 2, max = 8, message = "이름은 2자 이상 8자 이하로 입력해 주세요.")
    private String name;

    @NotBlank(message = "필수 입력")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}",
        message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요. 특수문자는 반드시 포함 해주세요.")
    private String password;

    @NotNull(message = "필수 입력")
    private Nationality nationality;

    @NotNull(message = "필수 입력")
    private Language language;
}
