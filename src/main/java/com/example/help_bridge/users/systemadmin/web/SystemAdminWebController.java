package com.example.help_bridge.users.systemadmin.web;

import com.example.help_bridge.common.web.ManagementPages;
import com.example.help_bridge.common.web.ManagementPages.Row;
import com.example.help_bridge.users.systemadmin.dto.request.SystemAdminCreateRequest;
import com.example.help_bridge.users.systemadmin.dto.request.SystemAdminUpdateRequest;
import com.example.help_bridge.users.systemadmin.service.SystemAdminService;
import com.example.help_bridge.users.systemadmin.exception.DuplicateSystemAdminException;
import com.example.help_bridge.users.systemadmin.exception.SystemAdminHasVerificationActsException;
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
@RequestMapping("/system-admins")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class SystemAdminWebController {
    private static final String PATH = "/system-admins";
    private final SystemAdminService service;

    @GetMapping
    public String list(Model model) {
        var rows = service.getAllSystemAdmins().stream()
                .map(item -> new Row(item.id(), List.of(item.firstName(), item.lastName(), item.email())))
                .toList();
        return ManagementPages.list(model, "Системні адміністратори", PATH,
                List.of("Ім’я", "Прізвище", "Електронна пошта"), rows);
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new SystemAdminCreateRequest(null, null, null, null));
        return createPage(model);
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") SystemAdminCreateRequest form,
                         BindingResult errors, Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) return createPage(model);
        try {
            service.createSystemAdmin(form);
        } catch (DuplicateSystemAdminException exception) {
            errors.rejectValue("email", "duplicate", "Обліковий запис із цією адресою вже існує.");
        }
        if (errors.hasErrors()) return createPage(model);
        redirect.addFlashAttribute("message", "Запис створено.");
        return "redirect:" + PATH;
    }

    private String createPage(Model model) {
        return ManagementPages.form(model, "Додати адміністратора", PATH + "/new", PATH,
                field("firstName", "Ім’я"),
                field("lastName", "Прізвище"),
                field("email", "Електронна пошта", "email"),
                field("password", "Пароль", "password"));
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var item = service.getSystemAdminById(id);
        model.addAttribute("form", new SystemAdminUpdateRequest(item.firstName(), item.lastName()));
        return editPage(id, model);
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute("form") SystemAdminUpdateRequest form,
                       BindingResult errors, Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) return editPage(id, model);
        service.updateSystemAdmin(id, form);
        redirect.addFlashAttribute("message", "Зміни збережено.");
        return "redirect:" + PATH;
    }

    private String editPage(Long id, Model model) {
        return ManagementPages.form(model, "Редагувати адміністратора", PATH + "/" + id + "/edit", PATH,
                field("firstName", "Ім’я"),
                field("lastName", "Прізвище"));
    }

    @GetMapping("/{id}/delete")
    public String deleteConfirmation(@PathVariable Long id, Model model) {
        var item = service.getSystemAdminById(id);
        return ManagementPages.delete(model, item.firstName() + " " + item.lastName(), PATH + "/" + id + "/delete", PATH);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.deleteSystemAdminById(id);
        } catch (SystemAdminHasVerificationActsException exception) {
            redirect.addFlashAttribute("error", "Адміністратор має записи перевірок і не може бути видалений.");
            return "redirect:" + PATH;
        }
        redirect.addFlashAttribute("message", "Запис видалено.");
        return "redirect:" + PATH;
    }
}
