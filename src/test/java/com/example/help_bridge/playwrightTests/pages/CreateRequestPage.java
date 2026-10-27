package com.example.help_bridge.playwrightTests.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.time.LocalDate;

// Fields are located by id: label texts overlap ("Тип допомоги" is also a part of the "Інше" field label)
public class CreateRequestPage {

    private final Page page;

    public CreateRequestPage(Page page) {
        this.page = page;
    }

    public CreateRequestPage open() {
        page.navigate("/requests/new");
        return this;
    }

    public CreateRequestPage selectAssistanceType(String code) {
        page.locator("#assistanceType").selectOption(code);
        return this;
    }

    public CreateRequestPage fillOtherAssistanceType(String text) {
        page.locator("#otherAssistanceType").fill(text);
        return this;
    }

    public CreateRequestPage fillAmount(String amount) {
        page.locator("#amount").fill(amount);
        return this;
    }

    public CreateRequestPage fillDeadline(LocalDate deadline) {
        page.locator("#deadline").fill(deadline.toString());
        return this;
    }

    public CreateRequestPage fillSituationDescription(String text) {
        page.locator("#situationDescription").fill(text);
        return this;
    }

    public CreateRequestPage fillNeedDescription(String text) {
        page.locator("#needDescription").fill(text);
        return this;
    }

    public CreateRequestPage fillInstitutionName(String text) {
        page.locator("#institutionName").fill(text);
        return this;
    }

    public CreateRequestPage fillApplicationNumber(String text) {
        page.locator("#applicationNumber").fill(text);
        return this;
    }

    public CreateRequestPage giveConsent() {
        page.locator("#dataProcessingConsent").check();
        return this;
    }

    public void submit() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Подати заявку")).click();
    }

    // Shown only when the "Інше" option is selected (toggled by JS)
    public Locator otherTypeField() {
        return page.locator("#otherTypeField");
    }

    public Locator fieldErrors() {
        return page.locator(".field-error");
    }
}
