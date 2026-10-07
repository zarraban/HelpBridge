package com.example.help_bridge.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskingMessageConverterTest {

    private MaskingMessageConverter converter;

    @BeforeEach
    void setUp() {
        converter = new MaskingMessageConverter();
        converter.start();
    }

    private String mask(String message) {
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMessage(message);
        return converter.convert(event);
    }

    @Test
    void masksPasswordInKeyValueFormat() {
        String result = mask("Create admin: email=a@b.com, password=secret123, firstName=Ivan");

        assertThat(result).doesNotContain("secret123");
        assertThat(result).contains("password=****");
        assertThat(result).contains("email=a@b.com");
    }

    @Test
    void masksPasswordInJson() {
        String result = mask("{\"email\":\"a@b.com\",\"password\":\"secret123\"}");

        assertThat(result).doesNotContain("secret123");
        assertThat(result).contains("a@b.com");
    }

    @Test
    void masksPasswordInsideRecordToString() {
        String result = mask("FundRepresentativeRequest[firstName=Ivan, password=secret123]");

        assertThat(result).doesNotContain("secret123");
    }

    @Test
    void masksBankDetailAndIban() {
        String result = mask("Fund: bankDetail=UA213223130000026007233566001");

        assertThat(result).doesNotContain("UA213223130000026007233566001");
    }

    @Test
    void masksStandaloneIban() {
        String result = mask("Transfer to UA213223130000026007233566001 done");

        assertThat(result).doesNotContain("26007233566001");
        assertThat(result).contains("UA****");
    }


    @Test
    void masksCardNumber() {
        String result = mask("Payment with card 4111 1111 1111 1111 failed");

        assertThat(result).doesNotContain("4111");
        assertThat(result).contains("****");
    }

    @Test
    void leavesRegularMessageUnchanged() {
        String message = "Fund 5 approved by admin 2";

        assertThat(mask(message)).isEqualTo(message);
    }
}