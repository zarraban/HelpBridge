package com.example.help_bridge.fundraising.fundraiser.web;

import com.example.help_bridge.fundraising.fundraiser.dto.request.CompleteAssignmentRequest;
import com.example.help_bridge.fundraising.fundraiser.dto.request.ReturnAssignmentRequest;
import com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus;
import com.example.help_bridge.fundraising.fundraiser.exception.*;
import com.example.help_bridge.fundraising.fundraiser.service.FundraiserAssignmentService;
import com.example.help_bridge.fundraising.fundraiser.service.VolunteerWorkspaceService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/my/assignments/{assignmentId}")
@PreAuthorize("hasRole('VOLUNTEER')")
@RequiredArgsConstructor
public class VolunteerAssignmentActionWebController {
    private static final String FORM = "volunteer-workspace/assignment-action";
    private final VolunteerWorkspaceService workspace;
    private final FundraiserAssignmentService assignments;

    @GetMapping("/complete")
    public String completeForm(@PathVariable Long assignmentId, Model model, HttpServletResponse response) {
        prepare(assignmentId, true, model, response);
        model.addAttribute("form", new CompleteAssignmentForm(""));
        return FORM;
    }

    @GetMapping("/return")
    public String returnForm(@PathVariable Long assignmentId, Model model, HttpServletResponse response) {
        prepare(assignmentId, false, model, response);
        model.addAttribute("form", new ReturnAssignmentForm(""));
        return FORM;
    }

    @PostMapping("/complete")
    public String complete(@PathVariable Long assignmentId, @Valid @ModelAttribute("form") CompleteAssignmentForm form,
                           BindingResult errors, Model model, HttpServletResponse response, RedirectAttributes redirect) {
        return save(assignmentId, true, form.closingComment(), errors, model, response, redirect);
    }

    @PostMapping("/return")
    public String returnAssignment(@PathVariable Long assignmentId, @Valid @ModelAttribute("form") ReturnAssignmentForm form,
                                   BindingResult errors, Model model, HttpServletResponse response, RedirectAttributes redirect) {
        return save(assignmentId, false, form.returnReason(), errors, model, response, redirect);
    }

    private String save(Long id, boolean completing, String comment, BindingResult errors, Model model,
                        HttpServletResponse response, RedirectAttributes redirect) {
        if (!prepare(id, completing, model, response) || errors.hasErrors()) return FORM;
        try {
            if (completing) assignments.completeAssignment(id, new CompleteAssignmentRequest(comment));
            else assignments.returnAssignment(id, new ReturnAssignmentRequest(comment));
        } catch (InvalidAssignmentStateException exception) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            model.addAttribute("actionable", false);
            model.addAttribute("conflict", true);
            return FORM;
        } catch (AssignmentNotFoundException | FundraiserNotFoundException exception) {
            redirect.addFlashAttribute("error", "Призначення або збір більше недоступні. Перевірте актуальний список.");
            return "redirect:/my/assignments";
        }
        redirect.addFlashAttribute("message", completing
                ? "Роботу за призначенням завершено. Коментар збережено."
                : "Призначення повернуто. Причину збережено.");
        return "redirect:/my/assignments";
    }

    private boolean prepare(Long id, boolean completing, Model model, HttpServletResponse response) {
        var assignment = workspace.getMyAssignment(id);
        boolean active = assignment.status() == AssignmentStatus.ACTIVE;
        model.addAttribute("assignment", assignment);
        model.addAttribute("completing", completing);
        model.addAttribute("actionable", active);
        model.addAttribute("conflict", !active);
        if (!active) response.setStatus(HttpServletResponse.SC_CONFLICT);
        return active;
    }
}
