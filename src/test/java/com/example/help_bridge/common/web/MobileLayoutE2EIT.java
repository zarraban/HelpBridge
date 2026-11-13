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

@WebMvcTest({com.example.help_bridge.fundraising.fundraiser.web.VolunteerEvidenceWebController.class,com.example.help_bridge.fundraising.fundraiser.web.VolunteerAssignmentActionWebController.class,com.example.help_bridge.fundraising.fundraiser.web.VolunteerWorkspaceWebController.class,com.example.help_bridge.fundraising.fundraiser.web.FundraiserWebController.class,com.example.help_bridge.fundraising.request.web.RequestBookingWebController.class, LoginController.class, RequestWebController.class, com.example.help_bridge.fundraising.request.web.RequestDocumentWebController.class})
@Import(SecurityConfiguration.class)
class MobileLayoutE2EIT {
    @MockitoBean com.example.help_bridge.fundraising.request.security.RequestAccessPolicy access;
    @MockitoBean com.example.help_bridge.fundraising.request.service.RequestDocumentService documentService;
    @MockitoBean com.example.help_bridge.fundraising.fundraiser.service.VolunteerWorkspaceService workspace;
    @MockitoBean com.example.help_bridge.fundraising.fundraiser.service.EvidenceService evidenceService;
    @Autowired MockMvc mvc;
    @MockitoBean com.example.help_bridge.fundraising.fundraiser.service.FundraiserAssignmentService assignments;
    @MockitoBean com.example.help_bridge.users.volunteer.service.VolunteerService volunteers;
    @MockitoBean RequestService requestService;
    @MockitoBean com.example.help_bridge.fundraising.request.service.RequestBookingService bookingService;
    @MockitoBean com.example.help_bridge.fundraising.fund.repository.FundRepository funds;
    @MockitoBean RequestVerificationService verificationService;
    @MockitoBean com.example.help_bridge.fundraising.fundraiser.service.FundraiserService fundraisers;
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
                java.time.LocalDateTime.now(), java.util.List.of(new com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.DocumentResponse(
                        1L, "ДовгаНазваДокумента".repeat(12), "https://files.test/proof.pdf")));
        when(requestService.getSharedPool()).thenReturn(java.util.List.of(request));
        when(requestService.getAssistanceTypes()).thenReturn(java.util.Map.of("MEDICAL", "Лікування", "OTHER", "Інше"));
        String list = mvc.perform(get("/requests").with(user("a".repeat(150) + "@test.com").roles("FUND_REPRESENTATIVE")))
                .andReturn().getResponse().getContentAsString();
        String form = mvc.perform(get("/requests/new").with(user("applicant").roles("APPLICANT")))
                .andReturn().getResponse().getContentAsString();
        when(access.currentViewer()).thenReturn(new com.example.help_bridge.fundraising.request.security.RequestAccessPolicy.Viewer("FUND_REPRESENTATIVE", null, 1L, com.example.help_bridge.fundraising.fund.entity.FundStatus.APPROVED));
        when(requestService.getRequestById(1L)).thenReturn(request);
        String details = mvc.perform(get("/requests/1").with(user("representative").roles("FUND_REPRESENTATIVE")))
                .andReturn().getResponse().getContentAsString();
        var fund = new com.example.help_bridge.fundraising.fund.entity.Fund();
        fund.setFundName("Фонд допомоги");
        when(funds.findById(1L)).thenReturn(java.util.Optional.of(fund));
        String bookingConfirmation = mvc.perform(get("/requests/1/book").with(user("representative").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var pending = new com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse(
                1L, 1L, null, "MEDICAL", java.math.BigDecimal.TEN, java.time.LocalDate.now().plusDays(10),
                "Ситуація", "Потрібна допомога", "Установа", "DOC-1",
                com.example.help_bridge.fundraising.request.entity.RequestStatus.PENDING_VERIFICATION, false,
                java.time.LocalDateTime.now(), java.util.List.of());
        when(access.currentViewer()).thenReturn(new com.example.help_bridge.fundraising.request.security.RequestAccessPolicy.Viewer("APPLICANT", 1L, null));
        when(requestService.getRequestById(1L)).thenReturn(pending);
        String edit = mvc.perform(get("/requests/1/edit").with(user("applicant").roles("APPLICANT")))
                .andReturn().getResponse().getContentAsString();
        String deletion = mvc.perform(get("/requests/1/delete").with(user("applicant").roles("APPLICANT")))
                .andReturn().getResponse().getContentAsString();
        var proof = new com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.DocumentResponse(
                1L, "Медичний висновок", "https://files.test/proof.pdf");
        when(documentService.getDocument(1L, 1L)).thenReturn(proof);
        String documentNew = mvc.perform(get("/requests/1/documents/new").with(user("applicant").roles("APPLICANT")))
                .andReturn().getResponse().getContentAsString();
        String documentEdit = mvc.perform(get("/requests/1/documents/1/edit").with(user("applicant").roles("APPLICANT")))
                .andReturn().getResponse().getContentAsString();
        String documentDelete = mvc.perform(get("/requests/1/documents/1/delete").with(user("applicant").roles("APPLICANT")))
                .andReturn().getResponse().getContentAsString();
        when(access.currentViewer()).thenReturn(new com.example.help_bridge.fundraising.request.security.RequestAccessPolicy.Viewer("SYSTEM_ADMIN", null, null));
        String reviewDetails = mvc.perform(get("/requests/1").with(user("admin").roles("SYSTEM_ADMIN")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String rejectConfirmation = mvc.perform(get("/requests/1/reject").with(user("admin").roles("SYSTEM_ADMIN")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var evidence = new com.example.help_bridge.fundraising.fundraiser.dto.response.EvidenceResponse(
                1L, 1L, "Чек".repeat(80), "Відгук".repeat(150), "https://files.test/proof.pdf", java.time.LocalDateTime.now());
        var fundraiser = new com.example.help_bridge.fundraising.fundraiser.dto.response.FundraiserResponse(
                1L, 1L, com.example.help_bridge.fundraising.fundraiser.entity.FundraiserStatus.IN_PROGRESS,
                java.time.LocalDateTime.now(), null, java.util.List.of(evidence));
        when(fundraisers.getFundraiserById(1L)).thenReturn(fundraiser);
        when(fundraisers.getFundraisers("createdAt", "asc", 1L, 5L)).thenReturn(
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(fundraiser),
                        org.springframework.data.domain.PageRequest.of(0, 5), 15));
        String fundraiserList = mvc.perform(get("/fundraisers").with(user("representative").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String fundraiserDetails = mvc.perform(get("/fundraisers/1").with(user("representative").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String closeConfirmation = mvc.perform(get("/fundraisers/1/close").with(user("representative").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        when(fundraisers.getFundraisers("createdAt", "asc", 1L, 5L)).thenReturn(org.springframework.data.domain.Page.empty());
        String emptyFundraisers = mvc.perform(get("/fundraisers").with(user("representative").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String cabinet = mvc.perform(get("/home").with(user("representative").roles("FUND_REPRESENTATIVE")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var ownAssignment = new com.example.help_bridge.fundraising.fundraiser.dto.response.FundraiserAssignmentResponse(
                1L, 5L, com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus.ACTIVE,
                java.time.LocalDateTime.now(), null, null, 1L, "Збір №1", fundraiser.status(), null);
        when(workspace.getMyAssignments()).thenReturn(java.util.List.of(ownAssignment));
        String ownAssignments = mvc.perform(get("/my/assignments").with(user("volunteer@test.org").roles("VOLUNTEER")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        when(workspace.getMyAssignments()).thenReturn(java.util.List.of());
        String emptyOwnAssignments = mvc.perform(get("/my/assignments").with(user("volunteer@test.org").roles("VOLUNTEER")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        when(workspace.getMyFundraiser(1L)).thenReturn(new com.example.help_bridge.fundraising.fundraiser.dto.response.VolunteerFundraiserResponse(
                fundraiser, new com.example.help_bridge.fundraising.fundraiser.dto.response.VolunteerFundraiserResponse.RequestSummary(
                        "Лікування", java.math.BigDecimal.TEN, java.time.LocalDate.now(), "Установа", "ДовгийОпис".repeat(100)), java.util.List.of(ownAssignment)));
        String ownDetails = mvc.perform(get("/my/fundraisers/1").with(user("volunteer@test.org").roles("VOLUNTEER")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        when(workspace.getMyAssignment(1L)).thenReturn(ownAssignment);
        String completeAssignment = mvc.perform(get("/my/assignments/1/complete").with(user("volunteer@test.org").roles("VOLUNTEER")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String returnAssignment = mvc.perform(get("/my/assignments/1/return").with(user("volunteer@test.org").roles("VOLUNTEER")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String evidenceForm = mvc.perform(get("/my/fundraisers/1/evidences/new").with(user("volunteer@test.org").roles("VOLUNTEER")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String css = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/resources/static/css/app.css"));
        String js = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/resources/static/js/forms.js"));
        try (var playwright = com.microsoft.playwright.Playwright.create();
             var browser = playwright.chromium().launch()) {
            var page = browser.newPage();
            var closingSubmissions = new java.util.concurrent.atomic.AtomicInteger();
            var bookingSubmissions = new java.util.concurrent.atomic.AtomicInteger();
            page.route("https://helpbridge.test/**", route -> {
                String path = java.net.URI.create(route.request().url()).getPath();
                if (path.equals("/requests/1/book") && route.request().method().equals("POST")) {
                    bookingSubmissions.incrementAndGet();
                }
                if (path.equals("/fundraisers/1/close") && route.request().method().equals("POST")) {
                    closingSubmissions.incrementAndGet();
                }
                String content = switch (path) {
                    case "/my/fundraisers/1/evidences/new" -> evidenceForm;
                    case "/my/assignments/1/complete" -> completeAssignment;
                    case "/my/assignments/1/return" -> returnAssignment;
                    case "/my/assignments" -> ownAssignments;
                    case "/my/assignments/empty" -> emptyOwnAssignments;
                    case "/my/fundraisers/1" -> ownDetails;
                    case "/home" -> cabinet;
                    case "/fundraisers" -> fundraiserList;
                    case "/fundraisers/empty" -> emptyFundraisers;
                    case "/fundraisers/1" -> fundraiserDetails;
                    case "/fundraisers/1/close" -> closeConfirmation;
                    case "/css/app.css" -> css;
                    case "/js/forms.js" -> js;
                    case "/requests" -> list;
                    case "/requests/new" -> form;
                    case "/requests/1" -> details;
                    case "/requests/1/book" -> bookingConfirmation;
                    case "/requests/1/review-details" -> reviewDetails;
                    case "/requests/1/reject" -> rejectConfirmation;
                    case "/requests/1/edit" -> edit;
                    case "/requests/1/delete" -> deletion;
                    case "/requests/1/documents/new" -> documentNew;
                    case "/requests/1/documents/1/edit" -> documentEdit;
                    case "/requests/1/documents/1/delete" -> documentDelete;
                    default -> login;
                };
                String type = path.endsWith(".css") ? "text/css" : path.endsWith(".js") ? "application/javascript" : "text/html";
                route.fulfill(new com.microsoft.playwright.Route.FulfillOptions().setContentType(type).setBody(content));
            });
            for (int width : new int[]{320, 390, 768, 1280}) {
                page.setViewportSize(width, 900);
                for (String path : new String[]{"/my/fundraisers/1/evidences/new", "/my/assignments/1/complete", "/my/assignments/1/return", "/my/assignments", "/my/assignments/empty", "/my/fundraisers/1", "/home", "/fundraisers", "/fundraisers/empty", "/fundraisers/1", "/fundraisers/1/close", "/login", "/requests", "/requests/new", "/requests/1", "/requests/1/edit", "/requests/1/delete", "/requests/1/documents/new", "/requests/1/documents/1/edit", "/requests/1/documents/1/delete", "/requests/1/review-details", "/requests/1/reject", "/requests/1/book"}) {
                    page.navigate("https://helpbridge.test" + path);
                    assertThat((Boolean) page.evaluate("document.documentElement.scrollWidth <= innerWidth")).as(path + " at " + width).isTrue();
                }
            }
            page.setViewportSize(320, 900);
            page.navigate("https://helpbridge.test/my/fundraisers/1");
            page.getByText("Додати доказ", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator("#receiptNumber")).isVisible();
            page.locator("#receiptNumber").fill("RCPT-1");
            page.locator("#attachmentUrl").fill("https://files.test/proof.jpg");
            page.locator("#recipientFeedback").fill("Отримувач підтвердив допомогу");
            page.getByText("До доказів збору", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator("#evidences")).isVisible();
            page.navigate("https://helpbridge.test/my/assignments");
            page.getByText("Завершити", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator("#closingComment")).isVisible();
            page.locator("#closingComment").fill("Допомогу доставлено отримувачу");
            page.getByText("Скасувати", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            page.getByText("Повернути", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator("#returnReason")).isVisible();
            page.getByText("Скасувати", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator("#active-heading")).isVisible();
            page.navigate("https://helpbridge.test/requests");
            page.locator(".skip-link").focus();
            page.keyboard().press("Enter");
            assertThat(page.locator("#main-content").evaluate("el => el === document.activeElement")).isEqualTo(true);
            page.locator(".table-container").focus();
            page.keyboard().press("ArrowRight");
            page.waitForCondition(() -> ((Number) page.locator(".table-container").evaluate("el => el.scrollLeft")).intValue() > 0);
            page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(java.nio.file.Path.of("target/playwright/mobile-requests.png")).setFullPage(true));
            page.navigate("https://helpbridge.test/requests/1");
            page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(java.nio.file.Path.of("target/playwright/mobile-request-details.png")).setFullPage(true));
            page.navigate("https://helpbridge.test/requests/1/edit");
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator("#institutionName")).hasValue("Установа");
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator("#deadline")).hasValue(pending.deadline().toString());
            page.locator("#assistanceType").selectOption("OTHER");
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator("#otherTypeField")).isVisible();
            page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(java.nio.file.Path.of("target/playwright/mobile-request-edit.png")).setFullPage(true));
            page.getByText("Скасувати", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            assertThat(page.url()).endsWith("/requests/1");
            page.navigate("https://helpbridge.test/requests/1/delete");
            page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(java.nio.file.Path.of("target/playwright/mobile-request-delete.png")).setFullPage(true));
            page.getByText("Скасувати", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            assertThat(page.url()).endsWith("/requests/1");
            page.navigate("https://helpbridge.test/requests/1/documents/1/edit");
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator("#fileName")).hasValue(proof.fileName());
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator("#fileUrl")).hasValue(proof.fileUrl());
            page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(java.nio.file.Path.of("target/playwright/mobile-document-edit.png")).setFullPage(true));
            page.getByText("Скасувати", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            assertThat(page.url()).endsWith("/requests/1#documents");
            page.navigate("https://helpbridge.test/requests/1/documents/1/delete");
            page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(java.nio.file.Path.of("target/playwright/mobile-document-delete.png")).setFullPage(true));
            page.navigate("https://helpbridge.test/requests/1");
            page.getByText("Взяти в роботу", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.getByText("Так, взяти в роботу")).isVisible();
            page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(java.nio.file.Path.of("target/playwright/mobile-request-booking.png")).setFullPage(true));
            page.getByText("Скасувати", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            assertThat(page.url()).endsWith("/requests/1");
            page.navigate("https://helpbridge.test/requests/1/book");
            page.waitForNavigation(() -> {
                var submitting = page.evaluate("""
                    () => {
                        const form = document.querySelector('form[data-submit-once]');
                        form.requestSubmit();
                        form.requestSubmit();
                        return form.getAttribute('aria-busy') === 'true'
                            && form.querySelector('button[type=submit]').disabled
                            && !form.querySelector('[data-submit-status]').hidden;
                    }
                    """);
                assertThat(submitting).isEqualTo(true);
            });
            assertThat(bookingSubmissions.get()).isEqualTo(1);
            page.navigate("https://helpbridge.test/requests/1/review-details");
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator("#review")).isVisible();
            page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(java.nio.file.Path.of("target/playwright/mobile-request-review.png")).setFullPage(true));
            page.getByText("Відхилити заявку", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.getByText("Так, відхилити заявку")).isVisible();
            page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(java.nio.file.Path.of("target/playwright/mobile-request-reject.png")).setFullPage(true));
            page.getByText("Скасувати", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            assertThat(page.url()).endsWith("/requests/1");
            page.navigate("https://helpbridge.test/home");
            page.locator("main a[href='/fundraisers']").click();
            page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(java.nio.file.Path.of("target/playwright/mobile-fundraisers.png")).setFullPage(true));
            page.locator(".table-container").focus();
            page.keyboard().press("ArrowRight");
            page.waitForCondition(() -> ((Number) page.locator(".table-container").evaluate("el => el.scrollLeft")).intValue() > 0);
            page.getByText("Збір №1", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(java.nio.file.Path.of("target/playwright/mobile-fundraiser-details.png")).setFullPage(true));
            page.getByText("Закрити збір", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(java.nio.file.Path.of("target/playwright/mobile-fundraiser-close.png")).setFullPage(true));
            page.getByText("Скасувати", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).click();
            assertThat(page.url()).endsWith("/fundraisers/1");
            page.navigate("https://helpbridge.test/fundraisers/1/close");
            page.waitForNavigation(() -> {
                assertThat(page.evaluate("""
                    () => {
                        const form = document.querySelector('form[data-submit-once]');
                        form.requestSubmit();
                        form.requestSubmit();
                        return form.getAttribute('aria-busy') === 'true'
                            && form.querySelector('button[type=submit]').disabled;
                    }
                    """)).isEqualTo(true);
            });
            assertThat(closingSubmissions.get()).isEqualTo(1);
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
