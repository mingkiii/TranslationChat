package com.example.translationchat.common.papago;

import com.example.translationchat.client.domain.type.Language;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PapagoService {
    @Value("${papa go.client-id}")
    private String clientId;
    @Value("${papa go.client-secret}")
    private String clientSecret;

    public String getTransSentence(String message, Language language, Language transLanguage) {
        String text;
        text = URLEncoder.encode(message, StandardCharsets.UTF_8);

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);

        String API_URL = "https://openapi.naver.com/v1/papago/n2mt";
        String responseBody = post(API_URL, requestHeaders, text, language, transLanguage);

        return convertToData(responseBody);
    }

    private String post(String apiUrl, Map<String, String> requestHeaders, String text, Language language, Language transLanguage){
        HttpURLConnection con = connect(apiUrl);
        String postParams = String.format("source=%s&target=%s&text=", language, transLanguage) + text; //원본언어 -> 목적언어
        try {
            con.setRequestMethod("POST");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postParams.getBytes());
                wr.flush();
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 응답
                return readBody(con.getInputStream());
            } else {  // 에러 응답
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    private HttpURLConnection connect(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private static String readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }

    private static String convertToData(String responseBody) {
        Gson gson = new Gson();
        JsonResponse response = gson.fromJson(responseBody, JsonResponse.class);

        return response.getMessage().getResult().getTranslatedText();
    }

    private static class JsonResponse {
        private Message message;

        public Message getMessage() {
            return message;
        }
    }

    private static class Message {
        private Result result;

        public Result getResult() {
            return result;
        }
    }

    private static class Result {
        private String translatedText;

        public String getTranslatedText() {
            return translatedText;
        }
    }
}
