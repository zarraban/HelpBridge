package com.example.help_bridge.playwrightTests.pages;

import com.example.help_bridge.users.auth.AccountType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class LoginPage {

    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public LoginPage open() {
        page.navigate("/login");
        return this;
    }

    public void loginAs(AccountType accountType, String email, String password){
        page.getByLabel("Тип облікового запису").selectOption(accountType.name());
        page.getByLabel("Email").fill(email);
        page.getByLabel("Пароль").fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Увійти")).click();
    }
    public Locator errorMessage() {
        return page.locator(".alert-error");
    }

    public Locator logoutMessage() {
        return page.locator(".alert-success");
    }
}
