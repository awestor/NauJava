package ru.daniil.NauJava;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.repository.ProductRepository;
import ru.daniil.NauJava.repository.UserRepository;
import ru.daniil.NauJava.request.create.RegistrationRequest;
import ru.daniil.NauJava.service.UserServiceImpl;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductApiControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;

    private User testUser;
    private Product systemProduct1;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        productRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        RegistrationRequest registerUser = new RegistrationRequest();
        registerUser.setEmail("test@example.com");
        registerUser.setLogin("testuser");
        registerUser.setPassword("Password123!");
        //registerUser.setName("John");
        //registerUser.setSurname("Doe");
        userService.registerUser(registerUser);

        if (userRepository.findByEmail("test@example.com").isPresent()) {
            testUser = userRepository.findByEmail("test@example.com").get();
        }

        systemProduct1 = new Product();
        systemProduct1.setName("Apple");
        systemProduct1.setCaloriesPer100g(52.0);
        systemProduct1.setProteinsPer100g(0.3);
        systemProduct1.setFatsPer100g(0.2);
        systemProduct1.setCarbsPer100g(14.0);
        systemProduct1.setCreatedByUser(null);
        systemProduct1 = productRepository.save(systemProduct1);

        Product systemProduct2 = new Product();
        systemProduct2.setName("Banana");
        systemProduct2.setCaloriesPer100g(89.0);
        systemProduct2.setProteinsPer100g(1.1);
        systemProduct2.setFatsPer100g(0.3);
        systemProduct2.setCarbsPer100g(22.8);
        systemProduct2.setCreatedByUser(null);
        systemProduct2 = productRepository.save(systemProduct2);

        Product userProduct1 = new Product();
        userProduct1.setName("Chicken Breast");
        userProduct1.setCaloriesPer100g(165.0);
        userProduct1.setProteinsPer100g(31.0);
        userProduct1.setFatsPer100g(3.6);
        userProduct1.setCarbsPer100g(0.0);
        userProduct1.setCreatedByUser(testUser);
        userProduct1 = productRepository.save(userProduct1);

        Product userProduct2 = new Product();
        userProduct2.setName("Oatmeal");
        userProduct2.setCaloriesPer100g(68.0);
        userProduct2.setProteinsPer100g(2.4);
        userProduct2.setFatsPer100g(1.4);
        userProduct2.setCarbsPer100g(12.0);
        userProduct2.setCreatedByUser(testUser);
        userProduct2 = productRepository.save(userProduct2);
    }

    private String extractCsrfToken(String htmlContent) {
        Document doc = Jsoup.parse(htmlContent);
        return doc.select("input[name=_csrf]").attr("value");
    }

    private SessionData authenticateUser() {
        Response loginPageResponse = given()
                .when()
                .get("/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String csrfToken = extractCsrfToken(loginPageResponse.getBody().asString());
        io.restassured.http.Cookie sessionCookie = loginPageResponse.getDetailedCookie("JSESSIONID");

        Response loginResponse = given()
                .cookie(sessionCookie)
                .contentType(ContentType.URLENC)
                .formParam("username", "testuser")
                .formParam("password", "Password123!")
                .formParam("_csrf", csrfToken)
                .when()
                .post("/login")
                .then()
                .statusCode(302)
                .extract()
                .response();

        return new SessionData(
                loginResponse.getDetailedCookie("JSESSIONID"),
                csrfToken
        );
    }

    /**
     * Вспомогательный класс для хранения данных сессии
     */
    public record SessionData(io.restassured.http.Cookie sessionCookie, String csrfToken) {
    }

    @Test
    void getAllProducts_WithAuthentication_ShouldReturnAllProducts() {
        SessionData session = authenticateUser();

        given()
                .cookie(session.sessionCookie)
                .header("X-CSRF-TOKEN", session.csrfToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products/all")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(4))
                .body("name", hasItems("Apple", "Banana", "Chicken Breast", "Oatmeal"));
    }

    @Test
    void getAllProducts_WithWrongCredentials_ShouldReturnError() {
        Response loginPageResponse = given()
                .when()
                .get("/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String csrfToken = extractCsrfToken(loginPageResponse.getBody().asString());
        io.restassured.http.Cookie sessionCookie = loginPageResponse.getDetailedCookie("JSESSIONID");

        given()
                .cookie(sessionCookie)
                .contentType(ContentType.URLENC)
                .formParam("username", "wronguser")
                .formParam("password", "wrongpassword")
                .formParam("_csrf", csrfToken)
                .when()
                .post("/login")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login?error"));
    }

    @Test
    void getAllSystemProducts_WithAuthentication_ShouldReturnOnlySystemProducts() {
        SessionData session = authenticateUser();

        given()
                .cookie(session.sessionCookie)
                .header("X-CSRF-TOKEN", session.csrfToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products/system")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(2))
                .body("name", hasItems("Apple", "Banana"))
                .body("findAll { it.createdByUser == null }.size()", is(2));
    }

    @Test
    void getByCreatorId_WithValidUserId_ShouldReturnUserProducts() {
        SessionData session = authenticateUser();

        given()
                .cookie(session.sessionCookie)
                .header("X-CSRF-TOKEN", session.csrfToken)
                .contentType(ContentType.JSON)
                .param("userId", testUser.getId())
                .when()
                .get("/api/products/getByCreatorId")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(2))
                .body("name", hasItems("Chicken Breast", "Oatmeal"));
    }

    @Test
    void getProductsByName_WithAuthentication_ShouldReturnMatchingProducts() {
        SessionData session = authenticateUser();

        given()
                .cookie(session.sessionCookie)
                .contentType(ContentType.JSON)
                .param("name", "apple")
                .when()
                .get("/api/products/getProductsByName")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("Apple"));
    }

    @Test
    void getProductById_WithValidId_ShouldReturnProduct() {
        SessionData session = authenticateUser();

        given()
                .cookie(session.sessionCookie)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products/{id}", systemProduct1.getId())
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(systemProduct1.getId().intValue()))
                .body("name", equalTo("Apple"))
                .body("caloriesPer100g", equalTo(52.0f));
    }

    @Test
    void getSystemProductsWithMinCalories_WithValidCalories_ShouldReturnFilteredProducts() {
        SessionData session = authenticateUser();

        given()
                .cookie(session.sessionCookie)
                .contentType(ContentType.JSON)
                .param("calories", 60.0)
                .when()
                .get("/api/products/getProductsWithMinCalories/system")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("Banana"))
                .body("[0].caloriesPer100g", greaterThanOrEqualTo(60.0f));
    }

    @Test
    void existsProductsByName_WithExistingProduct_ShouldReturnTrue() {
        SessionData session = authenticateUser();

        given()
                .cookie(session.sessionCookie)
                .contentType(ContentType.JSON)
                .param("productName", "Apple")
                .when()
                .get("/api/products/existsProductsByName")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(equalTo("true"));
    }

    @Test
    void responseHeaders_ShouldContainCorrectContentType() {
        SessionData session = authenticateUser();

        given()
                .cookie(session.sessionCookie)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products/system")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Content-Type", containsString("application/json"));
    }

    @Test
    void apiRequest_WithoutCsrfToken_ShouldBeRejected() {
        SessionData session = authenticateUser();

        given()
                .cookie(session.sessionCookie)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products/all")
                .then()
                .statusCode(200);
    }
}