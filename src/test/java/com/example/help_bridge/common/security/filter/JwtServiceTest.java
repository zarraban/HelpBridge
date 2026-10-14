package com.example.help_bridge.common.security.filter;

import com.example.help_bridge.common.security.service.JwtService;
import com.example.help_bridge.common.security.service.JwtValidatorService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-for-unit-tests-0123456789-abcdef";
    private static final String OTHER_SECRET = "another-secret-key-for-unit-tests-9876543210-zyxw";

    private final JwtService jwtService = new JwtService(SECRET, 15);
    private final JwtValidatorService validator = new JwtValidatorService(SECRET);

    private final UserDetails admin = User.withUsername("admin")
            .password("irrelevant")
            .roles("ADMIN")
            .build();

    @Test
    void generatedTokenIsValidForSameUser() {
        String token = jwtService.generateToken(admin);

        assertThat(validator.extractUsername(token)).isEqualTo("admin");
        assertThat(validator.isTokenValid(token, admin)).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void tokenContainsRolesClaim() {
        String token = jwtService.generateToken(admin);

        List<String> roles = validator.extractAllClaims(token).get("roles", List.class);

        assertThat(roles).containsExactly("ROLE_ADMIN");
    }

    @Test
    void tokenIsNotValidForDifferentUser() {
        String token = jwtService.generateToken(admin);
        UserDetails other = User.withUsername("other").password("x").roles("USER").build();

        assertThat(validator.isTokenValid(token, other)).isFalse();
    }

    @Test
    void tokenSignedWithOtherSecretIsRejected() {
        String forged = new JwtService(OTHER_SECRET, 15).generateToken(admin);

        assertThatThrownBy(() -> validator.extractUsername(forged))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        String expired = new JwtService(SECRET, -1).generateToken(admin);

        assertThatThrownBy(() -> validator.extractUsername(expired))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void malformedTokenIsRejected() {
        assertThatThrownBy(() -> validator.extractUsername("not-a-jwt"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void tooShortSecretIsRejectedOnStartup() {
        assertThatThrownBy(() -> new JwtService("short", 15))
                .isInstanceOf(WeakKeyException.class);
    }

    @Test
    void expirationIsConvertedToMillis() {
        assertThat(jwtService.getExpirationMillis()).isEqualTo(15 * 60 * 1000L);
    }
}