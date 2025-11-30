package ru.daniil.NauJava.controller.userData;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.entity.UserProfile;
import ru.daniil.NauJava.service.ActivityLevelService;
import ru.daniil.NauJava.service.UserProfileService;
import ru.daniil.NauJava.service.UserService;

import java.util.Date;

@Controller
@RequestMapping("/view")
public class AccountController {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final ActivityLevelService activityLevelService;

    public AccountController(UserService userService, UserProfileService userProfileService,
                             ActivityLevelService activityLevelService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
        this.activityLevelService = activityLevelService;
    }

    @GetMapping("/account")
    public String getAccountPage(Model model) {
        User user = userService.getAuthUser().orElseThrow();
        UserProfile userProfile = userProfileService.getUserProfileByUser(user);

        userProfileService.updateStreakIfNeeded(userProfile);

        if (userProfile.getDateOfBirth() != null) {
            Date utilDate = java.sql.Date.valueOf(userProfile.getDateOfBirth());
            model.addAttribute("date", utilDate);
        }
        else {
            model.addAttribute("date", null);
        }

        model.addAttribute("user", user);
        model.addAttribute("userProfile", userProfile);
        model.addAttribute("nutritionGoal", userProfile.getNutritionGoal());
        model.addAttribute("activityLevels", activityLevelService.getAllActivityLevels());
        model.addAttribute("hasCompleteProfile", userProfileService.hasCompleteProfile(userProfile));

        return "account";
    }
}