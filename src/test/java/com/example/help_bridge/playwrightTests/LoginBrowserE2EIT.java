package com.example.help_bridge.playwrightTests;

import com.example.help_bridge.playwrightTests.pages.LoginPage;
import com.example.help_bridge.playwrightTests.pages.RequestsListPage;
import com.example.help_bridge.playwrightTests.support.BrowserTestSupport;
import com.example.help_bridge.support.E2ETest;
import com.example.help_bridge.users.auth.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@E2ETest
class LoginBrowserE2EIT extends BrowserTestSupport {

    @BeforeEach
    void seedAccounts() {
        seedApplicant();
    }

    @Test
    void applicantLogsInAndSeesOwnRequests() {
        LoginPage loginPage = new LoginPage(page).open();

        loginPage.loginAs(AccountType.APPLICANT, APPLICANT_EMAIL, PASSWORD);

        RequestsListPage requestsPage = new RequestsListPage(page);
        requestsPage.assertOpened();
        assertThat(requestsPage.heading()).hasText("Мої заявки на допомогу");
        assertThat(requestsPage.newRequestButton()).isVisible();
    }

    @Test
    void wrongPasswordShowsErrorAndStaysOnLogin() {
        LoginPage loginPage = new LoginPage(page).open();

        loginPage.loginAs(AccountType.APPLICANT, APPLICANT_EMAIL, "wrongPassword");

        assertThat(loginPage.errorMessage()).isVisible();
        assertThat(loginPage.errorMessage()).hasText("Невірний email або пароль.");
    }
}
