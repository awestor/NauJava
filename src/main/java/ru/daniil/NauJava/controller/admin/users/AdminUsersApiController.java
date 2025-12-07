package ru.daniil.NauJava.controller.admin.users;

import jakarta.persistence.EntityNotFoundException;
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
import ru.daniil.NauJava.service.admin.AdminServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/admin/api/users")
public class AdminUsersApiController {

    private final AdminService adminService;

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
            List<UsersListResponse> users = adminService.getUsersWithLastActivity();
            System.out.println("Данные получены. Происходит возврат.");
            return ResponseEntity.ok(users);
        } catch (Exception e) {
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
            UsersStatisticsResponse stats = adminService.getUsersStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{login}")
    public ResponseEntity<UserDetailsResponse> getUserDetails(@PathVariable String login) {
        try {
            UserDetailsResponse userDetails = adminService.getUserDetailsByLogin(login);
            return ResponseEntity.ok(userDetails);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}