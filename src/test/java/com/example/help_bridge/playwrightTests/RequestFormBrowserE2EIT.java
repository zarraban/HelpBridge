package com.example.help_bridge.playwrightTests;

import com.example.help_bridge.playwrightTests.pages.CreateRequestPage;
import com.example.help_bridge.playwrightTests.pages.LoginPage;
import com.example.help_bridge.playwrightTests.pages.RequestsListPage;
import com.example.help_bridge.playwrightTests.support.BrowserTestSupport;
import com.example.help_bridge.support.E2ETest;
import com.example.help_bridge.users.auth.AccountType;
import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@E2ETest
class RequestFormBrowserE2EIT extends BrowserTestSupport {

    @BeforeEach
    void loginAsApplicant() {
        seedApplicant();
        new LoginPage(page).open().loginAs(AccountType.APPLICANT, APPLICANT_EMAIL, PASSWORD);
    }

    @Test
    void applicantCreatesRequestAndSeesItInList() {
        RequestsListPage requestsPage = new RequestsListPage(page);
        CreateRequestPage form = requestsPage.openNewRequestForm();
        LocalDate deadline = LocalDate.now().plusDays(10);

        form.selectAssistanceType("MEDICAL")
                .fillAmount("2500")
                .fillDeadline(deadline)
                .fillSituationDescription("Smth has happened")
                .fillInstitutionName("Our Academy")
                .fillNeedDescription("Bad things has happened")
                .fillApplicationNumber("1")
                .giveConsent()
                .submit();

        requestsPage.assertOpened();
        assertThat(requestsPage.successMessage())
                .hasText("Заявку подано. Вона з'явиться у спільному пулі після підтвердження адміністратором.");
        assertThat(requestsPage.rows()).hasCount(1);
        assertThat(requestsPage.rows().first().locator("td")).hasText(new String[]{
                "Лікування",
                "Bad things has happened",
                "2500.00",
                "Our Academy",
                deadline.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                "Очікує перевірки"
        });
    }

    @Test
    void otherAssistanceTypeShowsExtraFieldAndEmptyFormShowsErrors() {
        CreateRequestPage form = new CreateRequestPage(page).open();

        assertThat(form.otherTypeField()).isHidden();

        form.selectAssistanceType("OTHER");
        assertThat(form.otherTypeField()).isVisible();

        form.selectAssistanceType("MEDICAL");
        assertThat(form.otherTypeField()).isHidden();

        form.submit();
        assertThat(form.fieldErrors().first()).isVisible();
        assertThat(form.fieldErrors().filter(new Locator.FilterOptions().setHasText("Вкажіть суму"))).isVisible();
    }
}
