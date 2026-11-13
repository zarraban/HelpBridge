package com.example.help_bridge.fundraising.fundraiser.web;

import com.example.help_bridge.fundraising.fundraiser.dto.request.AssignVolunteerRequest;
import com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserStatus;
import com.example.help_bridge.fundraising.fundraiser.exception.InvalidAssignmentStateException;
import com.example.help_bridge.fundraising.fundraiser.service.FundraiserAssignmentService;
import com.example.help_bridge.fundraising.fundraiser.service.FundraiserService;
import com.example.help_bridge.fundraising.request.service.RequestService;
import com.example.help_bridge.users.volunteer.service.VolunteerService;
import com.example.help_bridge.users.volunteer.exception.VolunteerNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.stream.Collectors;
import java.util.List;
import com.example.help_bridge.users.volunteer.dto.response.VolunteerResponse;

@Controller
@RequestMapping("/fundraisers/{fundraiserId}/assignments")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'FUND_REPRESENTATIVE')")
@RequiredArgsConstructor
public class FundraiserAssignmentWebController {
    private final FundraiserService fundraisers;
    private final FundraiserAssignmentService assignments;
    private final RequestService requests;
    private final VolunteerService volunteers;

    @GetMapping("/new")
    public String form(@PathVariable Long fundraiserId, Model model) {
        prepare(fundraiserId, model);
        model.addAttribute("form", new AssignVolunteerForm(null));
        return "fundraisers/assignment-form";
    }

    @PostMapping
    public String assign(@PathVariable Long fundraiserId, @Valid @ModelAttribute("form") AssignVolunteerForm form,
                         BindingResult errors, Model model, HttpServletResponse response, RedirectAttributes redirect) {
        var available = prepare(fundraiserId, model);
        if (!(Boolean) model.getAttribute("canAssign")) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return "fundraisers/assignment-form";
        }
        if (errors.hasErrors()) return "fundraisers/assignment-form";
        if (available.stream().noneMatch(v -> v.id().equals(form.volunteerId()))) {
            errors.rejectValue("volunteerId", "assignment.unavailable", "Цей волонтер більше недоступний для призначення. Оберіть волонтера з актуального списку.");
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return "fundraisers/assignment-form";
        }
        try {
            assignments.assignVolunteerToFund(fundraiserId, new AssignVolunteerRequest(form.volunteerId()));
        } catch (InvalidAssignmentStateException | VolunteerNotFoundException exception) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            errors.reject("assignment.conflict", "Призначення не виконано: волонтер уже призначений, став неактивним або збір закрито. Перевірте актуальні дані.");
            prepare(fundraiserId, model);
            return "fundraisers/assignment-form";
        }
        redirect.addFlashAttribute("message", "Волонтера призначено на збір.");
        return "redirect:/fundraisers/" + fundraiserId + "#assignments";
    }

    private List<VolunteerResponse> prepare(Long id, Model model) {
        var fundraiser = fundraisers.getFundraiserById(id);
        var history = assignments.getFundraiserAssignments(id);
        var activeIds = history.stream().filter(a -> a.status() == AssignmentStatus.ACTIVE)
                .map(a -> a.volunteerId()).collect(Collectors.toSet());
        Long fundId = fundraiser.requestId() == null ? null : requests.getRequestById(fundraiser.requestId()).fundId();
        var available = fundId == null ? List.<VolunteerResponse>of()
                : volunteers.getFundVolunteers(fundId).stream().filter(v -> !activeIds.contains(v.id())).toList();
        model.addAttribute("fundraiser", fundraiser);
        model.addAttribute("availableVolunteers", available);
        model.addAttribute("closed", fundraiser.status() == FundraiserStatus.CLOSED);
        model.addAttribute("canAssign", fundraiser.status() != FundraiserStatus.CLOSED && !available.isEmpty());
        model.addAttribute("fundId", fundId);
        return available;
    }
}
