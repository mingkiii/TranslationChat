package com.example.translationchat.chat.domain.request;

import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RandomChatMessageRequest {
    @NotEmpty
    @Length(max = 100)
    private String content;
}
