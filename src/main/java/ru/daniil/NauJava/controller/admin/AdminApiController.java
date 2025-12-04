package ru.daniil.NauJava.controller.admin;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.daniil.NauJava.request.UserDetailsResponse;
import ru.daniil.NauJava.request.UsersListResponse;
import ru.daniil.NauJava.request.UsersStatisticsResponse;
import ru.daniil.NauJava.service.admin.AdminService;

import java.util.List;

@RestController
@RequestMapping("/admin/api/users")
public class AdminApiController {

    private final AdminService adminService;

    public AdminApiController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<List<UsersListResponse>> getUsersList() {
        try {
            // TODO: здесь необходимо добавить кеширование
            List<UsersListResponse> users = adminService.getUsersWithLastActivity();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<UsersStatisticsResponse> getUsersStats() {
        try {
            // TODO: здесь необходимо добавить кеширование
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