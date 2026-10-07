package com.example.help_bridge.common.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

public class MaskingMessageConverter extends MessageConverter {

    private static final Pattern KEY_VALUE = Pattern.compile(
            "(?i)(\"?(?:password|passwordHash|token|cvv|cvc|bankDetail|iban)\"?\\s*[:=]\\s*)(\"[^\"]*\"|[^,\\s;}\\)]+)");
    private static final Pattern CARD = Pattern.compile("\\b(?:\\d[ -]?){13,19}\\b");
    private static final Pattern IBAN = Pattern.compile("\\bUA\\d{2}[0-9A-Z]{25}\\b");

    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);
        message = KEY_VALUE.matcher(message).replaceAll("$1****");
        message = IBAN.matcher(message).replaceAll("UA****");
        return CARD.matcher(message).replaceAll("****");
    }
}
