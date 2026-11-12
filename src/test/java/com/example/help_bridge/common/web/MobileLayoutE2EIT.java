package com.example.help_bridge.common.web;

import com.example.help_bridge.fundraising.request.web.RequestWebController;
import com.example.help_bridge.fundraising.request.service.RequestService;
import com.example.help_bridge.fundraising.request.service.RequestVerificationService;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.example.help_bridge.common.config.SecurityConfiguration;
import com.example.help_bridge.common.security.service.JwtService;
import com.example.help_bridge.common.security.service.JwtValidatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({LoginController.class, RequestWebController.class})
@Import(SecurityConfiguration.class)
class MobileLayoutE2EIT {
    @Autowired MockMvc mvc;
    @MockitoBean RequestService requestService;
    @MockitoBean RequestVerificationService verificationService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean AuthenticationManager authenticationManager;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean JwtService jwtService;
    @MockitoBean JwtValidatorService jwtValidatorService;
    @MockitoBean AuthenticationEntryPoint entryPoint;
    @MockitoBean AccessDeniedHandler deniedHandler;

    @Test
    void pagesFitPhoneAndSupportKeyboard() throws Exception {
        String login = mvc.perform(get("/login")).andReturn().getResponse().getContentAsString();
        var request = new com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse(
                1L, 1L, null, "MEDICAL", java.math.BigDecimal.TEN, java.time.LocalDate.now(),
                "Ситуація", "ДовгийОпис".repeat(60), "Установа", "DOC-1",
                com.example.help_bridge.fundraising.request.entity.RequestStatus.NEW, true,
                java.time.LocalDateTime.now(), java.util.List.of());
        when(requestService.getSharedPool()).thenReturn(java.util.List.of(request));
        when(requestService.getAssistanceTypes()).thenReturn(java.util.Map.of("MEDICAL", "Лікування"));
        String list = mvc.perform(get("/requests").with(user("a".repeat(150) + "@test.com").roles("FUND_REPRESENTATIVE")))
                .andReturn().getResponse().getContentAsString();
        String form = mvc.perform(get("/requests/new").with(user("applicant").roles("APPLICANT")))
                .andReturn().getResponse().getContentAsString();
        String css = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/resources/static/css/app.css"));
        String js = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/resources/static/js/forms.js"));
        try (var playwright = com.microsoft.playwright.Playwright.create();
             var browser = playwright.chromium().launch()) {
            var page = browser.newPage();
            page.route("https://helpbridge.test/**", route -> {
                String path = java.net.URI.create(route.request().url()).getPath();
                String content = switch (path) {
                    case "/css/app.css" -> css;
                    case "/js/forms.js" -> js;
                    case "/requests" -> list;
                    case "/requests/new" -> form;
                    default -> login;
                };
                String type = path.endsWith(".css") ? "text/css" : path.endsWith(".js") ? "application/javascript" : "text/html";
                route.fulfill(new com.microsoft.playwright.Route.FulfillOptions().setContentType(type).setBody(content));
            });
            for (int width : new int[]{320, 390, 768, 1280}) {
                page.setViewportSize(width, 900);
                for (String path : new String[]{"/login", "/requests", "/requests/new"}) {
                    page.navigate("https://helpbridge.test" + path);
                    assertThat((Boolean) page.evaluate("document.documentElement.scrollWidth <= innerWidth")).as(path + " at " + width).isTrue();
                }
            }
            page.setViewportSize(320, 900);
            page.navigate("https://helpbridge.test/requests");
            page.locator(".skip-link").focus();
            page.keyboard().press("Enter");
            assertThat(page.locator("#main-content").evaluate("el => el === document.activeElement")).isEqualTo(true);
            page.locator(".table-container").focus();
            page.keyboard().press("ArrowRight");
            page.waitForCondition(() -> ((Number) page.locator(".table-container").evaluate("el => el.scrollLeft")).intValue() > 0);
            page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(java.nio.file.Path.of("target/playwright/mobile-requests.png")).setFullPage(true));
            page.navigate("https://helpbridge.test/login");
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator("[data-submit-status]")).isHidden();
            page.locator("#email").focus();
            page.keyboard().press("Tab");
            assertThat(page.locator("#password").evaluate("el => el === document.activeElement")).isEqualTo(true);
            page.keyboard().press("Tab");
            assertThat(page.locator("#login-form button").evaluate("el => el === document.activeElement")).isEqualTo(true);
            page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(java.nio.file.Path.of("target/playwright/mobile-login.png")).setFullPage(true));
        }
    }
}
