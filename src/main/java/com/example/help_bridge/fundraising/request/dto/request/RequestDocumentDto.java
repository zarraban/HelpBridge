package com.example.help_bridge.fundraising.request.dto.request;

import jakarta.validation.constraints.NotBlank;

public class RequestDocumentDto {

    public record CreateDocumentRequest(
            @NotBlank(message = "File name is mandatory")
            String fileName,

            @NotBlank(message = "File URL is mandatory")
            String fileUrl
    ) {}

    public record DocumentResponse(
            Long id,
            String fileName,
            String fileUrl
    ) {}
}