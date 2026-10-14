package com.example.help_bridge.common.security.filter;

import com.example.help_bridge.common.security.service.JwtService;
import com.example.help_bridge.common.security.service.JwtValidatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtLoginFilterTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-0123456789";

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthenticationEntryPoint authenticationEntryPoint;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private JwtService jwtService;
    private JwtLoginFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 15);
        filter = new JwtLoginFilter(authenticationManager, jwtService, objectMapper, authenticationEntryPoint);
    }

    private MockHttpServletRequest post(String path, String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setServletPath(path);
        request.setContentType("application/json");
        request.setContent(body.getBytes());
        return request;
    }

    private static final String VALID_BODY = """
            {"accountType":"VOLUNTEER","email":"anna@gmail.com","password":"Secret123!"}
            """;

    @Test
    void returnsTokenWhenCredentialsAreValid() throws Exception {
        UserDetails user = User.withUsername("VOLUNTEER:anna@gmail.com").password("hash").roles("VOLUNTEER").build();
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(post("/api/auth/login", VALID_BODY), response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
        var json = objectMapper.readTree(response.getContentAsString());
        assertThat(json.get("tokenType").asString()).isEqualTo("Bearer");
        assertThat(json.get("expiresIn").asLong()).isEqualTo(900);
        String token = json.get("accessToken").asString();
        assertThat(new JwtValidatorService(SECRET).extractUsername(token)).isEqualTo("VOLUNTEER:anna@gmail.com");
    }

    @Test
    void passesAccountTypeAndEmailAsUsernameToTheAuthenticationManager() throws Exception {
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new BadCredentialsException("x"));

        filter.doFilter(post("/api/auth/login", VALID_BODY), new MockHttpServletResponse(), new MockFilterChain());

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("VOLUNTEER:anna@gmail.com");
        assertThat(captor.getValue().getCredentials()).isEqualTo("Secret123!");
    }

    @Test
    void delegatesToEntryPointWhenCredentialsAreInvalid() throws Exception {
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new BadCredentialsException("x"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(post("/api/auth/login", VALID_BODY), response, new MockFilterChain());

        verify(authenticationEntryPoint).commence(any(), any(), isA(BadCredentialsException.class));
        assertThat(response.getContentAsString()).doesNotContain("accessToken");
    }

    @Test
    void delegatesToEntryPointWhenAccountIsDisabled() throws Exception {
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new DisabledException("x"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(post("/api/auth/login", VALID_BODY), response, new MockFilterChain());

        verify(authenticationEntryPoint).commence(any(), any(), isA(DisabledException.class));
    }

    @Test
    void delegatesToEntryPointWhenBodyIsMalformedJson() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(post("/api/auth/login", "{ \"accountType\": "), response, new MockFilterChain());

        verify(authenticationEntryPoint).commence(any(), any(), isA(BadCredentialsException.class));
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void delegatesToEntryPointWhenAccountTypeIsUnknown() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String body = "{\"accountType\":\"WIZARD\",\"email\":\"a@b.com\",\"password\":\"x\"}";

        filter.doFilter(post("/api/auth/login", body), response, new MockFilterChain());

        verify(authenticationEntryPoint).commence(any(), any(), isA(BadCredentialsException.class));
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void delegatesToEntryPointWhenFieldsAreBlank() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String body = "{\"accountType\":\"VOLUNTEER\",\"email\":\" \",\"password\":\"\"}";

        filter.doFilter(post("/api/auth/login", body), response, new MockFilterChain());

        verify(authenticationEntryPoint).commence(any(), any(), isA(BadCredentialsException.class));
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void ignoresRequestsToOtherPaths() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(post("/api/funds", VALID_BODY), response, chain);

        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(authenticationManager, authenticationEntryPoint);
    }

    @Test
    void ignoresGetRequestsToTheLoginPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/login");
        request.setServletPath("/api/auth/login");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(authenticationManager, authenticationEntryPoint);
    }
}
