package com.example.help_bridge.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class Fund {
    private Long id;
    private String fundRepresName;
    private String fundRepresSurname;
    private String fundName;
    private String edrpou;
    private String bankDetail;
    private String registeredAddress;
    private String actualAddress;
    private String phoneNumber;
    private String corpEmail;
    private String website;
    private Map<String, String> socialMediaUrls;
    private String description;
    private FundStatus status;
}
