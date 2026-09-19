package com.example.help_bridge.fund.service;

import com.example.help_bridge.fund.dto.FundCreateRequest;
import com.example.help_bridge.fund.dto.FundResponse;
import com.example.help_bridge.fund.dto.FundStatusUpdateRequest;
import com.example.help_bridge.fund.entity.Fund;
import com.example.help_bridge.fund.entity.FundStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class FundService {

    private final Map<Long, Fund> funds = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<FundResponse> getAllFunds() {
        List<FundResponse> fundResponses = new ArrayList<>();
        funds.forEach((id, fund) -> fundResponses.add(mapFundToDto(id, fund)));
        return fundResponses;
    }

    public FundResponse getFundById(Long id) {
        Fund fund = funds.get(id);
        if (fund == null) {
            return null;
        }
        return mapFundToDto(id, fund);
    }

    public FundResponse createFund(FundCreateRequest request) {
        Long id = idGenerator.getAndIncrement();
        Fund fund = mapRequestDtoToFund(request);
        fund.setId(id);
        funds.put(id, fund);
        return mapFundToDto(id, fund);
    }

    public FundResponse updateFundStatus(Long id, FundStatusUpdateRequest request) {
        Fund fund = funds.get(id);
        if (fund == null) {
            return null;
        }
        fund.setStatus(request.status());
        funds.put(id, fund);
        return mapFundToDto(id, fund);
    }

    public void deleteFundById(Long id) {
        funds.remove(id);
    }

    private FundResponse mapFundToDto(Long id, Fund fund) {
        return new FundResponse(
                id,
                fund.getFundRepresName(),
                fund.getFundRepresSurname(),
                fund.getFundName(),
                fund.getEdrpou(),
                fund.getPhoneNumber(),
                fund.getBankDetail(),
                fund.getRegisteredAddress(),
                fund.getActualAddress(),
                fund.getCorpEmail(),
                fund.getWebsite(),
                fund.getSocialMediaUrls(),
                fund.getDescription(),
                fund.getStatus()
        );
    }

    private Fund mapRequestDtoToFund(FundCreateRequest request) {
        Fund fund = new Fund();
        fund.setFundRepresName(request.fundRepresName());
        fund.setFundRepresSurname(request.fundRepresSurname());
        fund.setFundName(request.fundName());
        fund.setEdrpou(request.edrpou());
        fund.setBankDetail(request.bankDetail());
        fund.setRegisteredAddress(request.registeredAddress());
        fund.setActualAddress(request.actualAddress());
        fund.setPhoneNumber(request.phoneNumber());
        fund.setCorpEmail(request.corpEmail());
        fund.setWebsite(request.website());
        fund.setSocialMediaUrls(request.socialMediaUrl());
        fund.setStatus(FundStatus.PENDING_APPROVAL);
        return fund;
    }
}