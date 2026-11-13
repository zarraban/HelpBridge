package com.example.help_bridge.fundraising.fund.web;

import com.example.help_bridge.common.web.ManagementPages;
import com.example.help_bridge.fundraising.fund.dto.request.FundCreateRequest;
import com.example.help_bridge.fundraising.fund.dto.request.FundDescriptUpdateRequest;
import com.example.help_bridge.fundraising.fund.dto.request.FundStatusUpdateRequest;
import com.example.help_bridge.fundraising.fund.dto.request.FundUpdateRequest;
import com.example.help_bridge.fundraising.fund.entity.FundStatus;
import com.example.help_bridge.fundraising.fund.exception.DuplicateFundException;
import com.example.help_bridge.fundraising.fund.exception.FundHasRequestsException;
import com.example.help_bridge.fundraising.fund.exception.FundNotApprovedException;
import com.example.help_bridge.fundraising.fund.exception.InvalidFundStatusTransitionException;
import com.example.help_bridge.fundraising.fund.service.FundService;
import com.example.help_bridge.fundraising.request.security.RequestAccessPolicy;
import com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Map;

import static com.example.help_bridge.common.web.ManagementPages.field;

@Controller
@RequestMapping("/funds")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'FUND_REPRESENTATIVE')")
@RequiredArgsConstructor
public class FundWebController {
    private final FundService funds;
    private final RequestAccessPolicy access;
    private final SystemAdminRepository admins;

    @InitBinder("form")
    void bindForm(WebDataBinder binder) {
        binder.setDisallowedFields("socialMediaUrls", "socialMediaUrls[*]");
    }

    @GetMapping
    public String list(Model model) {
        var viewer = access.currentViewer();
        if (!viewer.isAdmin()) {
            if (viewer.fundId() == null) throw new AccessDeniedException("No fund assigned");
            return "redirect:/funds/" + viewer.fundId();
        }
        model.addAttribute("funds", funds.getAllFunds());
        return "funds/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        access.currentViewer().requireBookingFund(id);
        var fund = funds.getFundById(id);
        model.addAttribute("fund", fund);
        model.addAttribute("canChangeStatus", Arrays.stream(FundStatus.values()).anyMatch(fund.status()::canTransitionTo));
        return "funds/details";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("form", new FundCreateRequest(null, null, null, null, null, null, null, null, Map.of()));
        return createPage(model);
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public String create(@Valid @ModelAttribute("form") FundCreateRequest form, BindingResult errors,
                         Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) return createPage(model);
        try {
            return saved(funds.createFund(form).id(), redirect, "Фонд створено.");
        } catch (DuplicateFundException exception) {
            errors.rejectValue("edrpou", "duplicate", "Фонд із цим кодом ЄДРПОУ вже існує.");
            return createPage(model);
        }
    }

    private String createPage(Model model) {
        return ManagementPages.form(model, "Додати фонд", "/funds/new", "/funds",
                field("fundName", "Назва фонду"), field("edrpou", "Код ЄДРПОУ"),
                field("bankDetail", "Банківські реквізити", "textarea"),
                field("registeredAddress", "Юридична адреса"), field("actualAddress", "Фактична адреса"),
                field("phoneNumber", "Телефон", "tel"), field("corpEmail", "Електронна пошта", "email"),
                field("website", "Вебсайт", "url"));
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        access.currentViewer().requireBookingFund(id);
        var fund = funds.getFundById(id);
        model.addAttribute("form", new FundUpdateRequest(fund.fundName(), fund.registeredAddress(),
                fund.actualAddress(), fund.phoneNumber(), fund.corpEmail(), fund.website(), fund.socialMediaUrls()));
        return editPage(id, model);
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute("form") FundUpdateRequest form,
                       BindingResult errors, Model model, RedirectAttributes redirect) {
        access.currentViewer().requireBookingFund(id);
        if (errors.hasErrors()) return editPage(id, model);
        var current = funds.getFundById(id);
        funds.updateFund(id, new FundUpdateRequest(form.fundName(), form.registeredAddress(), form.actualAddress(),
                form.phoneNumber(), form.corpEmail(), form.website(), current.socialMediaUrls()));
        return saved(id, redirect, "Дані фонду оновлено.");
    }

    private String editPage(Long id, Model model) {
        return ManagementPages.form(model, "Редагувати фонд", "/funds/" + id + "/edit", "/funds/" + id,
                field("fundName", "Назва фонду"), field("registeredAddress", "Юридична адреса"),
                field("actualAddress", "Фактична адреса"), field("phoneNumber", "Телефон", "tel"),
                field("corpEmail", "Електронна пошта", "email"), field("website", "Вебсайт", "url"));
    }

    @GetMapping("/{id}/description")
    public String descriptionForm(@PathVariable Long id, Model model) {
        access.currentViewer().requireBookingFund(id);
        model.addAttribute("form", new FundDescriptUpdateRequest(funds.getFundById(id).description()));
        return descriptionPage(id, model);
    }

    @PostMapping("/{id}/description")
    public String description(@PathVariable Long id, @Valid @ModelAttribute("form") FundDescriptUpdateRequest form,
                              BindingResult errors, Model model, RedirectAttributes redirect) {
        access.currentViewer().requireBookingFund(id);
        if (errors.hasErrors()) return descriptionPage(id, model);
        try {
            funds.updateFundDescription(id, form);
            return saved(id, redirect, "Опис фонду оновлено.");
        } catch (FundNotApprovedException exception) {
            errors.reject("status", "Опис можна змінювати після схвалення фонду.");
            return descriptionPage(id, model);
        }
    }

    private String descriptionPage(Long id, Model model) {
        return ManagementPages.form(model, "Змінити опис фонду", "/funds/" + id + "/description", "/funds/" + id,
                field("description", "Опис", "textarea"));
    }

    @GetMapping("/{id}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public String statusForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", new StatusForm(null, null));
        return statusPage(id, model);
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public String status(@PathVariable Long id, @Valid @ModelAttribute("form") StatusForm form,
                         BindingResult errors, Authentication authentication, Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) return statusPage(id, model);
        String username = authentication.getName();
        Long adminId = admins.findByEmail(username.substring(username.indexOf(':') + 1))
                .orElseThrow(() -> new AccessDeniedException("Administrator account unavailable")).getId();
        try {
            funds.updateFundStatus(id, new FundStatusUpdateRequest(form.status(), adminId, form.comment()));
            return saved(id, redirect, "Статус фонду оновлено.");
        } catch (InvalidFundStatusTransitionException exception) {
            errors.reject("status", "Такий перехід статусу вже недоступний.");
            return statusPage(id, model);
        }
    }

    private String statusPage(Long id, Model model) {
        var fund = funds.getFundById(id);
        model.addAttribute("fund", fund);
        model.addAttribute("statuses", Arrays.stream(FundStatus.values()).filter(fund.status()::canTransitionTo).toList());
        return "funds/status";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public String deleteConfirmation(@PathVariable Long id, Model model) {
        return ManagementPages.delete(model, funds.getFundById(id).fundName(), "/funds/" + id + "/delete", "/funds/" + id);
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            funds.deleteFundById(id);
            redirect.addFlashAttribute("message", "Фонд видалено.");
            return "redirect:/funds";
        } catch (FundHasRequestsException exception) {
            redirect.addFlashAttribute("error", "Фонд має пов’язані заявки і не може бути видалений.");
            return "redirect:/funds/" + id;
        }
    }

    private static String saved(Long id, RedirectAttributes redirect, String message) {
        redirect.addFlashAttribute("message", message);
        return "redirect:/funds/" + id;
    }

    public record StatusForm(@NotNull FundStatus status, @Size(max = 1000) String comment) {}
}
