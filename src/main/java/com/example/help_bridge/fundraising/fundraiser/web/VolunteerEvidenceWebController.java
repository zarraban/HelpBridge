package com.example.help_bridge.fundraising.fundraiser.web;

import com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserStatus;
import com.example.help_bridge.fundraising.fundraiser.exception.*;
import com.example.help_bridge.fundraising.fundraiser.service.EvidenceService;
import com.example.help_bridge.fundraising.fundraiser.service.VolunteerWorkspaceService;
import com.example.help_bridge.fundraising.request.web.RequestDocumentLink;
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
@RequestMapping("/my/fundraisers/{fundraiserId}/evidences")
@PreAuthorize("hasRole('VOLUNTEER')")
@RequiredArgsConstructor
public class VolunteerEvidenceWebController {
    private final VolunteerWorkspaceService workspace;
    private final EvidenceService evidences;

    @GetMapping("/new")
    public String form(@PathVariable Long fundraiserId, Model model, HttpServletResponse response) {
        prepare(fundraiserId, model, response);
        model.addAttribute("form", new EvidenceForm(null, null, null));
        return "volunteer-workspace/evidence-form";
    }

    @PostMapping
    public String create(@PathVariable Long fundraiserId, @Valid @ModelAttribute("form") EvidenceForm form,
                         BindingResult errors, Model model, HttpServletResponse response, RedirectAttributes redirect) {
        if (!prepare(fundraiserId, model, response)) return "volunteer-workspace/evidence-form";
        if (form.isEmpty()) errors.reject("evidence.empty", "Заповніть хоча б одне поле: номер квитанції, посилання на фото або відгук отримувача.");
        if (form.attachmentUrl() != null && !RequestDocumentLink.isSafeWebUrl(form.attachmentUrl())) {
            errors.rejectValue("attachmentUrl", "evidence.url", "Вкажіть повне HTTP/HTTPS-посилання без логіна та пароля.");
        }
        if (errors.hasErrors()) return "volunteer-workspace/evidence-form";
        try {
            evidences.addEvidenceToFundraiser(fundraiserId, form.toRequest());
        } catch (InvalidEvidenceException exception) {
            errors.reject("evidence.invalid", "Доказ не збережено. Перевірте номер квитанції (від 5 символів) та посилання на фото у форматі .jpg, .jpeg або .png.");
            return "volunteer-workspace/evidence-form";
        } catch (InvalidFundraiserStateException exception) {
            model.addAttribute("canAddEvidence", false);
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return "volunteer-workspace/evidence-form";
        } catch (FundraiserNotFoundException exception) {
            redirect.addFlashAttribute("error", "Збір більше недоступний. Перевірте актуальні призначення.");
            return "redirect:/my/assignments";
        }
        redirect.addFlashAttribute("message", "Доказ додано до збору.");
        return "redirect:/my/fundraisers/" + fundraiserId + "#evidences";
    }

    private boolean prepare(Long id, Model model, HttpServletResponse response) {
        var details = workspace.getMyFundraiser(id);
        boolean allowed = details.fundraiser().status() != FundraiserStatus.CLOSED
                && details.assignments().stream().anyMatch(a -> a.status() == AssignmentStatus.ACTIVE);
        model.addAttribute("fundraiser", details.fundraiser());
        model.addAttribute("canAddEvidence", allowed);
        if (!allowed) response.setStatus(HttpServletResponse.SC_CONFLICT);
        return allowed;
    }
}
