package ru.daniil.NauJava.seleniumTests;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.springframework.test.context.ActiveProfiles;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.repository.RoleRepository;
import ru.daniil.NauJava.repository.UserRepository;
import ru.daniil.NauJava.request.create.RegistrationRequest;
import ru.daniil.NauJava.service.UserProfileServiceImpl;
import ru.daniil.NauJava.service.UserServiceImpl;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRoleUITest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserProfileServiceImpl userProfileService;


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

        createTestUserWithUserRole();
    }

    private void createTestUserWithUserRole() {
        userRepository.deleteAll();
        
        RegistrationRequest registerUser = new RegistrationRequest();
        registerUser.setEmail("user@example.com");
        registerUser.setLogin("testUser");
        registerUser.setPassword("Password123!");

        User user = userService.registerUser(registerUser);
        userProfileService.createUserProfileForUser(user);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        userRepository.deleteAll();
    }

    private void loginUser() {
        driver.get(baseUrl + "/login");

        WebElement usernameField = wait.until(ExpectedConditions
                .presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys("testUser");
        passwordField.sendKeys("Password123!");
        loginButton.click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h1[contains(text(), 'Добро пожаловать')]")));
    }

    @Test
    void testUserNavigation() {
        loginUser();

        WebElement productsLink = wait.until(ExpectedConditions
                .presenceOfElementLocated(By.xpath("//a[contains(@href, '/view/products/list')]")));
        WebElement mealsLink = driver.findElement(By.xpath("//a[contains(@href, '/view/meals/list')]"));
        WebElement statsLink = driver.findElement(By.xpath("//a[contains(@href, '/view/daily-reports/stat')]"));

        assert productsLink.isDisplayed() : "Ссылка на продукты должна быть видна";
        assert mealsLink.isDisplayed() : "Ссылка на приёмы пищи должна быть видна";
        assert statsLink.isDisplayed() : "Ссылка на статистику должна быть видна";

        List<WebElement> adminLinks = driver.findElements(By.xpath(
                "//a[contains(@href, '/admin/') or contains(@href, '/swagger-ui.html')]"));
        assert adminLinks.isEmpty() : "Администраторские ссылки не должны отображаться для USER";
    }

    @Test
    void testProductsPageNavigation() {
        loginUser();

        WebElement productsLink = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("//a[contains(@href, '/view/products/list')]")));
        productsLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h1[contains(text(), 'Список продуктов')]")));

        WebElement createButton = wait.until(ExpectedConditions
                .presenceOfElementLocated(By.xpath("//a[contains(normalize-space(), 'Создать продукт')]")));
        assert createButton.isDisplayed() : "Кнопка создания продукта должна быть видна";

        createButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h1[contains(text(), 'Create New Product')]")));

        WebElement nameInput = driver.findElement(By.id("name"));
        WebElement caloriesInput = driver.findElement(By.id("caloriesPer100g"));
        WebElement submitButton = driver.findElement(By.xpath("//button[contains(text(), 'Create Product')]"));

        assert nameInput.isDisplayed() : "Поле названия должно быть видно";
        assert caloriesInput.isDisplayed() : "Поле калорий должно быть видно";
        assert submitButton.isDisplayed() : "Кнопка создания должна быть видна";
    }

    @Test
    void testMealsPageNavigation() {
        loginUser();

        // Переходим на страницу приёмов пищи
        WebElement mealsLink = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("//a[contains(@href, '/view/meals/list')]")));
        mealsLink.click();

        // Проверяем, что загрузилась страница приёмов пищи
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h1[contains(text(), 'Мои приёмы пищи')]")));

        // Проверяем кнопку создания приёма пищи
        WebElement createButton = driver.findElement(By.xpath("//button[contains(text()," +
                " 'Создать первый приём пищи')]"));
        assert createButton.isDisplayed() : "Кнопка создания приёма пищи должна быть видна";

        // Проверяем календарь
        WebElement calendarSection = driver.findElement(By.className("calendar-section"));
        assert calendarSection.isDisplayed() : "Секция календаря должна быть видна";
    }

    @Test
    void testStatisticsPageNavigation() {
        loginUser();

        // Переходим на страницу статистики
        WebElement statsLink = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("//a[contains(@href, '/view/daily-reports/stat')]")));
        statsLink.click();

        // Проверяем, что загрузилась страница анализа питания
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h1[contains(text(), 'Анализ потребления питательных веществ')]")));

        // Проверяем основные элементы
        WebElement calendarSection = driver.findElement(By.className("calendar-section"));
        WebElement chartSection = driver.findElement(By.className("chart-section"));

        assert calendarSection.isDisplayed() : "Секция календаря должна быть видна";
        assert chartSection.isDisplayed() : "Секция графика должна быть видна";
    }

    @Test
    void testAccountPageNavigation() {
        loginUser();

        // Переходим на страницу профиля через Welcome ссылку
        WebElement welcomeLink = wait.until(ExpectedConditions
                .elementToBeClickable(By.className("user-welcome-link")));
        welcomeLink.click();

        // Проверяем, что загрузилась страница профиля
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//title[contains( normalize-space(), 'Профиль пользователя')]")));

        // Проверяем вкладки
        WebElement accountTab = driver.findElement(By.xpath("//button[@data-tab='account']"));
        WebElement profileTab = driver.findElement(By.xpath("//button[@data-tab='profile']"));

        assert accountTab.isDisplayed() : "Вкладка аккаунта должна быть видна";
        assert profileTab.isDisplayed() : "Вкладка профиля должна быть видна";

        // Проверяем календарь
        WebElement calendar = driver.findElement(By.id("calendar"));
        assert calendar.isDisplayed() : "Календарь должен быть виден";
    }

    @Test
    void testAccountModalFunctionality() {
        loginUser();

        // Переходим на страницу профиля
        driver.get(baseUrl + "/view/account");

        // Кликаем на вкладку профиля
        WebElement profileTab = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("//button[@data-tab='profile']")));
        profileTab.click();

        // Кликаем на кнопку редактирования профиля
        WebElement editProfileButton = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("//button[contains(text(), 'Редактировать данные профиля')]")));
        editProfileButton.click();

        // Проверяем, что открылось модальное окно
        WebElement profileModal = wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.id("profileModal")));
        assert profileModal.isDisplayed() : "Модальное окно редактирования профиля должно быть видно";

        WebElement nameInput = driver.findElement(By.id("name"));
        WebElement surnameInput = driver.findElement(By.id("surname"));
        WebElement heightInput = driver.findElement(By.id("height"));
        WebElement saveButton = driver.findElement(By.id("profileSubmitBtn"));

        assert nameInput.isDisplayed() : "Поле имени должно быть видно";
        assert surnameInput.isDisplayed() : "Поле фамилии должно быть видно";
        assert heightInput.isDisplayed() : "Поле роста должно быть видно";
        assert saveButton.isDisplayed() : "Кнопка сохранения должна быть видна";

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("profileModal")));

        // Прокручиваем к кнопке закрытия
        WebElement closeButton = modal.findElement(By.className("btn-close"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", closeButton);

        // Даем время для прокрутки
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Кликаем через JavaScript (обход проблем с видимостью)
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeButton);

        wait.until(ExpectedConditions.invisibilityOf(profileModal));
    }

    @Test
    void testLogoutFunctionality() {
        loginUser();
        WebElement logoutButton = wait.until(ExpectedConditions
                .elementToBeClickable(By.cssSelector("button.btn-logout")));
        logoutButton.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        assert Objects.requireNonNull(driver.getCurrentUrl()).contains("/login") : "После выхода должны быть на странице логина";

        WebElement loginForm = driver.findElement(By.tagName("form"));
        assert loginForm.isDisplayed() : "Форма логина должна быть видна после выхода";
    }
}
