package ru.daniil.NauJava.controller.admin.users;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/view")
public class AdminUsersViewController {

    @GetMapping("/users")
    public String getAdminDashboard() {
        return "admin-users";
    }
}