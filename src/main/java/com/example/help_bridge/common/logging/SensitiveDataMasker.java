package com.example.help_bridge.common.logging;

import java.util.regex.Pattern;

public final class SensitiveDataMasker {

    private static final Pattern KEY_VALUE = Pattern.compile(
            "(?i)(\"?(?:password|passwordHash|token|cvv|cvc|bankDetail|iban)\"?\\s*[:=]\\s*)(\"[^\"]*\"|[^,\\s;}\\)]+)");
    private static final Pattern CARD = Pattern.compile("\\b(?:\\d[ -]?){13,19}\\b");
    private static final Pattern IBAN = Pattern.compile("\\bUA\\d{2}[0-9A-Z]{25}\\b");

    private SensitiveDataMasker() {
    }

    public static String mask(String value) {
        if (value == null) {
            return null;
        }
        String masked = KEY_VALUE.matcher(value).replaceAll("$1****");
        masked = IBAN.matcher(masked).replaceAll("UA****");
        return CARD.matcher(masked).replaceAll("****");
    }
}
