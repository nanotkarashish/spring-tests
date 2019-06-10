package com.testwithspring.master.web

import com.testwithspring.master.web.login.LoginPage
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver

/**
 * This page object is used to interact with the navigation bar.
 */
final class NavigationBar {

    private static final String LOGOUT_FORM_ID = "logout-form"

    private final WebDriver browser

    NavigationBar(WebDriver browser) {
        this.browser = browser
    }

    /**
     * Logs the current user out.
     * @return the next that is rendered when user is logged out (login page).
     * @throws RuntimeException if logout button is not visible.
     */
    LoginPage logUserOut() {
        if (browser.findElements(By.id(LOGOUT_FORM_ID)).isEmpty()) {
            throw new RuntimeException('Cannot log the user out because the logout button is not visible. Is user logged in?')
        }

        browser.findElement(By.id(LOGOUT_FORM_ID)).submit()
        return new LoginPage(browser)
    }
}
