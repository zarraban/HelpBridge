package com.example.help_bridge.playwrightTests.support;

import com.example.help_bridge.fundraising.user.entity.User;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import com.example.help_bridge.users.systemadmin.entity.SystemAdmin;
import com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class BrowserTestSupport {

    static Playwright playwright;
    static Browser browser;

    @LocalServerPort
    protected int hostPort;
    protected BrowserContext context;
    protected Page page;

    protected static final String PASSWORD = "Pass1234!";
    protected static final String APPLICANT_EMAIL = "applicant@helpbridge.test";
    protected static final String ADMIN_EMAIL = "admin@helpbridge.test";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SystemAdminRepository systemAdminRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    final String LOCALHOST = "http://localhost:";
    static final Path ARTIFACTS_DIR = Paths.get("target/playwright");

    @RegisterExtension
    final FailureFlag failure = new FailureFlag();

    @BeforeAll
    static void openBrowser(){
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @BeforeEach
    void createSession(){
        context = browser.newContext(new Browser.NewContextOptions()
                .setBaseURL(LOCALHOST+hostPort));
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true));
        page = context.newPage();
    }

    @AfterEach
    void closeSession(TestInfo info) throws IOException {
        if (failure.failed) {
            String name = info.getTestClass().map(Class::getSimpleName).orElse("Test")
                    + "_" + info.getTestMethod().map(m -> m.getName()).orElse("test");
            Files.createDirectories(ARTIFACTS_DIR);

            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(ARTIFACTS_DIR.resolve(name + ".png"))
                    .setFullPage(true));
            context.tracing().stop(new Tracing.StopOptions()
                    .setPath(ARTIFACTS_DIR.resolve(name + "-trace.zip")));
        } else {
            context.tracing().stop();
        }
        context.close();
    }

    protected void seedApplicant() {
        userRepository.save(new User("Test", "Applicant", APPLICANT_EMAIL, passwordEncoder.encode(PASSWORD)));
    }

    protected void seedAdmin() {
        systemAdminRepository.save(new SystemAdmin(ADMIN_EMAIL, passwordEncoder.encode(PASSWORD), "Test", "Admin"));
    }

    @AfterAll
    static void closeBrowser(){
        browser.close();
        playwright.close();
    }

    static class FailureFlag implements TestExecutionExceptionHandler {
        volatile boolean failed;

        @Override
        public void handleTestExecutionException(ExtensionContext ctx, Throwable t) throws Throwable {
            failed = true;
            throw t;
        }
    }
}
