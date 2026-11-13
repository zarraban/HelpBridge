package com.example.help_bridge.playwrightTests;

import com.example.help_bridge.fundraising.fund.entity.*;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.fundraising.fundraiser.entity.*;
import com.example.help_bridge.fundraising.fundraiser.repository.*;
import com.example.help_bridge.fundraising.request.entity.Request;
import com.example.help_bridge.fundraising.request.repository.*;
import com.example.help_bridge.fundraising.user.entity.User;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.example.help_bridge.playwrightTests.pages.LoginPage;
import com.example.help_bridge.playwrightTests.support.BrowserTestSupport;
import com.example.help_bridge.users.auth.AccountType;
import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import com.example.help_bridge.users.volunteer.entity.VolunteerStatus;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:volunteer-browser;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.jpa.show-sql=false", "logging.level.root=WARN"})
@ActiveProfiles("dev")
class VolunteerLifecycleBrowserE2EIT extends BrowserTestSupport {
    static final AtomicInteger sequence = new AtomicInteger();
    @Autowired FundRepository funds;
    @Autowired FundRepresentativeRepository representatives;
    @Autowired UserRepository users;
    @Autowired RequestRepository requests;
    @Autowired AssistanceTypeRepository types;
    @Autowired FundraiserRepository fundraisers;
    @Autowired FundraiserAssignmentRepository assignments;
    @Autowired VolunteerRepository volunteers;
    @Autowired PasswordEncoder passwords;
    Fund fund;
    Fundraiser fundraiser;
    String repEmail;
    String volunteerEmail;

    @BeforeEach void seedJourney() {
        int n = sequence.incrementAndGet();
        repEmail = "rep" + n + "@volunteer-journey.test";
        volunteerEmail = "volunteer" + n + "@volunteer-journey.test";
        fund = new Fund("Фонд волонтерів " + n, "9400000" + n, "UA00", "Kyiv", "Kyiv", "+380501112233", repEmail, "https://fund.test", Map.of());
        fund.setStatus(FundStatus.APPROVED);
        fund = funds.saveAndFlush(fund);
        representatives.saveAndFlush(new FundRepresentative("Представник", "Фонду", repEmail, "+380501112233", passwords.encode(PASSWORD), fund));
        var owner = users.saveAndFlush(new User("Applicant", "Test", "owner" + n + "@volunteer-journey.test", passwords.encode(PASSWORD)));
        var request = new Request(owner, types.findById("MEDICAL").orElseThrow(), BigDecimal.TEN,
                LocalDate.now().plusDays(10), "Situation", "Доставити медичну допомогу", "Клініка", "DOC", true);
        request.approve(); request.book(fund);
        request = requests.saveAndFlush(request);
        fundraiser = new Fundraiser(); fundraiser.setRequestId(request.getId()); fundraiser.setStatus(FundraiserStatus.PENDING_ASSIGNMENT);
        fundraiser = fundraisers.saveAndFlush(fundraiser);
    }

    @ParameterizedTest @ValueSource(ints = {390, 1280})
    void representativeAndVolunteerCompleteJourneyThroughUi(int width) {
        page.setViewportSize(width, 900);
        new LoginPage(page).open().loginAs(AccountType.FUND_REPRESENTATIVE, repEmail, PASSWORD);
        page.locator("main a[href='/team']").click();
        assertThat(page.locator("main")).containsText("У команді ще немає волонтерів");
        fitsViewport(page);
        link(page, "Додати волонтера").click();
        fitsViewport(page);
        fillVolunteer(page);
        button(page, "Додати волонтера").click();
        assertThat(page.locator(".alert-success")).containsText("Волонтера додано");
        Long volunteerId = volunteers.findByEmail(volunteerEmail).orElseThrow().getId();

        link(page, "Додати волонтера").click(); fillVolunteer(page); button(page, "Додати волонтера").click();
        assertThat(page.locator("main")).containsText("Email або телефон уже належить");
        assertThat(page.locator("#password")).hasValue("");
        link(page, "Скасувати").click();
        page.getByText("Редагувати", new Page.GetByTextOptions().setExact(true)).click();
        page.locator("#lastName").fill("Оновлена"); button(page, "Зберегти зміни").click();
        assertThat(page.locator("main")).containsText("Олена Оновлена");
        fitsViewport(page);

        openFundraiser(page); assign(page, volunteerId);
        assertThat(page.locator("#assignments")).containsText("Активне");
        org.assertj.core.api.Assertions.assertThat(fundraisers.findById(fundraiser.getId()).orElseThrow().getStatus()).isEqualTo(FundraiserStatus.IN_PROGRESS);
        page.navigate("/funds/" + fund.getId() + "/volunteers");
        page.getByText("Видалити", new Page.GetByTextOptions().setExact(true)).click(); button(page, "Видалити волонтера").click();
        assertThat(page.locator(".alert-error")).containsText("активні призначення");

        try (var volunteerContext = browserContext()) {
            Page volunteer = volunteerContext.newPage(); volunteer.setViewportSize(width, 900);
            new LoginPage(volunteer).open().loginAs(AccountType.VOLUNTEER, volunteerEmail, PASSWORD);
            volunteer.locator("main a[href='/my/assignments']").click();
            volunteer.getByText("Деталі збору", new Page.GetByTextOptions().setExact(true)).click();
            assertThat(volunteer.locator("main")).containsText("Доставити медичну допомогу");
            link(volunteer, "Додати доказ").click(); button(volunteer, "Додати доказ").click();
            assertThat(volunteer.locator("main")).containsText("Заповніть хоча б одне поле");
            volunteer.locator("#receiptNumber").fill("RCPT-1");
            volunteer.locator("#recipientFeedback").fill("Отримувач підтвердив допомогу");
            button(volunteer, "Додати доказ").click();
            assertThat(volunteer.locator("#evidences")).containsText("RCPT-1");
            volunteer.getByText("Повернути", new Page.GetByTextOptions().setExact(true)).click();
            volunteer.locator("#returnReason").fill("Потрібна допомога іншого волонтера"); button(volunteer, "Повернути призначення").click();
            assertThat(volunteer.locator("main")).containsText("Потрібна допомога іншого волонтера");
            org.assertj.core.api.Assertions.assertThat(fundraisers.findById(fundraiser.getId()).orElseThrow().getStatus()).isEqualTo(FundraiserStatus.PENDING_ASSIGNMENT);
            openFundraiser(page); assign(page, volunteerId);
            volunteer.reload();
            var active = volunteer.locator("section[aria-labelledby='active-heading']");
            active.getByText("Завершити", new Locator.GetByTextOptions().setExact(true)).click();
            String completedFormUrl = volunteer.url();
            volunteer.locator("#closingComment").fill("Допомогу доставлено у повному обсязі"); button(volunteer, "Підтвердити завершення").click();
            assertThat(volunteer.locator("main")).containsText("Допомогу доставлено у повному обсязі");
            volunteer.navigate(completedFormUrl);
            assertThat(volunteer.locator("main")).containsText("дію вже виконано");
            assertThat(volunteer.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Підтвердити завершення"))).hasCount(0);
            openFundraiser(page);
            assertThat(page.locator("#assignments")).containsText("Допомогу доставлено у повному обсязі");
            org.assertj.core.api.Assertions.assertThat(fundraisers.findById(fundraiser.getId()).orElseThrow().getStatus()).isEqualTo(FundraiserStatus.IN_PROGRESS);
            link(page, "Закрити збір").click(); button(page, "Так, закрити збір").click();
            assertThat(page.locator("main")).containsText("Збір успішно закрито");
            volunteer.navigate("/my/fundraisers/" + fundraiser.getId());
            assertThat(volunteer.locator("main")).containsText("Закритий");
            assertThat(volunteer.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Додати доказ").setExact(true))).hasCount(0);
        }
        page.navigate("/funds/" + fund.getId() + "/volunteers");
        page.getByText("Видалити", new Page.GetByTextOptions().setExact(true)).click(); button(page, "Видалити волонтера").click();
        assertThat(page.locator("main")).containsText("У команді ще немає волонтерів");
        org.assertj.core.api.Assertions.assertThat(volunteers.findById(volunteerId).orElseThrow().getStatus()).isEqualTo(VolunteerStatus.INACTIVE);
        org.assertj.core.api.Assertions.assertThat(assignments.findByVolunteerId(volunteerId)).hasSize(2);
    }

    private void fillVolunteer(Page p) {
        p.locator("#firstName").fill("Олена"); p.locator("#lastName").fill("Коваль");
        p.locator("#email").fill(volunteerEmail); p.locator("#phone").fill("+380501234567"); p.locator("#password").fill(PASSWORD);
    }
    private void openFundraiser(Page p) {
        p.locator("nav a[href='/fundraisers']").click();
        p.getByText("Збір №" + fundraiser.getId(), new Page.GetByTextOptions().setExact(true)).click();
    }
    private void assign(Page p, Long id) {
        link(p, "Призначити волонтера").click(); fitsViewport(p); p.locator("#volunteerId").selectOption(id.toString()); button(p, "Призначити волонтера").click();
        assertThat(p.locator(".alert-success")).containsText("Волонтера призначено");
    }
    private static void fitsViewport(Page p) {
        org.assertj.core.api.Assertions.assertThat((Boolean) p.evaluate("document.documentElement.scrollWidth <= innerWidth"))
                .as("Page fits viewport: " + p.url()).isTrue();
    }
    private BrowserContext browserContext() { return page.context().browser().newContext(new Browser.NewContextOptions().setBaseURL("http://localhost:" + hostPort)); }
    private static Locator button(Page p, String text) { return p.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(text).setExact(true)); }
    private static Locator link(Page p, String text) { return p.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(text).setExact(true)); }
}
