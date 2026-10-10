package com.example.help_bridge.fundraising.fundraiser.listener;

import com.example.help_bridge.fundraising.fundraiser.entity.Fundraiser;
import com.example.help_bridge.fundraising.fundraiser.entity.FundraiserStatus;
import com.example.help_bridge.fundraising.fundraiser.repository.FundraiserRepository;
import com.example.help_bridge.fundraising.request.event.RequestBookedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Opens a fundraiser when a fund books a request. Runs synchronously in the
 * booking transaction, so a booked request always has its fundraiser.
 */
@Component
public class RequestBookedListener {

    private static final Logger log = LoggerFactory.getLogger(RequestBookedListener.class);

    private final FundraiserRepository fundraiserRepository;

    public RequestBookedListener(FundraiserRepository fundraiserRepository) {
        this.fundraiserRepository = fundraiserRepository;
    }

    @EventListener
    public void onRequestBooked(RequestBookedEvent event) {
        if (fundraiserRepository.existsByRequestId(event.requestId())) {
            return;
        }
        Fundraiser fundraiser = new Fundraiser();
        fundraiser.setRequestId(event.requestId());
        fundraiser.setStatus(FundraiserStatus.PENDING_ASSIGNMENT);
        fundraiserRepository.save(fundraiser);
        log.info("Fundraiser created for request {} booked by fund {}", event.requestId(), event.fundId());
    }
}
