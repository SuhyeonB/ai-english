package com.example.ai_english.domain.auth.service;

import com.example.ai_english.domain.auth.dto.CustomOAuth2User;
import com.example.ai_english.domain.auth.entity.OAuthAccount;
import com.example.ai_english.domain.auth.repository.OAuthAccountRepository;
import com.example.ai_english.domain.user.entity.Role;
import com.example.ai_english.domain.user.entity.User;
import com.example.ai_english.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String providerId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String nickname = (String) attributes.get("name");

        User user = oAuthAccountRepository.findByProviderAndProviderId(provider, providerId)
                .map(OAuthAccount::getUser)
                .orElseGet(() -> registerNewUser(email, nickname, provider, providerId));

        return new CustomOAuth2User(user, attributes);
    }

    private User registerNewUser(String email, String nickname, String provider, String providerId) {
        // If a user with this email already exists (e.g. regular login), link OAuth to that account
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .nickname(nickname)
                                .role(Role.USER)
                                .build()
                        )
                );

        oAuthAccountRepository.save(OAuthAccount.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .build());

        return user;
    }
}
