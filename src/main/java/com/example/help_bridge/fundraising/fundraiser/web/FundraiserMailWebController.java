package com.example.help_bridge.fundraising.fundraiser.web;

import com.example.help_bridge.common.web.ManagementPages;
import com.example.help_bridge.fundraising.fundraiser.dto.request.SendMailingRequest;
import com.example.help_bridge.fundraising.fundraiser.service.FundraiserService;
import com.example.help_bridge.users.donor.service.DonorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.example.help_bridge.common.web.ManagementPages.field;

@Controller
@PreAuthorize("hasRole('FUND_REPRESENTATIVE')")
@RequiredArgsConstructor
public class FundraiserMailWebController {
    private final FundraiserService fundraisers;
    private final DonorService donors;

    @GetMapping("/fundraisers/{id}/mail")
    public String form(@PathVariable Long id, Model model) {
        fundraisers.getFundraiserById(id);
        model.addAttribute("form", new MailForm(null, null));
        return page(id, donors.getDonorsByFundraiserId(id).isEmpty(), model);
    }

    @PostMapping("/fundraisers/{id}/mail")
    public String send(@PathVariable Long id, @Valid @ModelAttribute("form") MailForm form,
                       BindingResult errors, Model model, RedirectAttributes redirect) {
        fundraisers.getFundraiserById(id);
        boolean noRecipients = donors.getDonorsByFundraiserId(id).isEmpty();
        if (noRecipients || errors.hasErrors()) return page(id, noRecipients, model);
        fundraisers.sendMailToDonors(new SendMailingRequest(id, form.subject(), form.message()));
        redirect.addFlashAttribute("message", "Розсилку поставлено в чергу.");
        return "redirect:/fundraisers/" + id;
    }

    private String page(Long id, boolean noRecipients, Model model) {
        model.addAttribute("noRecipients", noRecipients);
        model.addAttribute("submitLabel", "Поставити в чергу");
        return ManagementPages.form(model, "Розсилка донорам збору №" + id, "/fundraisers/" + id + "/mail",
                "/fundraisers/" + id, field("subject", "Тема"), field("message", "Повідомлення", "textarea"));
    }

    public record MailForm(@NotBlank @Size(max = 150) String subject, @NotBlank @Size(max = 5000) String message) {}
}
