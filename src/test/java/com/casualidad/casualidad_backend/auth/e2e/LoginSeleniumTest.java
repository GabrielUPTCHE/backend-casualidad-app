package com.casualidad.casualidad_backend.auth.e2e;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.urlContains;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

class LoginSeleniumTest {

    private static final String LOGIN_URL = System.getProperty(
        "app.baseUrl",
        "https://main.d2b2pi5dpwpebm.amplifyapp.com/login"
    );

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1440,1200");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.get(LOGIN_URL);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Login exitoso debe redirigir a home")
    void loginExitosoDebeRedirigirAHome() {
        completarFormulario("alejandro123@yopmail.com", "123456789");
        esperarEnvioDelLogin();

        wait.until(urlContains("/home"));
        assertTrue(driver.getCurrentUrl().contains("/home"));
        wait.until(visibilityOfElementLocated(By.xpath("//h3[normalize-space()='Rendimiento Financiero']")));
    }

    @Test
    @DisplayName("Login fallido por correo debe mostrar error")
    void loginFallidoPorCorreoDebeMostrarError() {
        completarFormulario("correo-inexistente@yopmail.com", "123456789");
        esperarEnvioDelLogin();

        assertTrue(driver.getCurrentUrl().contains("/login"));
        assertErrorVisible();
    }

    @Test
    @DisplayName("Login fallido por contraseña debe mostrar error")
    void loginFallidoPorContrasenaDebeMostrarError() {
        completarFormulario("alejandro123@yopmail.com", "contrasena-incorrecta");
        esperarEnvioDelLogin();

        assertTrue(driver.getCurrentUrl().contains("/login"));
        assertErrorVisible();
    }

    private void completarFormulario(String correo, String contrasena) {
        WebElement correoInput = wait.until(visibilityOfElementLocated(By.cssSelector("input[type='email']")));
        WebElement contrasenaInput = wait.until(visibilityOfElementLocated(By.cssSelector("input[type='password']")));

        correoInput.clear();
        correoInput.sendKeys(correo);
        contrasenaInput.clear();
        contrasenaInput.sendKeys(contrasena);
    }

    private void esperarEnvioDelLogin() {
        wait.until(elementToBeClickable(By.xpath("//button[contains(.,'Entrar')]"))).click();
    }

    private void assertErrorVisible() {
        WebElement errorTitulo = wait.until(visibilityOfElementLocated(By.xpath("//h2[normalize-space()='Credenciales inválidas']")));
        assertEquals("Credenciales inválidas", errorTitulo.getText());
        wait.until(visibilityOfElementLocated(By.xpath("//button[normalize-space()='Intentar de nuevo']")));
    }
}
