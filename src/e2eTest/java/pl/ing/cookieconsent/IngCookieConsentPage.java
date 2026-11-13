package pl.ing.cookieconsent;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;

import java.util.Optional;

final class IngCookieConsentPage {
    private static final String URL = "https://www.ing.pl";
    private static final String CONSENT_HEADING = "ING Bank Śląski";
    private static final String SETTINGS_HEADING = "Ustawienia plików cookies i innych technologii";

    private final Page page;
    private final Locator consentDialog;
    private final Locator settingsDialog;

    IngCookieConsentPage(Page page) {
        this.page = page;
        this.consentDialog = page.getByRole(AriaRole.DIALOG)
                .filter(new Locator.FilterOptions().setHas(page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName(CONSENT_HEADING).setExact(false)))).first();
        this.settingsDialog = page.getByRole(AriaRole.DIALOG)
                .filter(new Locator.FilterOptions().setHas(page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName(SETTINGS_HEADING).setExact(true)))).first();
    }

    void open() {
        page.navigate(URL, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        consentDialog.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    void openSettings() {
        if (consentDialog.isVisible()) {
            button(consentDialog, "Dostosuj").click();
        } else {
            button(page, "Ustawienia cookie").click();
        }
        settingsDialog.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    void acceptAll() {
        button(consentDialog, "Zaakceptuj wszystkie").click();
        waitForConsentClosed();
    }

    void rejectAll() {
        button(consentDialog, "Odrzuć wszystkie").click();
        waitForConsentClosed();
    }

    void saveSelected() {
        button(settingsDialog, "Zaakceptuj zaznaczone").click();
        waitForSettingsClosed();
    }

    void rejectAllFromSettings() {
        button(settingsDialog, "Odrzuć wszystkie").click();
        waitForSettingsClosed();
    }

    void toggleAnalyticalCookies() {
        analyticalSwitch().click();
    }

    void toggleMarketingCookies() {
        marketingSwitch().click();
    }

    boolean analyticalCookiesAreEnabled() {
        return "true".equals(analyticalSwitch().getAttribute("aria-checked"));
    }

    boolean marketingCookiesAreEnabled() {
        return "true".equals(marketingSwitch().getAttribute("aria-checked"));
    }

    boolean technicalCookiesAreEnabled() {
        return "true".equals(settingsDialog.getByRole(AriaRole.SWITCH,
                new Locator.GetByRoleOptions().setName("Cookies techniczne").setExact(true))
                .getAttribute("aria-checked"));
    }

    boolean technicalCookiesAreDisabledForEditing() {
        return settingsDialog.getByRole(AriaRole.SWITCH,
                new Locator.GetByRoleOptions().setName("Cookies techniczne").setExact(true)).isDisabled();
    }

    Optional<String> consentCookieValue() {
        return page.context().cookies().stream()
                .filter(cookie -> "cookiePolicyGDPR".equals(cookie.name))
                .map(cookie -> cookie.value)
                .findFirst();
    }

    boolean consentDialogIsVisible() {
        return consentDialog.isVisible();
    }

    boolean settingsDialogIsVisible() {
        return settingsDialog.isVisible();
    }

    private Locator analyticalSwitch() {
        return settingsDialog.getByRole(AriaRole.SWITCH,
                new Locator.GetByRoleOptions().setName("Cookies analityczne").setExact(true));
    }

    private Locator marketingSwitch() {
        return settingsDialog.getByRole(AriaRole.SWITCH,
                new Locator.GetByRoleOptions().setName("Cookies marketingowe").setExact(true));
    }

    private static Locator button(Locator scope, String name) {
        return scope.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(name).setExact(true));
    }

    private static Locator button(Page page, String name) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
    }

    private void waitForConsentClosed() {
        consentDialog.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    private void waitForSettingsClosed() {
        settingsDialog.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }
}
