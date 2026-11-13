package com.example.help_bridge.fundraising.request;

import com.example.help_bridge.fundraising.request.repository.RequestRepository;
import com.example.help_bridge.fundraising.request.repository.RequestDocumentRepository;
import com.example.help_bridge.fundraising.user.entity.User;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

@SpringBootTest(properties = {"spring.jpa.show-sql=false", "logging.level.root=WARN"})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
@Transactional
class RequestJourneyE2EIT {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired RequestRepository requests;
    @Autowired RequestDocumentRepository documents;

    @Test
    void applicantCompletesRequestAndDocumentJourney() {
        var owner = users.save(new User("Journey", "Applicant", "journey@flow.test", "hash"));
        var session = new MockHttpSession();
        try (var playwright = Playwright.create(); var browser = playwright.chromium().launch()) {
            var page = browser.newPage(new Browser.NewPageOptions().setViewportSize(390, 844));
            // Route browser traffic through the real MVC stack, services and database without a listening port.
            page.route("https://helpbridge.test/**", route -> {
                try {
                    var uri = URI.create(route.request().url());
                    var builder = request(org.springframework.http.HttpMethod.valueOf(route.request().method()), uri.getPath())
                            .session(session).with(user("APPLICANT:" + owner.getEmail()).roles("APPLICANT"))
                            .characterEncoding("UTF-8");
                    String body = route.request().postData();
                    if (body != null) {
                        for (String pair : body.split("&")) {
                            String[] values = pair.split("=", 2);
                            builder.param(URLDecoder.decode(values[0], StandardCharsets.UTF_8),
                                    values.length == 2 ? URLDecoder.decode(values[1], StandardCharsets.UTF_8) : "");
                        }
                    }
                    var response = mvc.perform(builder).andReturn().getResponse();
                    var headers = new HashMap<String, String>();
                    headers.put("Cache-Control", "no-store");
                    if (response.getStatus() >= 300 && response.getStatus() < 400 && response.getHeader("Location") != null) {
                        // A fulfilled redirect bypasses Playwright routing for the rest of its chain.
                        // Start a fresh GET navigation so every page still uses the real MVC stack.
                        String location = org.springframework.web.util.HtmlUtils.htmlEscape(response.getHeader("Location"));
                        route.fulfill(new Route.FulfillOptions().setStatus(200).setContentType("text/html")
                                .setBody("<html><head><meta http-equiv=\"refresh\" content=\"0;url=" + location + "\"></head></html>"));
                        return;
                    }
                    route.fulfill(new Route.FulfillOptions().setStatus(response.getStatus()).setHeaders(headers)
                            .setContentType(response.getContentType() == null ? "text/plain" : response.getContentType())
                            .setBody(response.getContentAsString()));
                } catch (Exception exception) {
                    route.abort();
                    throw new IllegalStateException("Failed to route browser request", exception);
                }
            });
            page.navigate("https://helpbridge.test/requests");
            link(page, "Подати нову заявку").click();
            page.locator("#assistanceType").selectOption("MEDICAL");
            page.locator("#amount").fill("1250.50");
            page.locator("#deadline").fill(LocalDate.now().plusDays(30).toString());
            page.locator("#situationDescription").fill("Потрібне лікування після травми");
            page.locator("#needDescription").fill("Оплата лікування");
            page.locator("#institutionName").fill("Клініка");
            page.locator("#applicationNumber").fill("JOURNEY-1");
            page.locator("#dataProcessingConsent").check();
            button(page, "Подати заявку").click();
            assertThat(page).hasURL(java.util.regex.Pattern.compile("https://helpbridge\\.test/requests/[0-9]+"));
            long id = requests.findByRequesterIdWithDetails(owner.getId()).getFirst().getId();
            String details = "https://helpbridge.test/requests/" + id;
            assertThat(page).hasURL(details);
            assertThat(page.locator(".alert-success")).containsText("Заявку подано");

            link(page, "Редагувати").click();
            page.locator("#institutionName").fill("Оновлена клініка");
            button(page, "Зберегти зміни").click();
            assertThat(page).hasURL(details);
            assertThat(page.locator("main")).containsText("Оновлена клініка");
            link(page, "Редагувати").click();
            page.locator("#institutionName").fill("Незбережені зміни");
            link(page, "Скасувати").click();
            assertThat(page).hasURL(details);
            assertThat(requests.findById(id).orElseThrow().getInstitutionName()).isEqualTo("Оновлена клініка");

            link(page, "Додати документ").click();
            page.locator("#fileName").fill("Медичний висновок");
            page.locator("#fileUrl").fill("https://files.test/proof.pdf");
            button(page, "Додати документ").click();
            assertThat(page).hasURL(details + "#documents");
            assertThat(page.locator("#documents .alert-success")).isInViewport();
            assertThat(page.locator(".alert-success")).hasCount(1);
            page.locator("#documents").getByText("Редагувати", new Locator.GetByTextOptions().setExact(true)).click();
            page.locator("#fileName").fill("Оновлений висновок");
            button(page, "Зберегти зміни").click();
            assertThat(page.locator("#documents .alert-success")).isInViewport();
            var proof = documents.findByRequestId(id).getFirst();
            assertThat(proof.getFileName()).isEqualTo("Оновлений висновок");
            page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Path.of("target/playwright/request-journey-documents.png")));

            page.locator("#documents").getByText("Видалити", new Locator.GetByTextOptions().setExact(true)).click();
            link(page, "Скасувати").click();
            assertThat(page).hasURL(details + "#documents");
            assertThat(documents.existsById(proof.getId())).isTrue();
            page.locator("#documents").getByText("Видалити", new Locator.GetByTextOptions().setExact(true)).click();
            button(page, "Так, видалити документ").click();
            assertThat(page).hasURL(details + "#documents");
            assertThat(page.locator("#documents")).containsText("ще не додано документів");
            assertThat(documents.existsById(proof.getId())).isFalse();

            link(page, "Видалити").click();
            link(page, "Скасувати").click();
            assertThat(page).hasURL(details);
            assertThat(requests.existsById(id)).isTrue();
            link(page, "Видалити").click();
            button(page, "Так, видалити заявку").click();
            assertThat(page).hasURL("https://helpbridge.test/requests");
            assertThat(page.locator(".alert-success")).containsText("видалено");
            assertThat(requests.existsById(id)).isFalse();
        }
    }

    private Locator link(Page page, String name) {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(name).setExact(true));
    }
    private Locator button(Page page, String name) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
    }
}
