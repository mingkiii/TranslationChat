package com.example.translationchat.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.translationchat.client.domain.type.Language;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "papa go.client-id=${CLIENT_ID}",
    "papa go.client-secret=${CLIENT_SECRET}"
})
class PapagoUtilTest {

    @Autowired
    private PapagoUtil papagoUtil;

    @Test
    public void testGetTransSentence(){
        String message = "안녕하세요";
        Language language = Language.ko;
        Language transLanguage = Language.en;

        String translatedText = papagoUtil.getTransSentence(message, language, transLanguage);

        // You can add assertions to verify the translated text
        assertEquals("Hello", translatedText);
    }
}