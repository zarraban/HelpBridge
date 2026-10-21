package com.example.help_bridge.fundraising.request.web;

import com.example.help_bridge.fundraising.request.exception.InvalidAssistanceTypeException;
import com.example.help_bridge.fundraising.request.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/requests")
public class RequestWebController {

    private final RequestService requestService;

    public RequestWebController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("requests", requestService.getSharedPool());
        model.addAttribute("assistanceTypes", requestService.getAssistanceTypes());
        return "requests/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", CreateRequestForm.empty());
        }
        model.addAttribute("assistanceTypes", requestService.getAssistanceTypes());
        return "requests/create-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") CreateRequestForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("assistanceTypes", requestService.getAssistanceTypes());
            return "requests/create-form";
        }
        try {
            requestService.createRequest(form.toDto());
        } catch (InvalidAssistanceTypeException e) {
            bindingResult.rejectValue("assistanceType", "request.assistanceType.invalid",
                    "Оберіть допустимий тип допомоги");
            model.addAttribute("assistanceTypes", requestService.getAssistanceTypes());
            return "requests/create-form";
        } catch (RuntimeException e) {
            bindingResult.rejectValue("userId", "request.create.failed",
                    "Не вдалося створити заявку. Перевірте ID користувача та спробуйте ще раз");
            model.addAttribute("assistanceTypes", requestService.getAssistanceTypes());
            return "requests/create-form";
        }
        redirectAttributes.addFlashAttribute("message", "Заявку подано. Вона з'явиться у списку після підтвердження адміністратором.");
        return "redirect:/requests";
    }
}