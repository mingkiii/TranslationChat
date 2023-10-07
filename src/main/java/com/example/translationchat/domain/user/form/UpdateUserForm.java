package com.example.translationchat.domain.user.form;

import com.example.translationchat.domain.type.Language;
import com.example.translationchat.domain.type.Nationality;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserForm {

    private Nationality nationality;
    private Language language;

    public void setNationality(String nationality) {
        this.nationality = (nationality != null && !nationality.isEmpty()) ? Nationality.valueOf(nationality) : null;
    }

    public void setLanguage(String language) {
        this.language = (language != null && !language.isEmpty()) ? Language.valueOf(language) : null;
    }
}
