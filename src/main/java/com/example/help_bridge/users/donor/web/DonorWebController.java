package com.example.help_bridge.users.donor.web;

import com.example.help_bridge.common.web.ManagementPages;
import com.example.help_bridge.common.web.ManagementPages.Row;
import com.example.help_bridge.users.donor.dto.request.DonorRequest;
import com.example.help_bridge.users.donor.service.DonorService;
import com.example.help_bridge.fundraising.fundraiser.exception.FundraiserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static com.example.help_bridge.common.web.ManagementPages.field;

@Controller
@RequestMapping("/donors")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class DonorWebController {
    private static final String PATH = "/donors";
    private static final int PAGE_SIZE = 10;
    private final DonorService service;

    @InitBinder("form")
    void bindForm(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, "phone", new StringTrimmerEditor(true));
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "asc") String sort,
                       @RequestParam(defaultValue = "1") long page, Model model) {
        if (!List.of("asc", "desc").contains(sort) || page < 1 || page >= Integer.MAX_VALUE / PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        model.addAttribute("page", page);
        model.addAttribute("sort", sort);
        var donors = service.getDonorPage(PageRequest.of((int) page - 1, PAGE_SIZE,
                Sort.by(Sort.Direction.fromString(sort), "createdAt", "id")));
        model.addAttribute("hasNext", donors.hasNext());
        var rows = donors.stream()
                .map(item -> new Row(item.id(), List.of(item.firstName(), item.lastName(), item.email(), String.valueOf(item.fundraiserId()))))
                .toList();
        return ManagementPages.list(model, "Донори", PATH,
                List.of("Ім’я", "Прізвище", "Електронна пошта", "Збір"), rows);
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new DonorRequest(null, null, null, null, null));
        return createPage(model);
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") DonorRequest form,
                         BindingResult errors, Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) return createPage(model);
        try {
            service.addNewDonor(form);
        } catch (FundraiserNotFoundException exception) {
            errors.rejectValue("fundraiserId", "missing", "Збір не знайдено.");
        }
        if (errors.hasErrors()) return createPage(model);
        redirect.addFlashAttribute("message", "Запис створено.");
        return "redirect:" + PATH;
    }

    private String createPage(Model model) {
        return ManagementPages.form(model, "Додати донора", PATH + "/new", PATH,
                field("firstName", "Ім’я"),
                field("lastName", "Прізвище"),
                field("email", "Електронна пошта", "email"),
                field("phone", "Телефон", "tel"),
                field("fundraiserId", "Номер збору", "number"));
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var item = service.getDonorById(id);
        model.addAttribute("form", new DonorRequest(item.fundraiserId(), item.firstName(), item.lastName(), item.email(), item.phone()));
        return editPage(id, model);
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute("form") DonorRequest form,
                       BindingResult errors, Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) return editPage(id, model);
        try {
            service.updateDonorFields(id, form);
        } catch (FundraiserNotFoundException exception) {
            errors.rejectValue("fundraiserId", "missing", "Збір не знайдено.");
        }
        if (errors.hasErrors()) return editPage(id, model);
        redirect.addFlashAttribute("message", "Зміни збережено.");
        return "redirect:" + PATH;
    }

    private String editPage(Long id, Model model) {
        return ManagementPages.form(model, "Редагувати донора", PATH + "/" + id + "/edit", PATH,
                field("firstName", "Ім’я"),
                field("lastName", "Прізвище"),
                field("email", "Електронна пошта", "email"),
                field("phone", "Телефон", "tel"),
                field("fundraiserId", "Номер збору", "number"));
    }

    @GetMapping("/{id}/delete")
    public String deleteConfirmation(@PathVariable Long id, Model model) {
        var item = service.getDonorById(id);
        return ManagementPages.delete(model, item.firstName() + " " + item.lastName(), PATH + "/" + id + "/delete", PATH);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        service.deleteDonorById(id);
        redirect.addFlashAttribute("message", "Запис видалено.");
        return "redirect:" + PATH;
    }
}
