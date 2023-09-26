package com.example.translationchat.domain.notification.form;

import com.example.translationchat.domain.user.entity.User;
import com.example.translationchat.domain.type.ContentType;
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
    private Long roomId;
    @NotNull
    private ContentType contentType;
}
