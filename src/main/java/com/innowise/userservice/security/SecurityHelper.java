package com.innowise.userservice.security;

import com.innowise.userservice.domain.dao.PaymentCardRepository;
import com.innowise.userservice.domain.dao.UserRepository;
import com.innowise.userservice.domain.entity.PaymentCard;
import com.innowise.userservice.domain.entity.User;
import com.innowise.userservice.domain.exeption.CardNotFoundException;
import com.innowise.userservice.domain.exeption.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

@Component("securityHelper")
@RequiredArgsConstructor
public class SecurityHelper {

    private final UserRepository userRepository;
    private final PaymentCardRepository paymentCardRepository;

    public boolean isOwner(Long id) {
        UUID userIdFromToken = getUserIdFromToken();
        if (userIdFromToken == null) {
            return false;
        }

        User userFromDb = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userFromDb.getUserId() != null && userFromDb.getUserId().equals(userIdFromToken);
    }

    public boolean isEmailOwner(String email) {
        UUID userIdFromToken = getUserIdFromToken();
        if (userIdFromToken == null) {
            return false;
        }

        User userFromDb = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        return userFromDb.getUserId() != null && userFromDb.getUserId().equals(userIdFromToken);
    }

    public boolean isCardOwner(Long cardId) {
        UUID userIdFromToken = getUserIdFromToken();
        if (userIdFromToken == null) return false;

        PaymentCard card = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        User owner = card.getUser();
        if (owner == null || owner.getUserId() == null) return false;

        return owner.getUserId().equals(userIdFromToken);
    }

    private UUID getUserIdFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken token) {
            String sub = token.getToken().getSubject();
            if (sub != null) {
                return UUID.fromString(sub);
            }
        }
        return null;
    }
}
