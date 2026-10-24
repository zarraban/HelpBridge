package com.example.help_bridge.fundraising.request.web;

import com.example.help_bridge.fundraising.request.dto.request.RequestVerificationDto;
import com.example.help_bridge.fundraising.request.entity.RequestStatus;
import com.example.help_bridge.fundraising.request.exception.InvalidAssistanceTypeException;
import com.example.help_bridge.fundraising.request.exception.InvalidRequestStateException;
import com.example.help_bridge.fundraising.request.exception.RequestNotFoundException;
import com.example.help_bridge.fundraising.request.exception.RequesterNotFoundException;
import com.example.help_bridge.fundraising.request.service.RequestService;
import com.example.help_bridge.fundraising.request.service.RequestVerificationService;
import com.example.help_bridge.fundraising.user.entity.User;
import com.example.help_bridge.fundraising.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/requests")
public class RequestWebController {

    private static final String LIST_ROLES = "hasAnyRole('APPLICANT', 'FUND_REPRESENTATIVE', 'SYSTEM_ADMIN')";
    private static final String APPLICANT_ROLE = "hasRole('APPLICANT')";
    private static final String ADMIN_ROLE = "hasRole('SYSTEM_ADMIN')";

    private final RequestService requestService;
    private final RequestVerificationService verificationService;
    private final UserRepository userRepository;

    public RequestWebController(RequestService requestService,
                                RequestVerificationService verificationService,
                                UserRepository userRepository) {
        this.requestService = requestService;
        this.verificationService = verificationService;
        this.userRepository = userRepository;
    }

    @PreAuthorize(LIST_ROLES)
    @GetMapping
    public String list(Authentication authentication, Model model) {
        boolean ownRequests = hasRole(authentication, "ROLE_APPLICANT");
        model.addAttribute("ownRequests", ownRequests);
        model.addAttribute("requests", ownRequests
                ? requestService.getRequestsByUser(currentUserId(authentication))
                : requestService.getSharedPool());
        if (hasRole(authentication, "ROLE_SYSTEM_ADMIN")) {
            model.addAttribute("pendingRequests",
                    requestService.getAllRequests(RequestStatus.PENDING_VERIFICATION));
        }
        model.addAttribute("assistanceTypes", requestService.getAssistanceTypes());
        return "requests/list";
    }

    @PreAuthorize(APPLICANT_ROLE)
    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", CreateRequestForm.empty());
        }
        model.addAttribute("assistanceTypes", requestService.getAssistanceTypes());
        return "requests/create-form";
    }

    @PreAuthorize(APPLICANT_ROLE)
    @PostMapping
    public String create(@Valid @ModelAttribute("form") CreateRequestForm form,
                         BindingResult bindingResult,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (form.requiresOtherText()
                && (form.otherAssistanceType() == null || form.otherAssistanceType().isBlank())) {
            bindingResult.rejectValue("otherAssistanceType", "request.otherType.required",
                    "Опишіть, який саме тип допомоги");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("assistanceTypes", requestService.getAssistanceTypes());
            return "requests/create-form";
        }
        try {
            requestService.createRequest(form.toDto(currentUserId(authentication)));
        } catch (InvalidAssistanceTypeException e) {
            bindingResult.rejectValue("assistanceType", "request.assistanceType.invalid",
                    "Оберіть допустимий тип допомоги");
            model.addAttribute("assistanceTypes", requestService.getAssistanceTypes());
            return "requests/create-form";
        } catch (RequesterNotFoundException e) {
            bindingResult.reject("request.requester.notFound",
                    "Не вдалося визначити вашого користувача. Увійдіть у систему ще раз");
            model.addAttribute("assistanceTypes", requestService.getAssistanceTypes());
            return "requests/create-form";
        }
        redirectAttributes.addFlashAttribute("message",
                "Заявку подано. Вона з'явиться у спільному пулі після підтвердження адміністратором.");
        return "redirect:/requests";
    }

    @PreAuthorize(ADMIN_ROLE)
    @PostMapping("/{id}/review")
    public String review(@PathVariable Long id,
                         @RequestParam boolean approved,
                         RedirectAttributes redirectAttributes) {
        try {
            verificationService.reviewRequest(id, new RequestVerificationDto(approved, null));
            redirectAttributes.addFlashAttribute("message",
                    approved ? "Заявку підтверджено, вона у спільному пулі." : "Заявку відхилено та видалено.");
        } catch (RequestNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Заявку не знайдено.");
        } catch (InvalidRequestStateException e) {
            redirectAttributes.addFlashAttribute("error", "Цю заявку вже не можна опрацювати.");
        }
        return "redirect:/requests";
    }

    private static boolean hasRole(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> authority.equals(a.getAuthority()));
    }

    private Long currentUserId(Authentication authentication) {
        String username = authentication.getName();
        String email = username.substring(username.indexOf(':') + 1);
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new AccessDeniedException("Applicant account was not found"));
    }
}