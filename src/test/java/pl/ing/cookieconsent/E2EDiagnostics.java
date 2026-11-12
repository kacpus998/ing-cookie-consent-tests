package pl.ing.cookieconsent;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class E2EDiagnostics {
    private static final int MAX_HTML_CHARS = 20_000;
    private static final int MAX_LOG_CHARS = 2_000;
    private static final Pattern SENSITIVE_VALUE = Pattern.compile(
            "(?i)(token|secret|password|authorization|credential|session|csrf|cookie)([\\\"'\\s:=]+)[^\\s,;\\\"'<>]+"
    );
    private static final Pattern URL_QUERY = Pattern.compile("(https?://[^\\s\\\"'<>?]+)\\?[^\\s\\\"'<>]*");

    private E2EDiagnostics() {}

    static void capture(Page page, BrowserName browser, Response navigationResponse,
                        List<String> redirects, List<String> pageErrors, List<String> consoleErrors) {
        Path outputDirectory = Path.of("build", "test-diagnostics", browser.name().toLowerCase());
        List<String> lines = new ArrayList<>();
        try {
            Files.createDirectories(outputDirectory);
            lines.add("browser: " + browser);
            lines.add("url: " + safeUrl(safeRead(page::url)));
            lines.add("title: " + safeText(safeRead(page::title)));
            lines.add("main_navigation_http_status: " +
                    (navigationResponse == null ? "unavailable" : navigationResponse.status()));
            lines.add("main_navigation_url: " +
                    (navigationResponse == null ? "unavailable" : safeUrl(navigationResponse.url())));
            lines.add("redirect_chain:");
            lines.addAll(redirects.isEmpty() ? List.of("  unavailable") : redirects);
            lines.add("dialog_elements_count: " + safeCount(page.locator("[role='dialog'], dialog")));
            lines.add("ING_Bank_Slaski_heading_count: " + safeCount(page.getByRole(AriaRole.HEADING,
                    new Page.GetByRoleOptions().setName("ING Bank Śląski").setExact(false))));
            lines.add("page_errors:");
            lines.addAll(safeLines(pageErrors));
            lines.add("console_errors:");
            lines.addAll(safeLines(consoleErrors));

            write(outputDirectory.resolve("diagnostics.txt"), String.join("\n", lines) + "\n");
            write(outputDirectory.resolve("page-fragment.html"), safeText(safeRead(() -> (String) page.evaluate(
                    """
                    () => {
                      const body = document.body ? document.body.cloneNode(true) : document.documentElement.cloneNode(true);
                      body.querySelectorAll('script, style, noscript, input, textarea, select, option').forEach(el => el.remove());
                      const sensitive = /(token|secret|password|authorization|credential|session|csrf|cookie)/i;
                      for (const element of [body, ...body.querySelectorAll('*')]) {
                        for (const attribute of [...element.attributes]) {
                          if (sensitive.test(attribute.name)) element.removeAttribute(attribute.name);
                          else if (['href', 'src', 'action'].includes(attribute.name.toLowerCase())) {
                            try {
                              const url = new URL(attribute.value, location.href);
                              element.setAttribute(attribute.name, url.origin + url.pathname);
                            } catch (_) {
                              element.removeAttribute(attribute.name);
                            }
                          }
                        }
                      }
                      return body.outerHTML;
                    }
                    """))));
            try {
                page.screenshot(new Page.ScreenshotOptions().setPath(outputDirectory.resolve("screenshot.png")));
            } catch (RuntimeException screenshotError) {
                lines.add("screenshot_error: " + safeText(screenshotError.getMessage()));
                write(outputDirectory.resolve("diagnostics.txt"), String.join("\n", lines) + "\n");
            }
        } catch (RuntimeException | IOException diagnosticError) {
            System.err.println("Could not write E2E failure diagnostics: " + safeText(diagnosticError.getMessage()));
        }
    }

    static String redirectChain(Response response) {
        List<String> chain = new ArrayList<>();
        Request request = response.request();
        while (request != null) {
            chain.add(0, "  " + safeUrl(request.url()));
            request = request.redirectedFrom();
        }
        chain.add("  -> HTTP " + response.status() + " " + safeUrl(response.url()));
        return String.join("\n", chain);
    }

    private static List<String> safeLines(List<String> messages) {
        if (messages.isEmpty()) return List.of("  none");
        return messages.stream().limit(30)
                .map(message -> "  " + safeText(message).substring(0, Math.min(MAX_LOG_CHARS, safeText(message).length())))
                .toList();
    }

    private static String safeUrl(String value) {
        if (value == null || value.isBlank()) return "unavailable";
        try {
            java.net.URI uri = java.net.URI.create(value);
            return new java.net.URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), null, null).toString();
        } catch (Exception ignored) {
            return "unavailable";
        }
    }

    private static String safeText(String value) {
        if (value == null) return "unavailable";
        return URL_QUERY.matcher(SENSITIVE_VALUE.matcher(value).replaceAll("$1$2[REDACTED]"))
                .replaceAll("$1?[REDACTED]");
    }

    private static int safeCount(com.microsoft.playwright.Locator locator) {
        try { return locator.count(); }
        catch (RuntimeException ignored) { return -1; }
    }

    private static String safeRead(java.util.function.Supplier<String> supplier) {
        try { return supplier.get(); }
        catch (RuntimeException ignored) { return "unavailable"; }
    }

    private static void write(Path path, String content) throws IOException {
        String bounded = content.length() > MAX_HTML_CHARS
                ? content.substring(0, MAX_HTML_CHARS) + "\n<!-- truncated -->\n" : content;
        Files.writeString(path, bounded, StandardCharsets.UTF_8);
    }
}
