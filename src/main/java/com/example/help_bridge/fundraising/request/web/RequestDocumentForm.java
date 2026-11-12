package com.example.help_bridge.fundraising.request.web;

import com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.CreateDocumentRequest;
import com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.DocumentResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestDocumentForm(
        @NotBlank(message = "Вкажіть назву документа")
        @Size(max = 255, message = "Назва має містити не більше 255 символів") String fileName,
        @NotBlank(message = "Вкажіть посилання на документ")
        @Size(max = 255, message = "Посилання має містити не більше 255 символів") String fileUrl) {
    public static RequestDocumentForm empty() { return new RequestDocumentForm("", ""); }
    public static RequestDocumentForm from(DocumentResponse document) {
        return new RequestDocumentForm(document.fileName(), document.fileUrl());
    }
    public CreateDocumentRequest toDto() { return new CreateDocumentRequest(fileName.trim(), fileUrl.trim()); }
}
