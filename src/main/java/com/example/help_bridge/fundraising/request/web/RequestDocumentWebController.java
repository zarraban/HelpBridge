package com.example.help_bridge.fundraising.request.web;

import com.example.help_bridge.fundraising.request.dto.request.RequestDto.RequestResponse;
import com.example.help_bridge.fundraising.request.exception.*;
import com.example.help_bridge.fundraising.request.security.RequestAccessPolicy;
import com.example.help_bridge.fundraising.request.service.RequestService;
import com.example.help_bridge.fundraising.request.service.RequestDocumentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/requests/{requestId}/documents")
@PreAuthorize("hasAnyRole('APPLICANT', 'SYSTEM_ADMIN')")
public class RequestDocumentWebController {
    private final RequestService requests;
    private final RequestDocumentService documents;
    private final RequestAccessPolicy access;

    public RequestDocumentWebController(RequestService requests, RequestDocumentService documents, RequestAccessPolicy access) {
        this.requests = requests;
        this.documents = documents;
        this.access = access;
    }

    @GetMapping("/new")
    public String newForm(@PathVariable Long requestId, Model model) {
        ensureEditable(manageableRequest(requestId));
        prepare(requestId, null, model);
        model.addAttribute("form", RequestDocumentForm.empty());
        return "requests/document-form";
    }

    @GetMapping("/{documentId}/edit")
    public String editForm(@PathVariable Long requestId, @PathVariable Long documentId, Model model) {
        ensureEditable(manageableRequest(requestId));
        model.addAttribute("form", RequestDocumentForm.from(documents.getDocument(requestId, documentId)));
        prepare(requestId, documentId, model);
        return "requests/document-form";
    }

    @PostMapping
    public String create(@PathVariable Long requestId, @Valid @ModelAttribute("form") RequestDocumentForm form,
                         BindingResult errors, Model model, HttpServletResponse response, RedirectAttributes redirect) {
        return save(requestId, null, form, errors, model, response, redirect);
    }

    @PostMapping("/{documentId}")
    public String update(@PathVariable Long requestId, @PathVariable Long documentId,
                         @Valid @ModelAttribute("form") RequestDocumentForm form,
                         BindingResult errors, Model model, HttpServletResponse response, RedirectAttributes redirect) {
        return save(requestId, documentId, form, errors, model, response, redirect);
    }

    private String save(Long requestId, Long documentId, RequestDocumentForm form, BindingResult errors,
                        Model model, HttpServletResponse response, RedirectAttributes redirect) {
        var request = manageableRequest(requestId);
        if (documentId != null) documents.getDocument(requestId, documentId);
        prepare(requestId, documentId, model);
        if (!access.currentViewer().permissions(request).manageDocuments()) return conflict(model, response);
        if (form.fileUrl() != null && !form.fileUrl().isBlank() && !RequestDocumentLink.isSafeWebUrl(form.fileUrl())) {
            errors.rejectValue("fileUrl", "document.url.invalid", "Вкажіть повне HTTP/HTTPS-посилання без логіна та пароля");
        }
        if (errors.hasErrors()) return "requests/document-form";
        try {
            if (documentId == null) documents.addDocument(requestId, form.toDto());
            else documents.updateDocument(requestId, documentId, form.toDto());
        } catch (InvalidRequestStateException | ObjectOptimisticLockingFailureException exception) {
            return conflict(model, response);
        }
        redirect.addFlashAttribute("message", documentId == null ? "Документ додано до заявки." : "Дані документа збережено.");
        return details(requestId);
    }

    @GetMapping("/{documentId}/delete")
    public String deleteForm(@PathVariable Long requestId, @PathVariable Long documentId, Model model) {
        ensureEditable(manageableRequest(requestId));
        prepare(requestId, documentId, model);
        model.addAttribute("document", documents.getDocument(requestId, documentId));
        return "requests/document-delete";
    }

    @PostMapping("/{documentId}/delete")
    public String delete(@PathVariable Long requestId, @PathVariable Long documentId, RedirectAttributes redirect) {
        try {
            documents.deleteDocument(requestId, documentId);
            redirect.addFlashAttribute("message", "Документ видалено із заявки.");
        } catch (RequestDocumentNotFoundException exception) {
            redirect.addFlashAttribute("error", "Документ уже видалено або його не знайдено. Перевірте актуальний список.");
        } catch (RequestNotFoundException exception) {
            redirect.addFlashAttribute("error", "Заявку вже видалено або її не знайдено.");
            return "redirect:/requests";
        } catch (InvalidRequestStateException | ObjectOptimisticLockingFailureException exception) {
            redirect.addFlashAttribute("error", "Стан заявки змінився. Видалення документа не виконано.");
        }
        return details(requestId);
    }

    private RequestResponse manageableRequest(Long id) {
        var request = requests.getRequestById(id);
        access.currentViewer().requireManage(request);
        return request;
    }

    private void ensureEditable(RequestResponse request) {
        if (!access.currentViewer().permissions(request).manageDocuments()) {
            throw new InvalidRequestStateException("Documents are locked after verification");
        }
    }

    private void prepare(Long requestId, Long documentId, Model model) {
        model.addAttribute("requestId", requestId);
        model.addAttribute("documentId", documentId);
        model.addAttribute("editing", documentId != null);
        model.addAttribute("conflict", false);
    }

    private String conflict(Model model, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        model.addAttribute("conflict", true);
        return "requests/document-form";
    }

    private String details(Long requestId) { return "redirect:/requests/" + requestId + "#documents"; }
}
