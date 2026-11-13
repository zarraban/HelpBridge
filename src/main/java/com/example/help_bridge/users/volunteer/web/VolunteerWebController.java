package com.example.help_bridge.users.volunteer.web;

import com.example.help_bridge.fundraising.request.security.RequestAccessPolicy;
import com.example.help_bridge.users.volunteer.command.*;
import com.example.help_bridge.users.volunteer.exception.*;
import com.example.help_bridge.users.volunteer.service.VolunteerService;
import com.example.help_bridge.users.volunteer.security.VolunteerAccessPolicy;
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
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('FUND_REPRESENTATIVE', 'SYSTEM_ADMIN')")
public class VolunteerWebController {
    private final VolunteerService volunteers;
    private final VolunteerAccessPolicy access;
    private final RequestAccessPolicy identity;

    @GetMapping("/team")
    @PreAuthorize("hasRole('FUND_REPRESENTATIVE')")
    public String ownTeam() {
        Long fundId = identity.currentViewer().fundId();
        access.requireManageFund(fundId);
        return listRedirect(fundId);
    }

    @GetMapping("/funds/{fundId}/volunteers")
    public String list(@PathVariable Long fundId, Model model) {
        model.addAttribute("volunteers", volunteers.getFundVolunteers(fundId));
        model.addAttribute("fundId", fundId);
        return "volunteers/list";
    }

    @GetMapping("/funds/{fundId}/volunteers/new")
    public String newForm(@PathVariable Long fundId, Model model) {
        access.requireManageFund(fundId);
        prepare(fundId, null, model);
        model.addAttribute("form", new RegisterVolunteerForm("", "", "", "", ""));
        return "volunteers/form";
    }

    @PostMapping("/funds/{fundId}/volunteers")
    public String create(@PathVariable Long fundId, @Valid @ModelAttribute("form") RegisterVolunteerForm form,
                         BindingResult errors, Model model, HttpServletResponse response, RedirectAttributes redirect) {
        access.requireManageFund(fundId);
        prepare(fundId, null, model);
        if (errors.hasErrors()) return "volunteers/form";
        try {
            volunteers.registerVolunteer(new RegisterVolunteerCommand(fundId, form.firstName(), form.lastName(),
                    form.email(), form.phone(), form.password()));
        } catch (DuplicateVolunteerException exception) {
            return duplicate(errors, response);
        }
        redirect.addFlashAttribute("message", "Волонтера додано до команди.");
        return listRedirect(fundId);
    }

    @GetMapping("/funds/{fundId}/volunteers/{volunteerId}/edit")
    public String editForm(@PathVariable Long fundId, @PathVariable Long volunteerId, Model model) {
        var volunteer = volunteers.getFundVolunteer(fundId, volunteerId);
        prepare(fundId, volunteerId, model);
        model.addAttribute("form", new VolunteerForm(volunteer.firstName(), volunteer.lastName(), volunteer.email(), volunteer.phone()));
        return "volunteers/form";
    }

    @PostMapping("/funds/{fundId}/volunteers/{volunteerId}")
    public String update(@PathVariable Long fundId, @PathVariable Long volunteerId,
                         @Valid @ModelAttribute("form") VolunteerForm form, BindingResult errors,
                         Model model, HttpServletResponse response, RedirectAttributes redirect) {
        volunteers.getFundVolunteer(fundId, volunteerId);
        prepare(fundId, volunteerId, model);
        if (errors.hasErrors()) return "volunteers/form";
        try {
            volunteers.updateVolunteer(new UpdateVolunteerCommand(volunteerId, fundId,
                    form.firstName(), form.lastName(), form.email(), form.phone()));
        } catch (DuplicateVolunteerException exception) {
            return duplicate(errors, response);
        }
        redirect.addFlashAttribute("message", "Дані волонтера збережено.");
        return listRedirect(fundId);
    }

    @GetMapping("/funds/{fundId}/volunteers/{volunteerId}/delete")
    public String deleteForm(@PathVariable Long fundId, @PathVariable Long volunteerId, Model model) {
        model.addAttribute("volunteer", volunteers.getFundVolunteer(fundId, volunteerId));
        model.addAttribute("fundId", fundId);
        return "volunteers/delete";
    }

    @PostMapping("/funds/{fundId}/volunteers/{volunteerId}/delete")
    public String delete(@PathVariable Long fundId, @PathVariable Long volunteerId, RedirectAttributes redirect) {
        try {
            volunteers.removeVolunteer(fundId, volunteerId);
            redirect.addFlashAttribute("message", "Волонтера видалено з команди. Історію роботи збережено.");
        } catch (VolunteerHasActiveAssignmentsException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        } catch (VolunteerNotFoundException exception) {
            redirect.addFlashAttribute("error", "Волонтера вже видалено або його не знайдено. Перевірте актуальний список.");
        }
        return listRedirect(fundId);
    }

    private void prepare(Long fundId, Long volunteerId, Model model) {
        model.addAttribute("fundId", fundId);
        model.addAttribute("volunteerId", volunteerId);
        model.addAttribute("editing", volunteerId != null);
    }

    private String duplicate(BindingResult errors, HttpServletResponse response) {
        errors.reject("volunteer.duplicate", "Email або телефон уже належить іншому волонтеру. Перевірте обидва поля.");
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        return "volunteers/form";
    }

    private String listRedirect(Long fundId) { return "redirect:/funds/" + fundId + "/volunteers"; }
}
