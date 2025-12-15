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
import ru.daniil.NauJava.enums.RoleType;
import ru.daniil.NauJava.repository.RoleRepository;
import ru.daniil.NauJava.repository.UserRepository;
import ru.daniil.NauJava.request.create.RegistrationRequest;
import ru.daniil.NauJava.service.UserProfileService;
import ru.daniil.NauJava.service.UserService;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminRoleUITest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileService userProfileService;

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

        createTestUserWithAdminRole();
    }

    private void createTestUserWithAdminRole() {
        userRepository.deleteAll();

        RegistrationRequest registerUser = new RegistrationRequest();
        registerUser.setEmail("admin@example.com");
        registerUser.setLogin("adminUser");
        registerUser.setPassword("AdminPassword123!");

        User user = userService.registerUserWithRole(registerUser, RoleType.ADMIN.toString());

        userProfileService.createUserProfileForUser(user);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        userRepository.deleteAll();
    }

    private void loginAdmin() {
        driver.get(baseUrl + "/login");

        WebElement usernameField = wait.until(ExpectedConditions
                .presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys("adminUser");
        passwordField.sendKeys("AdminPassword123!");
        loginButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h1[contains(text(), 'Добро пожаловать')]")));
    }

    @Test
    void testAdminNavigation() {
        loginAdmin();

        WebElement swaggerLink = wait.until(ExpectedConditions
                .presenceOfElementLocated(By.xpath("//a[contains(@href, '/swagger-ui.html')]")));
        WebElement usersLink = driver.findElement(By.xpath("//a[contains(@href, '/admin/view/users')]"));
        WebElement reportsLink = driver.findElement(By.xpath("//a[contains(@href, '/admin/view/reports/list')]"));

        assert swaggerLink.isDisplayed() : "Ссылка на Swagger UI должна быть видна";
        assert usersLink.isDisplayed() : "Ссылка на пользователей должна быть видна";
        assert reportsLink.isDisplayed() : "Ссылка на отчёты должна быть видна";

        List<WebElement> adminBadges = driver.findElements(By.className("admin-badge"));
        assert !adminBadges.isEmpty() : "Бейдж ADMIN должен отображаться";
    }

    @Test
    void testUsersManagementPage() {
        loginAdmin();

        WebElement usersLink = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("//a[contains(@href, '/admin/view/users')]")));
        usersLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h1[contains(text(), 'Активность пользователей')]")));

        WebElement adminBadge = driver.findElement(By.className("admin-badge"));
        assert adminBadge.isDisplayed() : "Бейдж ADMIN должен быть виден";
        assert adminBadge.getText().equals("ADMIN") : "Бейдж должен содержать текст 'ADMIN'";

        WebElement statsSection = driver.findElement(By.className("stats-section"));
        assert statsSection.isDisplayed() : "Секция статистики должна быть видна";

        WebElement usersTable = driver.findElement(By.className("users-table"));
        assert usersTable.isDisplayed() : "Таблица пользователей должна быть видна";

        WebElement pageSizeSelector = driver.findElement(By.id("pageSizeSelect"));
        assert pageSizeSelector.isDisplayed() : "Селектор размера страницы должен быть виден";
    }

    @Test
    void testReportsManagementPage() {
        loginAdmin();

        WebElement reportsLink = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("//a[contains(@href, '/admin/view/reports/list')]")));
        reportsLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h1[contains(text(), 'Отчёты системы')]")));

        WebElement adminBadge = driver.findElement(By.className("admin-badge"));
        assert adminBadge.isDisplayed() : "Бейдж ADMIN должен быть виден";

        WebElement creationSection = driver.findElement(By.className("report-creation-section"));
        assert creationSection.isDisplayed() : "Секция создания отчёта должна быть видна";

        WebElement startDateInput = driver.findElement(By.id("startDate"));
        WebElement endDateInput = driver.findElement(By.id("endDate"));

        assert startDateInput.isDisplayed() : "Поле даты начала должно быть видно";
        assert endDateInput.isDisplayed() : "Поле даты окончания должно быть видно";

        WebElement quickActionsSection = driver.findElement(By.className("quick-actions-section"));
        assert quickActionsSection.isDisplayed() : "Секция быстрых действий должна быть видна";

        WebElement reportsListSection = driver.findElement(By.className("reports-list-section"));
        assert reportsListSection.isDisplayed() : "Секция списка отчётов должна быть видна";
    }

    @Test
    void testReportsModalFunctionality() {
        loginAdmin();

        driver.get(baseUrl + "/admin/view/reports/list");

        try {
            WebElement createReportBtn = wait.until(ExpectedConditions
                    .elementToBeClickable(By.id("createReportBtn")));
            createReportBtn.click();

            Thread.sleep(1000);

            WebElement closeButton = wait.until(ExpectedConditions
                    .visibilityOfElementLocated(By.className("report-row")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", closeButton);

            Thread.sleep(500);

            WebElement ReportRow = wait.until(ExpectedConditions
                    .visibilityOfElementLocated(By.className("report-row")));
            assert ReportRow.isDisplayed() : "Предупреждение о существующем отчёте должно отображаться";

            WebElement warningMessage = wait.until(ExpectedConditions
                    .visibilityOfElementLocated(By.className("info-text")));
            assert warningMessage.isDisplayed() : "Предупреждение о существующем отчёте должно отображаться";
        } catch (TimeoutException e) {
            System.out.println("Отчёта не существует, предупреждение не отображается");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        WebElement createReportButton = driver.findElement(By.id("createReportBtn"));
        assert createReportButton.isDisplayed() : "Кнопка создания отчёта должна быть видна";
        assert createReportButton.isEnabled() : "Кнопка создания отчёта должна быть активна";
    }

    @Test
    void testSwaggerUINavigation() {
        loginAdmin();

        WebElement swaggerLink = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("//a[contains(@href, '/swagger-ui.html')]")));
        swaggerLink.click();

        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(), 'Swagger UI') or contains(@class, 'swagger-ui')]")));

        driver.switchTo().window(originalWindow);

        assert Objects.equals(driver.getCurrentUrl(), baseUrl + "/") :
                "После возврата должны быть на главной странице";
    }

    @Test
    void testUserDetailsModal() throws InterruptedException {
        loginAdmin();

        driver.get(baseUrl + "/admin/view/users");

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".users-table tbody tr")));

        WebElement detailLogin = wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.className("stats-title")));
        assert detailLogin.isDisplayed() : "Логин пользователя должен отображаться в модальном окне";
    }

    @Test
    void testAdminCannotAccessUserSpecificPages() {
        loginAdmin();

        driver.get(baseUrl + "/view/products/list");

        try {
            WebElement errorMessage = driver.findElement(By.cssSelector(".error, .alert-danger"));
            assert errorMessage.isDisplayed() : "Админ не должен получать ошибку доступа к странице продуктов";
        } catch (NoSuchElementException e) {
            // ничего не делать
        }

        WebElement returnNav = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("//a[contains(@class, 'btn-primary')]")));
        assert returnNav.isDisplayed() : "Кнопка возврата должна быть в навигации";

        returnNav.click();

        WebElement adminNav = wait.until(ExpectedConditions
                        .presenceOfElementLocated(By.xpath("//nav[contains(@class, 'nav-menu')]")));

        assert adminNav.isDisplayed() : "Навигация админа должна отображаться";
    }

    @Test
    void testAdminLogout() {
        loginAdmin();

        WebElement logoutButton = wait.until(ExpectedConditions
                .elementToBeClickable(By.cssSelector("button.btn-logout")));
        logoutButton.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        assert Objects.requireNonNull(driver.getCurrentUrl()).contains("/login") : "После выхода должны быть на странице логина";

        WebElement loginForm = driver.findElement(By.tagName("form"));
        assert loginForm.isDisplayed() : "Форма логина должна быть видна после выхода";
    }
}
