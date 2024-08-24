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

public class GoogleSearchTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        // Configura las opciones del navegador
        EdgeOptions options = new EdgeOptions();
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
    public void testGoogleSearchTitle() {
        // Navegar a Google
        driver.get("https://www.google.com");
        
        // Encontrar la barra de búsqueda por su nombre
        WebElement searchBox = driver.findElement(By.name("q"));
        // Escribir "Selenium WebDriver"
        searchBox.sendKeys("Selenium WebDriver");
        // Enviar la búsqueda (equivalente a presionar Enter)
        searchBox.submit();

        // Esperar a que el título de la página contenga el texto esperado
        wait.until(ExpectedConditions.titleContains("Selenium WebDriver"));

        // Validar el título de la página
        String title = driver.getTitle();
        boolean titleContainsText = title.contains("Selenium WebDriver");
        assertTrue(titleContainsText, "Test Failed: El título de la página no contiene 'Selenium WebDriver'. Título actual: " + title);
        
        // Imprimir mensaje si el test pasa
        System.out.println("Test Passed");
    }

    @Test
    public void testGoogleSearchResults() {
        // Navegar a Google
        driver.get("https://www.google.com");
        
        // Encontrar la barra de búsqueda por su nombre
        WebElement searchBox = driver.findElement(By.name("q"));
        // Escribir "Selenium WebDriver"
        searchBox.sendKeys("Selenium WebDriver");
        // Enviar la búsqueda (equivalente a presionar Enter)
        searchBox.submit();

        // Esperar a que los resultados de búsqueda estén visibles
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("search")));

        // Verificar que los resultados de búsqueda están visibles
        WebElement searchResults = driver.findElement(By.id("search"));
        boolean resultsDisplayed = searchResults.isDisplayed();
        assertTrue(resultsDisplayed, "Test Failed: No se encontraron resultados de búsqueda.");

        // Verificar que al menos un resultado contiene el texto esperado
        boolean resultContainsText = false;
        for (WebElement result : searchResults.findElements(By.className("g"))) {
            if (result.getText().contains("Selenium WebDriver")) {
                resultContainsText = true;
                break;
            }
        }
        assertTrue(resultContainsText, "Test Failed: Ninguno de los resultados de búsqueda contiene 'Selenium WebDriver'.");

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
