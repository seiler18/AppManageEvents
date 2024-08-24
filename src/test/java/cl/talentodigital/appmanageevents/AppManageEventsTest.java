package cl.talentodigital.appmanageevents;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppManageEventsTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        // Configura las opciones del navegador
        EdgeOptions options = new EdgeOptions();
         // Agrega argumentos para ejecutar el navegador en modo headless
         options.addArguments("--headless");
         options.addArguments("--disable-gpu"); // Opcional, pero puede ayudar a evitar problemas en algunos entornos
         options.addArguments("--no-sandbox"); // Opcional, puede ser necesario en algunos entornos de CI/CD
         options.addArguments("--disable-dev-shm-usage"); // Opcional, puede ayudar a evitar problemas de recursos en entornos limitados
        // Establece la propiedad para el WebDriver de Microsoft Edge
        System.setProperty("webdriver.edge.driver", "/usr/bin/msedgedriver");
        // Inicia el WebDriver
        driver = new EdgeDriver(options);
        // Configura WebDriverWait con un tiempo de espera de 10 segundos
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    public void testEventListPage() {
        // Navegar a la URL de la aplicación
        driver.get("https://appmanageevents-6fe8c2902944.herokuapp.com/api/eventos");
        
        // Esperar a que la página cargue el contenido
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));

        // Obtener el contenido de la página
        WebElement body = driver.findElement(By.tagName("body"));
        String pageContent = body.getText();
        
        // Validar que el contenido de la página contiene información esperada
        // Verificar que al menos uno de los eventos está presente en el texto de la página
        assertTrue(pageContent.contains("Conferencia Tech"), "Test Failed: 'Conferencia Tech' no está presente en la página.");
        assertTrue(pageContent.contains("Feria de Empleo"), "Test Failed: 'Feria de Empleo' no está presente en la página.");
        assertTrue(pageContent.contains("Congreso Médico"), "Test Failed: 'Congreso Médico' no está presente en la página.");
        assertTrue(pageContent.contains("Reunión Anual"), "Test Failed: 'Reunión Anual' no está presente en la página.");
        assertTrue(pageContent.contains("Seminario de Educación"), "Test Failed: 'Seminario de Educación' no está presente en la página.");

        // Imprimir mensaje si el test pasa
        System.out.println("Test Passed");
    }

    @AfterEach
    public void tearDown() {
        // Cerrar el navegador
        if (driver != null) {
            driver.quit();
        }
    }
}
