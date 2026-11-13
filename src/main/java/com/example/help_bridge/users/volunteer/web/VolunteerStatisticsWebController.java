package com.example.help_bridge.users.volunteer.web;

import com.example.help_bridge.users.volunteer.dto.request.VolunteerStatisticsRequest;
import com.example.help_bridge.users.volunteer.security.VolunteerAccessPolicy;
import com.example.help_bridge.users.volunteer.service.VolunteerStatisticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;

@Controller
@PreAuthorize("hasRole('VOLUNTEER')")
@RequiredArgsConstructor
public class VolunteerStatisticsWebController {
    private final VolunteerAccessPolicy access;
    private final VolunteerStatisticsService statistics;

    @GetMapping("/my/statistics")
    public String form(Model model) {
        access.currentVolunteerId();
        model.addAttribute("form", new VolunteerStatisticsRequest(null, null));
        model.addAttribute("today", LocalDate.now());
        return "volunteer-workspace/statistics";
    }

    @GetMapping(value = "/my/statistics", params = "show")
    public String results(@Valid @ModelAttribute("form") VolunteerStatisticsRequest form, BindingResult errors, Model model) {
        Long volunteerId = access.currentVolunteerId();
        model.addAttribute("today", LocalDate.now());
        if (!errors.hasErrors()) {
            model.addAttribute("statistics", statistics.getClosedFundraisersStatistics(volunteerId, form));
        }
        return "volunteer-workspace/statistics";
    }
}
