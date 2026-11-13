package com.example.help_bridge.users.fundrepresentative.web;

import com.example.help_bridge.common.web.ManagementPages;
import com.example.help_bridge.common.web.ManagementPages.Row;
import com.example.help_bridge.users.fundrepresentative.dto.request.FundRepresentativeRequest;
import com.example.help_bridge.users.fundrepresentative.dto.request.FundRepresentativeUpdateRequest;
import com.example.help_bridge.users.fundrepresentative.service.FundRepresentativeService;
import com.example.help_bridge.users.fundrepresentative.exception.DuplicateFundRepresentativeException;
import com.example.help_bridge.fundraising.fund.exception.FundNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static com.example.help_bridge.common.web.ManagementPages.field;

@Controller
@RequestMapping("/fund-representatives")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class FundRepresentativeWebController {
    private static final String PATH = "/fund-representatives";
    private final FundRepresentativeService service;

    @GetMapping
    public String list(Model model) {
        var rows = service.getAll().stream()
                .map(item -> new Row(item.id(), List.of(item.firstName(), item.lastName(), item.email(), String.valueOf(item.fundId()))))
                .toList();
        return ManagementPages.list(model, "Представники фондів", PATH,
                List.of("Ім’я", "Прізвище", "Електронна пошта", "Фонд"), rows);
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new FundRepresentativeRequest(null, null, null, null, null, null));
        return createPage(model);
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") FundRepresentativeRequest form,
                         BindingResult errors, Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) return createPage(model);
        try {
            service.create(form);
        } catch (DuplicateFundRepresentativeException exception) {
            errors.rejectValue("email", "duplicate", "Обліковий запис із цією адресою вже існує.");
        } catch (FundNotFoundException exception) {
            errors.rejectValue("fundId", "missing", "Фонд не знайдено.");
        }
        if (errors.hasErrors()) return createPage(model);
        redirect.addFlashAttribute("message", "Запис створено.");
        return "redirect:" + PATH;
    }

    private String createPage(Model model) {
        return ManagementPages.form(model, "Додати представника", PATH + "/new", PATH,
                field("firstName", "Ім’я"),
                field("lastName", "Прізвище"),
                field("email", "Електронна пошта", "email"),
                field("phone", "Телефон", "tel"),
                field("password", "Пароль", "password"),
                field("fundId", "Номер фонду", "number"));
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var item = service.getById(id);
        model.addAttribute("form", new FundRepresentativeUpdateRequest(item.firstName(), item.lastName(), item.email(), item.phone()));
        return editPage(id, model);
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute("form") FundRepresentativeUpdateRequest form,
                       BindingResult errors, Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) return editPage(id, model);
        try {
            service.update(id, form);
        } catch (DuplicateFundRepresentativeException exception) {
            errors.rejectValue("email", "duplicate", "Обліковий запис із цією адресою вже існує.");
        }
        if (errors.hasErrors()) return editPage(id, model);
        redirect.addFlashAttribute("message", "Зміни збережено.");
        return "redirect:" + PATH;
    }

    private String editPage(Long id, Model model) {
        return ManagementPages.form(model, "Редагувати представника", PATH + "/" + id + "/edit", PATH,
                field("firstName", "Ім’я"),
                field("lastName", "Прізвище"),
                field("email", "Електронна пошта", "email"),
                field("phone", "Телефон", "tel"));
    }

    @GetMapping("/{id}/delete")
    public String deleteConfirmation(@PathVariable Long id, Model model) {
        var item = service.getById(id);
        return ManagementPages.delete(model, item.firstName() + " " + item.lastName(), PATH + "/" + id + "/delete", PATH);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        service.delete(id);
        redirect.addFlashAttribute("message", "Запис видалено.");
        return "redirect:" + PATH;
    }
}
