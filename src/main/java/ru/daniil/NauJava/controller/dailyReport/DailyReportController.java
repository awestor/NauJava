package ru.daniil.NauJava.controller.dailyReport;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.daniil.NauJava.entity.UserProfile;
import ru.daniil.NauJava.service.DailyReportService;
import ru.daniil.NauJava.service.UserProfileService;

@Controller
@RequestMapping("/view/daily-reports")
public class DailyReportController {
    private final UserProfileService userProfileService;
    private final DailyReportService dailyReportService;

    public DailyReportController(UserProfileService userProfileService,
                                     DailyReportService dailyReportService) {
        this.userProfileService = userProfileService;
        this.dailyReportService = dailyReportService;
    }

    @GetMapping("/stat")
    public String getActivityPage(Model model) {
        try {
            UserProfile userProfile = userProfileService.getAuthUserProfile();
            model.addAttribute("userProfile", userProfile);
        } catch (Exception e) {
            model.addAttribute("userProfile", null);
        }
        return "calendarActivity";
    }
}