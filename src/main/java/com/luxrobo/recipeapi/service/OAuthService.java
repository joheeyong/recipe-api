package com.luxrobo.recipeapi.service;

import com.luxrobo.recipeapi.entity.User;
import com.luxrobo.recipeapi.repository.UserRepository;
import com.luxrobo.recipeapi.security.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class OAuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final WebClient webClient = WebClient.create();

    @Value("${google.client-id}")
    private String googleClientId;
    @Value("${google.client-secret}")
    private String googleClientSecret;
    @Value("${naver.login.client-id}")
    private String naverClientId;
    @Value("${naver.login.client-secret}")
    private String naverClientSecret;
    @Value("${kakao.client-id}")
    private String kakaoClientId;
    @Value("${kakao.client-secret:}")
    private String kakaoClientSecret;

    private static final Set<String> ALLOWED_REDIRECTS = Set.of(
        "http://localhost:3000",
        "https://recipe-web.vercel.app",
        "https://recipe-web-rosy.vercel.app"
    );

    public OAuthService(UserRepository userRepository, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }

    public Map<String, Object> googleLogin(String code, String redirectUri) {
        validateRedirectUri(redirectUri);

        Map<String, String> tokenBody = Map.of(
            "code", code,
            "client_id", googleClientId,
            "client_secret", googleClientSecret,
            "redirect_uri", redirectUri,
            "grant_type", "authorization_code"
        );

        Map<String, Object> tokenResponse = webClient.post()
            .uri("https://oauth2.googleapis.com/token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(tokenBody)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        String accessToken = (String) tokenResponse.get("access_token");

        Map<String, Object> userInfo = webClient.get()
            .uri("https://www.googleapis.com/oauth2/v2/userinfo")
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        return upsertUser("google",
            String.valueOf(userInfo.get("id")),
            (String) userInfo.get("name"),
            (String) userInfo.get("email"),
            (String) userInfo.get("picture"));
    }

    public Map<String, Object> naverLogin(String code, String state, String redirectUri) {
        validateRedirectUri(redirectUri);

        String tokenUrl = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code"
            + "&client_id=" + naverClientId
            + "&client_secret=" + naverClientSecret
            + "&code=" + code
            + "&state=" + state;

        Map<String, Object> tokenResponse = webClient.get()
            .uri(tokenUrl)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        String accessToken = (String) tokenResponse.get("access_token");

        Map<String, Object> apiResponse = webClient.get()
            .uri("https://openapi.naver.com/v1/nid/me")
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        Map<String, Object> userInfo = (Map<String, Object>) apiResponse.get("response");

        return upsertUser("naver",
            (String) userInfo.get("id"),
            (String) userInfo.get("name"),
            (String) userInfo.get("email"),
            (String) userInfo.get("profile_image"));
    }

    public Map<String, Object> kakaoLogin(String code, String redirectUri) {
        validateRedirectUri(redirectUri);

        String tokenBody = "grant_type=authorization_code"
            + "&client_id=" + kakaoClientId
            + "&redirect_uri=" + redirectUri
            + "&code=" + code;

        if (kakaoClientSecret != null && !kakaoClientSecret.isEmpty()) {
            tokenBody += "&client_secret=" + kakaoClientSecret;
        }

        Map<String, Object> tokenResponse = webClient.post()
            .uri("https://kauth.kakao.com/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(tokenBody)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        String accessToken = (String) tokenResponse.get("access_token");

        Map<String, Object> userInfo = webClient.get()
            .uri("https://kapi.kakao.com/v2/user/me")
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return upsertUser("kakao",
            String.valueOf(userInfo.get("id")),
            (String) profile.get("nickname"),
            (String) kakaoAccount.get("email"),
            (String) profile.get("profile_image_url"));
    }

    private Map<String, Object> upsertUser(String provider, String providerId,
                                            String name, String email, String profileImage) {
        User user = userRepository.findByProviderAndProviderId(provider, providerId)
            .orElseGet(() -> new User(name, email, provider, providerId, profileImage));

        user.setName(name);
        user.setEmail(email);
        user.setProfileImage(profileImage);
        userRepository.save(user);

        String token = jwtProvider.generateToken(user.getId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", token);
        response.put("user", Map.of(
            "id", user.getId(),
            "name", user.getName() != null ? user.getName() : "",
            "email", user.getEmail() != null ? user.getEmail() : "",
            "profileImage", user.getProfileImage() != null ? user.getProfileImage() : "",
            "provider", user.getProvider()
        ));
        return response;
    }

    private void validateRedirectUri(String redirectUri) {
        boolean valid = ALLOWED_REDIRECTS.stream()
            .anyMatch(allowed -> redirectUri != null && redirectUri.startsWith(allowed));
        if (!valid) {
            throw new IllegalArgumentException("Invalid redirect URI");
        }
    }
}
