package com.example.translationchat.domain.notification.form;

import com.example.translationchat.domain.type.ContentType;
import com.example.translationchat.domain.user.entity.User;
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
    private Long sendUserId;
    private String sendUserName;
    @NotNull
    private ContentType contentType;

    public static NotificationForm of(User user, User sendUser, ContentType contentType) {
        return NotificationForm.builder()
            .user(user)
            .sendUserId(sendUser == null ? null : sendUser.getId())
            .sendUserName(sendUser == null ? null : sendUser.getName())
            .contentType(contentType)
            .build();
    }
}
