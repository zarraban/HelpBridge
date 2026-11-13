package com.example.help_bridge.playwrightTests;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.users.auth.AccountType;
import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import com.example.help_bridge.playwrightTests.pages.CreateRequestPage;
import com.example.help_bridge.playwrightTests.pages.LoginPage;
import com.example.help_bridge.playwrightTests.support.BrowserTestSupport;
import com.example.help_bridge.support.E2ETest;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@E2ETest
class RequestLifecycleBrowserE2EIT extends BrowserTestSupport {
    private static final String FIRST_REP = "first@journey.test";
    private static final String SECOND_REP = "second@journey.test";
    private static final String PROOF_URL = "https://documents.helpbridge.test/medical-proof";
    @Autowired FundRepository funds;
    @Autowired FundRepresentativeRepository representatives;
    @Autowired PasswordEncoder passwords;
    @Autowired JdbcClient jdbc;
    private long firstFund;

    @BeforeEach
    void seedRoles() {
        seedApplicant();
        seedAdmin();
        firstFund = seedFund("Фонд лікування", "92000001", FIRST_REP);
        seedFund("Другий фонд", "92000002", SECOND_REP);
        new LoginPage(page).open().loginAs(AccountType.APPLICANT, APPLICANT_EMAIL, PASSWORD);
        assertThat(page).hasURL(Pattern.compile(".*/requests(#verification)?$"));
    }

    @ParameterizedTest
    @ValueSource(ints = {390, 1280})
    void applicantAdminAndFundCompleteJourneyThroughUi(int width) {
        page.setViewportSize(width, 900);
        String details = submitRequestWithDocument();
        try (var admin = session(); var fund = session()) {
            Page adminPage = login(admin, AccountType.SYSTEM_ADMIN, ADMIN_EMAIL);
            adminPage.setViewportSize(width, 900);
            openReviewFromQueue(adminPage);
            assertThat(adminPage.locator("#documents")).containsText("Медичний висновок");
            admin.route(PROOF_URL, route -> route.fulfill(new Route.FulfillOptions()
                    .setContentType("text/plain").setBody("Test medical evidence")));
            Page proof = admin.waitForPage(() -> adminPage.getByRole(AriaRole.LINK,
                    new Page.GetByRoleOptions().setName(Pattern.compile("^Медичний висновок"))).click());
            assertThat(proof.locator("body")).containsText("Test medical evidence");
            proof.close();
            button(adminPage, "Підтвердити заявку").click();
            assertThat(adminPage.locator(".alert-success")).containsText("Заявку підтверджено");
            assertThat(adminPage.locator("#review")).hasCount(0);

            Page fundPage = login(fund, AccountType.FUND_REPRESENTATIVE, FIRST_REP);
            fundPage.setViewportSize(width, 900);
            fundPage.navigate("/requests");
            fundPage.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(Pattern.compile("Переглянути заявку №"))).click();
            link(fundPage, "Взяти в роботу").click();
            assertThat(fundPage.locator("main")).containsText("Фонд лікування");
            button(fundPage, "Так, взяти в роботу").focus();
            fundPage.keyboard().press("Enter");
            assertThat(fundPage).hasURL(details);
            assertThat(fundPage.locator("main")).containsText("Ваш фонд узяв заявку в роботу.");
            assertThat(link(fundPage, "Взяти в роботу")).hasCount(0);
            page.reload();
            assertThat(page.locator(".badge-group")).containsText("В роботі");
            assertThat(page.locator("#review")).hasCount(0);
            assertThat(link(page, "Редагувати")).hasCount(0);
            assertThat(page.locator("#documents")).containsText("Медичний висновок");
            assertStoredBooking(details);
        }
    }

    @Test
    void rejectedRequestRemainsVisibleToApplicantAndOutsideFundPool() {
        String details = submitRequestWithDocument();
        try (var admin = session(); var fund = session()) {
            Page adminPage = login(admin, AccountType.SYSTEM_ADMIN, ADMIN_EMAIL);
            openReviewFromQueue(adminPage);
            link(adminPage, "Відхилити заявку").click();
            link(adminPage, "Скасувати").click();
            assertThat(adminPage.locator("#review")).isVisible();
            link(adminPage, "Відхилити заявку").click();
            button(adminPage, "Так, відхилити заявку").click();
            assertThat(adminPage).hasURL(details);
            assertThat(adminPage.locator(".alert-success")).containsText("Заявку відхилено");
            page.reload();
            assertThat(page.locator(".badge-group")).containsText("Відхилена");
            assertThat(page.locator("#documents")).containsText("Медичний висновок");
            Page fundPage = login(fund, AccountType.FUND_REPRESENTATIVE, FIRST_REP);
            fundPage.navigate("/requests");
            assertThat(fundPage.locator("main")).containsText("Спільний пул поки порожній");
            org.assertj.core.api.Assertions.assertThat(jdbc.sql("select count(*) from fundraiser").query(Long.class).single()).isZero();
        }
    }

    @Test
    void fundWithStaleConfirmationSeesConflictAndCannotTakeBookedRequest() {
        String details = submitRequestWithDocument();
        try (var admin = session(); var first = session(); var second = session()) {
            Page adminPage = login(admin, AccountType.SYSTEM_ADMIN, ADMIN_EMAIL);
            openReviewFromQueue(adminPage);
            button(adminPage, "Підтвердити заявку").click();
            assertThat(adminPage.locator(".alert-success")).containsText("Заявку підтверджено");
            Page firstPage = login(first, AccountType.FUND_REPRESENTATIVE, FIRST_REP);
            Page secondPage = login(second, AccountType.FUND_REPRESENTATIVE, SECOND_REP);
            for (Page candidate : new Page[]{firstPage, secondPage}) {
                candidate.navigate(details);
                link(candidate, "Взяти в роботу").click();
                assertThat(button(candidate, "Так, взяти в роботу")).isVisible();
            }
            button(firstPage, "Так, взяти в роботу").click();
            assertThat(firstPage).hasURL(details);
            button(secondPage, "Так, взяти в роботу").click();
            assertThat(secondPage).hasURL(Pattern.compile(".*/requests$"));
            assertThat(secondPage.locator(".alert-error")).containsText("інший фонд");
            assertThat(secondPage.locator("main")).containsText("Спільний пул поки порожній");
            assertStoredBooking(details);
        }
    }

    private String submitRequestWithDocument() {
        new CreateRequestPage(page).open().selectAssistanceType("MEDICAL").fillAmount("2500")
                .fillDeadline(LocalDate.now().plusDays(30)).fillSituationDescription("Потрібне лікування після травми")
                .fillNeedDescription("Оплата лікування").fillInstitutionName("Клініка")
                .fillApplicationNumber("BROWSER-JOURNEY").giveConsent().submit();
        assertThat(page).hasURL(Pattern.compile(".*/requests/[0-9]+$"));
        String details = page.url();
        link(page, "Додати документ").click();
        page.getByLabel("Назва документа", new Page.GetByLabelOptions().setExact(true)).fill("Медичний висновок");
        page.getByLabel("Посилання на документ", new Page.GetByLabelOptions().setExact(true)).fill(PROOF_URL);
        button(page, "Додати документ").click();
        assertThat(page.locator("#documents")).containsText("Медичний висновок");
        assertThat(page.locator(".badge-group")).containsText("Очікує перевірки");
        return details;
    }

    private void openReviewFromQueue(Page admin) {
        admin.navigate("/requests#verification");
        admin.locator("#verification").getByRole(AriaRole.LINK,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName(Pattern.compile("Перевірити заявку №"))).click();
        assertThat(admin.locator("#review")).isVisible();
    }

    private void assertStoredBooking(String details) {
        long id = Long.parseLong(details.substring(details.lastIndexOf('/') + 1));
        org.assertj.core.api.Assertions.assertThat(jdbc.sql("select fund_id from requests where id = ?")
                .param(id).query(Long.class).single()).isEqualTo(firstFund);
        org.assertj.core.api.Assertions.assertThat(jdbc.sql("select count(*) from fundraiser where request_id = ?")
                .param(id).query(Long.class).single()).isEqualTo(1);
    }

    private long seedFund(String name, String edrpou, String email) {
        Fund fund = new Fund(name, edrpou, "UA00", "Kyiv", "Kyiv", "+380501112233", email, "https://fund.test", Map.of());
        fund.setStatus(FundStatus.APPROVED);
        fund = funds.saveAndFlush(fund);
        representatives.saveAndFlush(new FundRepresentative("Test", "Rep", email, "+380501112233", passwords.encode(PASSWORD), fund));
        return fund.getId();
    }

    private BrowserContext session() {
        return context.browser().newContext(new com.microsoft.playwright.Browser.NewContextOptions()
                .setBaseURL("http://localhost:" + hostPort));
    }

    private Page login(BrowserContext session, AccountType role, String email) {
        Page result = session.newPage();
        new LoginPage(result).open().loginAs(role, email, PASSWORD);
        assertThat(result).hasURL(Pattern.compile(".*/requests(#verification)?$"));
        return result;
    }

    private static com.microsoft.playwright.Locator link(Page page, String name) {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(name).setExact(true));
    }

    private static com.microsoft.playwright.Locator button(Page page, String name) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
    }
}
