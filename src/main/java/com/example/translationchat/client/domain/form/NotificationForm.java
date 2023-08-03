package com.example.translationchat.client.domain.form;

import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.type.ContentType;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationForm {
    @NotNull
    private User user;
    private Long args;
    @NotNull
    private ContentType contentType;
}
