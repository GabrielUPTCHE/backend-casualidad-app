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
    // .\mvnw.cmd -Dtest=LoginSeleniumTest test
    private static final String LOGIN_URL = System.getProperty(
        "app.baseUrl",
        "https://main.d2b2pi5dpwpebm.amplifyapp.com/login"
    );
    private static final String APP_BASE_URL = LOGIN_URL.replace("/login", "");

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Para ejecutar en modo headless (sin abrir la ventana del navegador), se descomenta la siguiente línea:
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
    @DisplayName("Login exitoso debe mostrar dashboard, datos de sesión y accesos a módulos")
    void loginExitosoDebeMostrarDashboardSesionYAccesos() {
        completarFormulario("alejandro123@yopmail.com", "123456789");
        esperarEnvioDelLogin();

        wait.until(urlContains("/home"));
        assertTrue(driver.getCurrentUrl().contains("/home"));

        assertDashboardVisible();
        assertDatosDeSesionVisibles();
        assertAccesosAModulos();
    }

    @Test
    @DisplayName("Login fallido por correo debe mostrar error")
    void loginFallidoPorCorreoDebeMostrarError() {
        completarFormulario("noexiste@yopmaiñ", "cualquiera");
        esperarEnvioDelLogin();

        assertTrue(driver.getCurrentUrl().contains("/login"));
        assertErrorVisible();
    }

    @Test
    @DisplayName("Login fallido por formato de correo debe mostrar aviso de formato inválido")
    void loginFallidoPorFormatoCorreoDebeMostrarAviso() {
        completarFormulario("alejandro123yopmail.com", "123456789");

        assertTrue(driver.getCurrentUrl().contains("/login"));
        assertFormatoCorreoInvalidoVisible();
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

    private void assertDashboardVisible() {
        wait.until(visibilityOfElementLocated(By.xpath("//h3[normalize-space()='Rendimiento Financiero']")));
        wait.until(visibilityOfElementLocated(By.xpath("//a[.//p[normalize-space()='Por Entregar'] or normalize-space()='Por Entregar']")));
        wait.until(visibilityOfElementLocated(By.xpath("//a[.//p[normalize-space()='Con Deuda'] or normalize-space()='Con Deuda']")));
        wait.until(visibilityOfElementLocated(By.xpath("//h3[normalize-space()='Rendimiento Financiero']")));
    }

    private void assertDatosDeSesionVisibles() {
        WebElement nombreUsuario = wait.until(visibilityOfElementLocated(By.xpath("//p[normalize-space()='Alejandro']")));
        WebElement correoUsuario = wait.until(visibilityOfElementLocated(By.xpath("//p[normalize-space()='alejandro123@yopmail.com']")));

        assertEquals("Alejandro", nombreUsuario.getText());
        assertEquals("alejandro123@yopmail.com", correoUsuario.getText());
    }

    private void assertAccesosAModulos() {
        assertModuloVisible("Inicio");
        verificarAccesoDirectoAModulo("/clientes");
        verificarAccesoDirectoAModulo("/inventario");
        verificarAccesoDirectoAModulo("/pedidos");
        verificarAccesoDirectoAModulo("/pagos");
        verificarAccesoDirectoAModulo("/reportes");
    }

    private void assertModuloVisible(String nombreModulo) {
        wait.until(visibilityOfElementLocated(By.xpath("//a[contains(normalize-space(.), '" + nombreModulo + "')]")));
    }

    private void verificarAccesoDirectoAModulo(String rutaEsperada) {
        driver.get(APP_BASE_URL + rutaEsperada);
        wait.until(urlContains(rutaEsperada));
        assertTrue(driver.getCurrentUrl().contains(rutaEsperada));
    }

    private void assertFormatoCorreoInvalidoVisible() {
        WebElement avisoFormato = wait.until(visibilityOfElementLocated(By.xpath(
            "//*[normalize-space()='Por favor, ingresa un correo electrónico válido (ejemplo@dominio.com)']"
        )));
        assertEquals(
            "Por favor, ingresa un correo electrónico válido (ejemplo@dominio.com)",
            avisoFormato.getText()
        );
    }
}
