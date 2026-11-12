package pl.ing.cookieconsent;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;

import java.util.Optional;

final class IngCookieConsentPage {
    private static final String URL = "https://www.ing.pl";

    private final Page page;
    private final Locator consentDialog;

    IngCookieConsentPage(Page page) {
        this.page = page;
        Locator consentHeading = page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("ING Bank Śląski").setExact(false));
        this.consentDialog = page.getByRole(AriaRole.DIALOG)
                .filter(new Locator.FilterOptions().setHas(consentHeading)).first();
    }

    void open() {
        page.navigate(URL, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        consentDialog.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        consentDialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Dostosuj").setExact(true)).waitFor();
    }

    void customize() {
        consentDialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Dostosuj").setExact(true)).click();
    }

    void toggleAnalyticalCookies() {
        Locator analyticalSwitch = page.getByRole(AriaRole.SWITCH,
                new Page.GetByRoleOptions().setName("Cookies analityczne").setExact(true));
        analyticalSwitch.click();
    }

    boolean analyticalCookiesAreEnabled() {
        Locator analyticalSwitch = page.getByRole(AriaRole.SWITCH,
                new Page.GetByRoleOptions().setName("Cookies analityczne").setExact(true));
        return "true".equals(analyticalSwitch.getAttribute("aria-checked"));
    }

    void acceptSelected() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Zaakceptuj zaznaczone").setExact(true)).click();
        consentDialog.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    Optional<String> consentCookieValue() {
        return page.context().cookies().stream()
                .filter(cookie -> "cookiePolicyGDPR".equals(cookie.name))
                .map(cookie -> cookie.value)
                .findFirst();
    }

    boolean isClosed() {
        return !consentDialog.isVisible();
    }

    boolean isOpen() {
        return consentDialog.isVisible();
    }
}
