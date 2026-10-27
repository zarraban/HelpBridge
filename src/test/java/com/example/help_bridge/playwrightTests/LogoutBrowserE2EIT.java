package com.example.help_bridge.playwrightTests;

import com.example.help_bridge.playwrightTests.pages.LoginPage;
import com.example.help_bridge.playwrightTests.pages.RequestsListPage;
import com.example.help_bridge.playwrightTests.support.BrowserTestSupport;
import com.example.help_bridge.support.E2ETest;
import com.example.help_bridge.users.auth.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@E2ETest
public class LogoutBrowserE2EIT extends BrowserTestSupport {


    @BeforeEach
    void seedAccount() {
        seedApplicant();
    }

    @Test
    void test_successMessageReceivedWhenLogOut() {
        LoginPage loginPage = new LoginPage(page).open();

        loginPage.loginAs(AccountType.APPLICANT, APPLICANT_EMAIL, PASSWORD);
        new RequestsListPage(page).logoutButton().click();

        assertThat(loginPage.logoutMessage()).isVisible();
        assertThat(loginPage.logoutMessage()).hasText("Ви вийшли з системи.");
    }
}
