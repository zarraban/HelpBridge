package com.example.help_bridge.fundraising.fundraiser.service.impl;

import com.example.help_bridge.fundraising.fundraiser.dto.request.AddEvidenceRequest;
import com.example.help_bridge.fundraising.fundraiser.entity.*;
import com.example.help_bridge.fundraising.fundraiser.exception.*;
import com.example.help_bridge.fundraising.fundraiser.security.FundraiserAccessPolicy;
import com.example.help_bridge.fundraising.fundraiser.strategy.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({EvidenceServiceImpl.class, ReceiptValidatorStrategy.class, PhotoValidatorStrategy.class, FeedbackValidatorStrategy.class})
class EvidencePersistenceTest {
    @Autowired EvidenceServiceImpl service;
    @Autowired TestEntityManager em;
    @MockitoBean FundraiserAccessPolicy access;

    Fundraiser fundraiser(FundraiserStatus status) {
        var fundraiser = new Fundraiser(); fundraiser.setStatus(status);
        return em.persistAndFlush(fundraiser);
    }
    @Test void combinedEvidenceAndFeedbackOnlySurviveReload() {
        Long id = fundraiser(FundraiserStatus.IN_PROGRESS).getId();
        var created = service.addEvidenceToFundraiser(id, new AddEvidenceRequest("RCPT-1", "Отримано", "https://files.test/proof.jpg"));
        service.addEvidenceToFundraiser(id, new AddEvidenceRequest(null, "я".repeat(1000), null));
        em.flush(); em.clear();
        var stored = service.getEvidencesByFundraiserId(id);
        assertEquals(2, stored.size());
        assertTrue(stored.stream().anyMatch(e -> e.evidenceId().equals(created.evidenceId()) && "Отримано".equals(e.recipientFeedback())
                && "RCPT-1".equals(e.receiptNumber()) && "https://files.test/proof.jpg".equals(e.attachmentUrl())));
        assertTrue(stored.stream().anyMatch(e -> e.recipientFeedback().length() == 1000));
        assertEquals(FundraiserStatus.IN_PROGRESS, em.find(Fundraiser.class, id).getStatus());
    }
    @Test void everySuppliedEvidenceTypeMustPassItsStrategy() {
        Long id = fundraiser(FundraiserStatus.IN_PROGRESS).getId();
        assertThrows(InvalidEvidenceException.class, () -> service.addEvidenceToFundraiser(id, new AddEvidenceRequest("RCPT-1", "Відгук", "https://files.test/proof.pdf")));
        assertThrows(InvalidEvidenceException.class, () -> service.addEvidenceToFundraiser(id, new AddEvidenceRequest("1234", "Відгук", null)));
        assertThrows(InvalidEvidenceException.class, () -> service.addEvidenceToFundraiser(id, new AddEvidenceRequest(null, null, null)));
        assertTrue(service.getEvidencesByFundraiserId(id).isEmpty());
    }
    @Test void closedFundraiserRejectsEvidenceWithoutSaving() {
        Long id = fundraiser(FundraiserStatus.CLOSED).getId();
        assertThrows(InvalidFundraiserStateException.class, () -> service.addEvidenceToFundraiser(id, new AddEvidenceRequest("RCPT-1", null, null)));
        assertTrue(service.getEvidencesByFundraiserId(id).isEmpty());
    }
}
