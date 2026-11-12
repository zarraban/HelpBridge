package com.example.help_bridge.fundraising.request.web;

import com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.DocumentResponse;
import java.net.URI;

/** Only web URLs are rendered as clickable links in document lists. */
public record RequestDocumentLink(String name, String url) {
    public static RequestDocumentLink from(DocumentResponse document) {
        String url = document.fileUrl();
        if (url != null) {
            try {
                URI uri = URI.create(url);
                if (("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                        && uri.getHost() != null && uri.getUserInfo() == null) {
                    return new RequestDocumentLink(document.fileName(), url);
                }
            } catch (IllegalArgumentException ignored) {
                // Keep the document visible, without an unsafe or malformed link.
            }
        }
        return new RequestDocumentLink(document.fileName(), null);
    }
}
