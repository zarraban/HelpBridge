package com.example.help_bridge.fundraising.request.web;

import com.example.help_bridge.fundraising.request.dto.request.RequestDocumentDto.DocumentResponse;
import java.net.URI;

/** Only web URLs are rendered as clickable links in document lists. */
public record RequestDocumentLink(Long id, String name, String url) {
    public static RequestDocumentLink from(DocumentResponse document) {
        String url = document.fileUrl();
        return new RequestDocumentLink(document.id(), document.fileName(), isSafeWebUrl(url) ? url.trim() : null);
    }

    public static boolean isSafeWebUrl(String url) {
        if (url == null) return false;
        try {
            URI uri = URI.create(url.trim());
            return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null && uri.getUserInfo() == null;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
