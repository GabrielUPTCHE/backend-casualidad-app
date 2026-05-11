package com.casualidad.casualidad_backend.auth.e2e;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.urlContains;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

class LoginSeleniumTest {
    // .\mvnw.cmd -Dtest=LoginSeleniumTest test
    private static final String LOGIN_URL = System.getProperty(
            "app.baseUrl",
            "https://main.d2b2pi5dpwpebm.amplifyapp.com/login");
    private static final String APP_BASE_URL = LOGIN_URL.replace("/login", "");

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String MOCK_INACTIVITY_TIMERS_SCRIPT = """
            (() => {
                const originalSetTimeout = window.setTimeout.bind(window);
                const originalSetInterval = window.setInterval.bind(window);
                const originalDateNow = Date.now.bind(Date);
                const fastForwardMs = 6 * 60 * 1000;

                window.setTimeout = (handler, delay = 0, ...args) => {
                    const normalizedDelay = typeof delay === 'number' && delay >= 1000 ? 1000 : delay;
                    return originalSetTimeout(handler, normalizedDelay, ...args);
                };

                window.setInterval = (handler, delay = 0, ...args) => {
                    const normalizedDelay = typeof delay === 'number' && delay >= 1000 ? 1000 : delay;
                    return originalSetInterval(handler, normalizedDelay, ...args);
                };

                Date.now = () => originalDateNow() + fastForwardMs;
            })();
            """;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Para abrir la ventana del navegador),
        // commentar la siguiente línea:
        // options.addArguments("--headless=new");
        options.addArguments("--window-size=1440,1200");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        habilitarMockDeTiempo();
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
        assertAccesosAModulos();
    }

    @Test
    @DisplayName("Editar perfil con nombre inválido debe mostrar validación")
    void editarPerfilConNombreInvalidoDebeMostrarValidacion() {
        completarFormulario("alejandro123@yopmail.com", "123456789");
        esperarEnvioDelLogin();

        wait.until(urlContains("/home"));
        abrirPerfilDesdeMenu();

        WebElement nombreInput = wait
                .until(visibilityOfElementLocated(By.cssSelector("input[formcontrolname='nombre']")));
        if (driver instanceof JavascriptExecutor js) {
            js.executeScript(
                    "const input = arguments[0];"
                            + "const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;"
                            + "setter.call(input, 'Juan@#');"
                            + "input.dispatchEvent(new Event('input', { bubbles: true }));"
                            + "input.dispatchEvent(new Event('blur', { bubbles: true }));",
                    nombreInput);
        } else {
            nombreInput.clear();
            nombreInput.sendKeys("Juan@#", Keys.TAB);
        }

        WebElement mensajeValidacion = wait.until(
                visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(.), 'Verifica los nombres')]")));
        assertTrue(mensajeValidacion.getText().contains("Verifica los nombres (solo letras, máx. 50)"));
        assertTrue(wait.until(visibilityOfElementLocated(By.xpath("//button[normalize-space()='Aceptar']")))
                .getDomProperty("disabled") != null);
    }

    @Test
    @DisplayName("Editar perfil con teléfono inválido debe mostrar validación")
    void editarPerfilConTelefonoInvalidoDebeMostrarValidacion() {
        completarFormulario("alejandro123@yopmail.com", "123456789");
        esperarEnvioDelLogin();

        wait.until(urlContains("/home"));
        abrirPerfilDesdeMenu();

        WebElement telefonoInput = wait
                .until(visibilityOfElementLocated(By.cssSelector("input[formcontrolname='telefono']")));
        if (driver instanceof JavascriptExecutor js) {
            js.executeScript(
                    "const input = arguments[0];"
                            + "const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;"
                            + "setter.call(input, '12345678');"
                            + "input.dispatchEvent(new Event('input', { bubbles: true }));"
                            + "input.dispatchEvent(new Event('blur', { bubbles: true }));",
                    telefonoInput);
        } else {
            telefonoInput.clear();
            telefonoInput.sendKeys("12345678", Keys.TAB);
        }

        WebElement mensajeValidacion = wait.until(visibilityOfElementLocated(
                By.xpath("//p[contains(normalize-space(.), 'El teléfono debe tener 10 dígitos.')]")));
        assertTrue(mensajeValidacion.getText().contains("El teléfono debe tener 10 dígitos."));
        assertTrue(wait.until(visibilityOfElementLocated(By.xpath("//button[normalize-space()='Aceptar']")))
                .getDomProperty("disabled") != null);
    }

        @Test
        @DisplayName("Agregar cliente con datos válidos debe registrar exitosamente y cerrar la ventana")
        void agregarClienteConDatosValidosDebeRegistrarExitosamenteYCerrarVentana() {
        completarFormulario("alejandro123@yopmail.com", "123456789");
        esperarEnvioDelLogin();

        wait.until(urlContains("/home"));
        abrirModuloClientes();
        abrirFormularioNuevoCliente();

            String telefonoUnico = generarTelefonoClienteUnico();
            completarClienteConEventos("Juan Pérez", telefonoUnico);

        WebElement registrarButton = wait.until(
            elementToBeClickable(By.xpath("//button[normalize-space()='Registrar Cliente']")));
        if (driver instanceof JavascriptExecutor js) {
            js.executeScript("arguments[0].click();", registrarButton);
        } else {
            registrarButton.click();
        }

        WebElement cerrarVentanaButton = wait.until(
            elementToBeClickable(By.xpath("//button[normalize-space()='Cerrar ventana']")));
        cerrarVentanaButton.click();
        }

    @Test
    @DisplayName("Registro de cliente sin nombre debe bloquear el envío del formulario")
    void registroDeClienteSinNombreDebeBloquearElEnvioDelFormulario() {
        completarFormulario("alejandro123@yopmail.com", "123456789");
        esperarEnvioDelLogin();

        wait.until(urlContains("/home"));
        abrirModuloClientes();
        abrirFormularioNuevoCliente();

        completarClienteConEventos("", "3001234567");

        WebElement registrarButton = wait.until(
                visibilityOfElementLocated(By.xpath("//button[normalize-space()='Registrar Cliente']")));
        assertTrue(registrarButton.getDomProperty("disabled") != null || !registrarButton.isEnabled());
    }

    @Test
    @DisplayName("Login con inactividad debe mostrar popup y permitir cerrar sesión")
    void loginConInactividadDebeMostrarPopupYCerrarSesion() {
        completarFormulario("alejandro123@yopmail.com", "123456789");
        esperarEnvioDelLogin();

        wait.until(urlContains("/home"));
        assertTrue(driver.getCurrentUrl().contains("/home"));

        esperarPopupDeInactividad();
        cerrarSesionDesdePopup();

        wait.until(urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"));
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
        WebElement errorTitulo = wait
                .until(visibilityOfElementLocated(By.xpath("//h2[normalize-space()='Credenciales inválidas']")));
        assertEquals("Credenciales inválidas", errorTitulo.getText());
        wait.until(visibilityOfElementLocated(By.xpath("//button[normalize-space()='Intentar de nuevo']")));
    }

    private void assertDashboardVisible() {
        wait.until(visibilityOfElementLocated(By.xpath("//h3[normalize-space()='Rendimiento Financiero']")));
        wait.until(visibilityOfElementLocated(
                By.xpath("//a[.//p[normalize-space()='Por Entregar'] or normalize-space()='Por Entregar']")));
        wait.until(visibilityOfElementLocated(
                By.xpath("//a[.//p[normalize-space()='Con Deuda'] or normalize-space()='Con Deuda']")));
        wait.until(visibilityOfElementLocated(By.xpath("//h3[normalize-space()='Rendimiento Financiero']")));
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
                "//*[normalize-space()='Por favor, ingresa un correo electrónico válido (ejemplo@dominio.com)']")));
        assertEquals(
                "Por favor, ingresa un correo electrónico válido (ejemplo@dominio.com)",
                avisoFormato.getText());
    }

    private void abrirPerfilDesdeMenu() {
        WebElement profileMenuButton = wait
                .until(visibilityOfElementLocated(By.cssSelector("button[aria-label='Menú de perfil']")));
        if (driver instanceof JavascriptExecutor js) {
            js.executeScript("arguments[0].click();", profileMenuButton);
        } else {
            profileMenuButton.click();
        }

        wait.until(visibilityOfElementLocated(By.cssSelector("button[role='menuitem']")));

        if (driver instanceof JavascriptExecutor js) {
            js.executeScript(
                    "const items = Array.from(document.querySelectorAll(\"button, a\"));"
                            + "const miPerfil = items.find(item => item.textContent && item.textContent.includes('Mi Perfil'));"
                            + "if (!miPerfil) throw new Error('No se encontró la opción Mi Perfil');"
                            + "miPerfil.click();");
        } else {
            WebElement miPerfil = wait.until(visibilityOfElementLocated(
                    By.xpath("//button[@role='menuitem' and contains(normalize-space(.), 'Mi Perfil')]")));
            miPerfil.click();
        }

        wait.until(urlContains("/perfil"));
        assertTrue(driver.getCurrentUrl().contains("/perfil"));
    }

    private void habilitarMockDeTiempo() {
        if (driver instanceof ChromeDriver chromeDriver) {
            chromeDriver.executeCdpCommand(
                    "Page.addScriptToEvaluateOnNewDocument",
                    Map.of("source", MOCK_INACTIVITY_TIMERS_SCRIPT));
        }
    }

    private void esperarPopupDeInactividad() {
        wait.until(visibilityOfElementLocated(By.xpath(
                "//*[contains(normalize-space(.), 'inactividad') or contains(normalize-space(.), 'Inactividad') or contains(normalize-space(.), 'inactivo') or contains(normalize-space(.), 'Inactivo')]")));
        wait.until(visibilityOfElementLocated(By.xpath("//button[normalize-space()='Cerrar sesión']")));
    }

    private void cerrarSesionDesdePopup() {
        wait.until(elementToBeClickable(By.xpath("//button[normalize-space()='Cerrar sesión']"))).click();
    }

    @Test
    @DisplayName("Logout manual desde perfil debe cerrar sesión y redirigir a login")
    void logoutManualDesdePerfilDebeCerrarSesionYRedirigirALogin() {
        completarFormulario("alejandro123@yopmail.com", "123456789");
        esperarEnvioDelLogin();

        wait.until(urlContains("/home"));
        assertTrue(driver.getCurrentUrl().contains("/home"));

        // Abrir el menú de perfil con el botón real del encabezado
        WebElement profileMenuButton = wait
                .until(visibilityOfElementLocated(By.cssSelector("button[aria-label='Menú de perfil']")));
        if (driver instanceof JavascriptExecutor js) {
            js.executeScript("arguments[0].click();", profileMenuButton);
        } else {
            profileMenuButton.click();
        }

        // Seleccionar la opción de cierre de sesión visible en el menú desplegable
        WebElement logoutMenuItem = wait.until(visibilityOfElementLocated(By.cssSelector("button[role='menuitem']")));
        if (driver instanceof JavascriptExecutor js) {
            js.executeScript("arguments[0].click();", logoutMenuItem);
        } else {
            logoutMenuItem.click();
        }

        // Esperar el diálogo de confirmación y confirmar
        wait.until(visibilityOfElementLocated(By.xpath("//h2[normalize-space()='¿Cerrar sesión?']")));
        WebElement confirmarCierre = wait.until(
                visibilityOfElementLocated(By.xpath("//button[contains(normalize-space(.), 'Sí, cerrar sesión')]")));
        if (driver instanceof JavascriptExecutor js) {
            js.executeScript("arguments[0].click();", confirmarCierre);
        } else {
            confirmarCierre.click();
        }

        // Verificar que redirige a login y que los inputs de login son visibles
        wait.until(urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"));
        wait.until(visibilityOfElementLocated(By.cssSelector("input[type='email']")));
    }

    @Test
    @DisplayName("Editar perfil con datos válidos debe mostrar éxito y poder restaurar")
    void editarPerfilConDatosValidosDebeMostrarExito() {
        completarFormulario("alejandro123@yopmail.com", "123456789");
        esperarEnvioDelLogin();
        abrirPerfilDesdeMenu();

        // Actualizar a datos nuevos: Carlos, Gómez, 3104567890
        actualizarDatosFormularioConEventos("nombre", "Carlos");
        actualizarDatosFormularioConEventos("apellidos", "Gómez");
        actualizarDatosFormularioConEventos("telefono", "3104567890");

        // Guardar cambios
        WebElement aceptarButton = wait
                .until(visibilityOfElementLocated(By.xpath("//button[contains(normalize-space(.), 'Aceptar')]")));
        aceptarButton.click();

        // Esperar a que el botón salga del estado de carga (máximo 10 segundos)
        wait.until(driver1 -> {
            WebElement btn = driver1.findElement(By.xpath("//button[contains(normalize-space(.), 'Aceptar')]"));
            String text = btn.getText();
            return !text.contains("progress_activity");
        });

        // Verificar que no hay errores de validación visibles
        String nombre1 = driver.findElement(
                By.cssSelector("input[formcontrolname='nombre']")).getDomProperty("value");

        String apellidos1 = driver.findElement(
                By.cssSelector("input[formcontrolname='apellidos']")).getDomProperty("value");

        String telefono1 = driver.findElement(
                By.cssSelector("input[formcontrolname='telefono']")).getDomProperty("value");

        assertEquals("Carlos", nombre1, "El nombre debe ser 'Carlos' después de guardar");
        assertEquals("Gómez", apellidos1, "Los apellidos deben ser 'Gómez' después de guardar");
        assertEquals("3104567890", telefono1, "El teléfono debe ser '3104567890' después de guardar");

        // Restaurar a datos originales: Alejandro, Perez, 3001234567
        actualizarDatosFormularioConEventos("nombre", "Alejandro");
        actualizarDatosFormularioConEventos("apellidos", "Perez");
        actualizarDatosFormularioConEventos("telefono", "3001234567");

        // Guardar cambios de restauración
        aceptarButton = wait
                .until(visibilityOfElementLocated(By.xpath("//button[contains(normalize-space(.), 'Aceptar')]")));
        aceptarButton.click();

        // Esperar a que el botón salga del estado de carga
        wait.until(driver1 -> {
            WebElement btn = driver1.findElement(By.xpath("//button[contains(normalize-space(.), 'Aceptar')]"));
            String text = btn.getText();
            return !text.contains("progress_activity");
        });

        // Verificar que se restauraron correctamente
        String nombre2 = driver.findElement(By.cssSelector("input[formcontrolname='nombre']")).getDomProperty("value");
        String apellidos2 = driver.findElement(By.cssSelector("input[formcontrolname='apellidos']"))
                .getDomProperty("value");
        String telefono2 = driver.findElement(By.cssSelector("input[formcontrolname='telefono']"))
                .getDomProperty("value");

        assertEquals("Alejandro", nombre2, "El nombre debe ser restaurado a 'Alejandro'");
        assertEquals("Perez", apellidos2, "Los apellidos deben ser restaurados a 'Perez'");
        assertEquals("3001234567", telefono2, "El teléfono debe ser restaurado a '3001234567'");
    }

    private void actualizarDatosFormularioConEventos(String fieldName, String value) {
        WebElement input = wait
                .until(visibilityOfElementLocated(By.cssSelector("input[formcontrolname='" + fieldName + "']")));
        if (driver instanceof JavascriptExecutor js) {
            js.executeScript(
                    "const input = arguments[0];"
                            + "const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;"
                            + "setter.call(input, arguments[1]);"
                            + "input.dispatchEvent(new Event('input', { bubbles: true }));"
                            + "input.dispatchEvent(new Event('blur', { bubbles: true }));",
                    input, value);
        }
    }

    private void abrirModuloClientes() {
        WebElement clientesLink = wait.until(
                visibilityOfElementLocated(By.xpath("//a[contains(normalize-space(.), 'Clientes')]") ));
        if (driver instanceof JavascriptExecutor js) {
            js.executeScript("arguments[0].click();", clientesLink);
        } else {
            clientesLink.click();
        }

        wait.until(urlContains("/clientes"));
        assertTrue(driver.getCurrentUrl().contains("/clientes"));
    }

    private void abrirFormularioNuevoCliente() {
        WebElement botonAñadirCliente = wait.until(
                visibilityOfElementLocated(By.xpath("//button[contains(normalize-space(.), 'Añadir Cliente') or contains(normalize-space(.), 'Comenzar registro')]")));
        if (driver instanceof JavascriptExecutor js) {
            js.executeScript("arguments[0].click();", botonAñadirCliente);
        } else {
            botonAñadirCliente.click();
        }

        wait.until(visibilityOfElementLocated(By.xpath("//h2[normalize-space()='Nuevo registro de Cliente']")));
    }

    private void completarClienteConEventos(String nombre, String telefono) {
        WebElement nombreInput = wait.until(
                visibilityOfElementLocated(By.cssSelector("input[formcontrolname='name']")));
        WebElement telefonoInput = wait.until(
                visibilityOfElementLocated(By.cssSelector("input[type='tel']")));

        if (driver instanceof JavascriptExecutor js) {
            js.executeScript(
                    "const input = arguments[0];"
                            + "const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;"
                            + "setter.call(input, arguments[1]);"
                            + "input.dispatchEvent(new Event('input', { bubbles: true }));"
                            + "input.dispatchEvent(new Event('blur', { bubbles: true }));",
                    nombreInput, nombre);

            js.executeScript(
                    "const input = arguments[0];"
                            + "const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;"
                            + "setter.call(input, arguments[1]);"
                            + "input.dispatchEvent(new Event('input', { bubbles: true }));"
                            + "input.dispatchEvent(new Event('blur', { bubbles: true }));",
                    telefonoInput, telefono);
        } else {
            nombreInput.clear();
            nombreInput.sendKeys(nombre, Keys.TAB);
            telefonoInput.clear();
            telefonoInput.sendKeys(telefono, Keys.TAB);
        }
    }

    private String generarTelefonoClienteUnico() {
        long sufijo = Math.abs(System.currentTimeMillis() % 10_000_000L);
        return String.format("300%07d", sufijo);
    }
}
