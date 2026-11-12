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
import jakarta.servlet.http.HttpServletResponse;
import com.example.help_bridge.fundraising.request.security.RequestAccessPolicy;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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

    private final RequestAccessPolicy access;
    private final RequestService requestService;
    private final RequestVerificationService verificationService;
    private final UserRepository userRepository;

    public RequestWebController(RequestService requestService,
                                RequestVerificationService verificationService,
                                UserRepository userRepository, RequestAccessPolicy access) {
        this.access = access;
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

    @PreAuthorize(LIST_ROLES)
    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        var request = requestService.getRequestById(id);
        model.addAttribute("request", request);
        model.addAttribute("permissions", access.currentViewer().permissions(request));
        model.addAttribute("assistanceTypes", requestService.getAssistanceTypes());
        model.addAttribute("documents", request.documents().stream().map(RequestDocumentLink::from).toList());
        return "requests/details";
    }

    @PreAuthorize("hasAnyRole('APPLICANT', 'SYSTEM_ADMIN')")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var request = requestService.getRequestById(id);
        var viewer = access.currentViewer();
        viewer.requireManage(request);
        if (!viewer.permissions(request).edit()) {
            throw new InvalidRequestStateException("Request is no longer pending verification");
        }
        model.addAttribute("form", EditRequestForm.from(request));
        prepareEditForm(id, model);
        return "requests/edit-form";
    }

    @PreAuthorize("hasAnyRole('APPLICANT', 'SYSTEM_ADMIN')")
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") EditRequestForm form,
                         BindingResult bindingResult,
                         Model model,
                         HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {
        var request = requestService.getRequestById(id);
        var viewer = access.currentViewer();
        viewer.requireManage(request);
        prepareEditForm(id, model);
        if (!viewer.permissions(request).edit()) return editConflict(model, response);
        validateOtherType(form.requiresOtherText(), form.otherAssistanceType(), form.needDescription(), bindingResult);
        if (bindingResult.hasErrors()) return "requests/edit-form";
        try {
            requestService.updateRequest(id, form.toDto());
        } catch (InvalidAssistanceTypeException exception) {
            bindingResult.rejectValue("assistanceType", "request.assistanceType.invalid", "Оберіть допустимий тип допомоги");
            return "requests/edit-form";
        } catch (InvalidRequestStateException | ObjectOptimisticLockingFailureException exception) {
            return editConflict(model, response);
        }
        redirectAttributes.addFlashAttribute("message", "Зміни до заявки збережено.");
        return "redirect:/requests/" + id;
    }

    @PreAuthorize("hasAnyRole('APPLICANT', 'SYSTEM_ADMIN')")
    @GetMapping("/{id}/delete")
    public String deleteConfirmation(@PathVariable Long id, Model model) {
        var request = requestService.getRequestById(id);
        var viewer = access.currentViewer();
        viewer.requireManage(request);
        if (!viewer.permissions(request).delete()) {
            throw new InvalidRequestStateException("Request cannot be deleted in its current status");
        }
        model.addAttribute("request", request);
        model.addAttribute("assistanceTypes", requestService.getAssistanceTypes());
        return "requests/delete-confirmation";
    }

    @PreAuthorize("hasAnyRole('APPLICANT', 'SYSTEM_ADMIN')")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            requestService.deleteRequest(id);
            redirectAttributes.addFlashAttribute("message", "Заявку №" + id + " та її документи видалено.");
        } catch (RequestNotFoundException exception) {
            redirectAttributes.addFlashAttribute("error", "Заявку вже видалено або її не знайдено. Список оновлено.");
        } catch (InvalidRequestStateException | ObjectOptimisticLockingFailureException exception) {
            redirectAttributes.addFlashAttribute("error", "Стан заявки змінився або її одночасно опрацювали. Видалення не виконано. Перевірте актуальний список заявок.");
        }
        return "redirect:/requests";
    }

    private void prepareEditForm(Long id, Model model) {
        model.addAttribute("requestId", id);
        model.addAttribute("conflict", false);
        model.addAttribute("assistanceTypes", requestService.getAssistanceTypes());
    }

    private String editConflict(Model model, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        model.addAttribute("conflict", true);
        return "requests/edit-form";
    }

    private void validateOtherType(boolean other, String otherText, String need, BindingResult errors) {
        if (other && (otherText == null || otherText.isBlank())) {
            errors.rejectValue("otherAssistanceType", "request.otherType.required", "Опишіть, який саме тип допомоги");
        } else if (other && need != null && ("[Інше: " + otherText.trim() + "] " + need).length() > 1000) {
            errors.rejectValue("needDescription", "request.need.tooLong", "Опис потреби разом з уточненням типу має містити не більше 1000 символів");
        }
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
        validateOtherType(form.requiresOtherText(), form.otherAssistanceType(), form.needDescription(), bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("assistanceTypes", requestService.getAssistanceTypes());
            return "requests/create-form";
        }
        Long createdId;
        try {
            createdId = requestService.createRequest(form.toDto(currentUserId(authentication))).id();
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
        return "redirect:/requests/" + createdId;
    }

    @PreAuthorize(ADMIN_ROLE)
    @PostMapping("/{id}/review")
    public String review(@PathVariable Long id,
                         @RequestParam boolean approved,
                         RedirectAttributes redirectAttributes) {
        try {
            verificationService.reviewRequest(id, new RequestVerificationDto(approved, null));
            redirectAttributes.addFlashAttribute("message",
                    approved ? "Заявку підтверджено, вона у спільному пулі." : "Заявку відхилено. Вона зберігається зі статусом «Відхилена».");
        } catch (RequestNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Заявку вже видалено або її не знайдено. Список оновлено — перевірте актуальні заявки.");
        } catch (InvalidRequestStateException e) {
            redirectAttributes.addFlashAttribute("error", "Стан заявки змінився або її вже опрацювали. Список оновлено — перевірте доступні дії.");
        }
        redirectAttributes.addFlashAttribute("messageSection", "verification");
        return "redirect:/requests#verification";
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