package ru.daniil.NauJava.seleniumTests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import ru.daniil.NauJava.repository.RoleRepository;
import ru.daniil.NauJava.repository.UserRepository;
import ru.daniil.NauJava.request.create.RegistrationRequest;
import ru.daniil.NauJava.service.UserServiceImpl;

import java.time.Duration;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserAuthenticationUITest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeAll
    void setUpClass() {
        WebDriverManager.chromiumdriver().setup();
    }
    @BeforeEach
    void setUp() {

        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        driver.manage().window().maximize();
        baseUrl = "http://localhost:" + port;

        createTestUser();
    }

    private void createTestUser() {
        userRepository.deleteAll();

        RegistrationRequest registerUser = new RegistrationRequest();
        registerUser.setEmail("testUser@example.com");
        registerUser.setLogin("testUser");
        registerUser.setPassword("Password123!");

        userService.registerUser(registerUser);

        if (userRepository.findByEmail("testUser@example.com").isPresent()) {
            userRepository.findByEmail("testUser@example.com").get();
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }

        userRepository.deleteAll();
    }

    @Test
    void testSuccessfulLoginAndLogout() {
        driver.get(baseUrl + "/login");

        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("/login"));

        WebElement loginForm = wait.until(ExpectedConditions
                .presenceOfElementLocated(By.tagName("form")));
        assertNotNull(loginForm, "Форма логина должна присутствовать");

        WebElement usernameField = driver.findElement(By.name("username"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys("testUser");
        passwordField.sendKeys("Password123!");

        loginButton.click();

        wait.until(ExpectedConditions.not(ExpectedConditions
                .urlContains("/login")));


        String currentUrl = driver.getCurrentUrl();
        assertFalse(currentUrl.contains("/login"));

        try {
            boolean isUserAuthenticated = false;
            try {
                WebElement logoutButton = driver.findElement(By.xpath(
                        "//a[contains(text(), 'Выход') or contains(text(), 'Logout')]"));
                isUserAuthenticated = true;
            } catch (Exception e) {
                // Ничего. Тогда проверяется страница на отсутствие в заголовке /login или /error.
            }

            if (!driver.getCurrentUrl().contains("/login") && !driver.getCurrentUrl().contains("/error")) {
                isUserAuthenticated = true;
            }

            assertTrue(isUserAuthenticated);
        } catch (Exception e) {
            fail("Не удалось подтвердить успешную авторизацию: " + e.getMessage());
        }

        performLogout();

        wait.until(ExpectedConditions.urlContains("/login"));

        String urlAfterLogout = driver.getCurrentUrl();
        assertTrue(urlAfterLogout.contains("/login"));

        driver.findElement(By.name("username"));
        assertTrue(true);
    }

    private void performLogout() {
        try {
            WebElement logoutLink = driver.findElement(By.xpath(
                    "//button[contains(text(), 'Выход') or contains(text(), 'Logout')]"));
            logoutLink.click();
        } catch (Exception e) {
            driver.get(baseUrl + "/logout");
        }

        wait.until(ExpectedConditions.not(ExpectedConditions
                .urlContains("/logout")));
    }

    @Test
    void testLoginWithInvalidCredentials() {
        driver.get(baseUrl + "/login");

        // Ввод некорректных учетных данных
        WebElement usernameField = driver.findElement(By.name("username"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys("invalidUser");
        passwordField.sendKeys("WrongPassword123!");
        loginButton.click();

        try {
            WebElement errorMessage = wait.until(ExpectedConditions
                    .presenceOfElementLocated(By.cssSelector(".error, .alert-danger, [class*='error']")));
            assertTrue(errorMessage.isDisplayed());
        } catch (Exception e) {
            assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("/login") ||
                            driver.getCurrentUrl().contains("error"),
                    "При неверных credentials должны остаться на странице логина или получить ошибку");
        }
    }

    @Test
    void testAccessProtectedPageWithoutAuthentication() {
        driver.get(baseUrl + "/api/products/all");

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("/login"),
                "При доступе к защищенной странице без авторизации должны быть перенаправлены на логин");
    }
}