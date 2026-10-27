package com.example.help_bridge.playwrightTests.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class RequestsListPage {

    private final Page page;

    public RequestsListPage(Page page) {
        this.page = page;
    }

    public RequestsListPage open() {
        page.navigate("/requests");
        return this;
    }

    public void assertOpened() {
        assertThat(page).hasURL(Pattern.compile(".*/requests$"));
    }

    public Locator heading() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1));
    }

    public Locator successMessage() {
        return page.locator(".alert-success");
    }

    public Locator rows() {
        return page.locator("tbody tr");
    }

    public Locator newRequestButton() {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Подати нову заявку"));
    }

    public Locator logoutButton() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Вийти"));
    }

    public CreateRequestPage openNewRequestForm() {
        newRequestButton().click();
        return new CreateRequestPage(page);
    }
}
