package ru.daniil.NauJava.controller.admin.users;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.daniil.NauJava.response.UserDetailsResponse;
import ru.daniil.NauJava.response.UsersListResponse;
import ru.daniil.NauJava.response.UsersStatisticsResponse;
import ru.daniil.NauJava.service.admin.AdminService;

import java.util.List;

@RestController
@RequestMapping("/admin/api/users")
public class AdminUsersApiController {

    private final AdminService adminService;

    private static final Logger logger = LoggerFactory.getLogger(AdminUsersApiController.class);
    private static final Logger appLogger = LoggerFactory.getLogger("APP-LOGGER");

    public AdminUsersApiController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Метод контроллера, что служит для генерации списка из информации по пользователям вместе
     * с их датой последней активности
     * @return список DTO UsersListResponse
     */
    @GetMapping
    public ResponseEntity<List<UsersListResponse>> getUsersList() {
        try {
            appLogger.info("GET /admin/api/users/ | Получение данных о пользователях");
            List<UsersListResponse> users = adminService.getUsersWithLastActivity();
            appLogger.debug("Данные о пользователях получены");
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.warn("Получение данных о пользователях прошло неудачно с ошибкой:{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Метод контроллера, что служит для генерации общей информации
     * по зарегистрированным пользователям
     * @return один DTO UsersStatisticsResponse
     */
    @GetMapping("/stats")
    public ResponseEntity<UsersStatisticsResponse> getUsersStats() {
        try {
            appLogger.info("GET /admin/api/users/stats | Получение общих данных о пользователях");

            UsersStatisticsResponse stats = adminService.getUsersStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.warn("Получение общих данных о пользователях прошло неудачно с ошибкой:{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{login}")
    public ResponseEntity<UserDetailsResponse> getUserDetails(@PathVariable String login) {
        try {
            appLogger.info("GET /admin/api/users/{login} | Получение данных о конкретном пользователе");

            UserDetailsResponse userDetails = adminService.getUserDetailsByLogin(login);
            return ResponseEntity.ok(userDetails);
        } catch (EntityNotFoundException e) {
            logger.error("Пользователь указанный в запросе не найден:{}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Получение о конкретном пользователе вызвало ошибку:{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}