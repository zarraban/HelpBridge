package com.example.help_bridge.users.volunteer.service;

import com.example.help_bridge.users.volunteer.command.RegisterVolunteerCommand;
import com.example.help_bridge.users.volunteer.command.UpdateVolunteerCommand;
import com.example.help_bridge.users.volunteer.dto.response.VolunteerResponse;
import com.example.help_bridge.users.volunteer.entity.Volunteer;
import com.example.help_bridge.users.volunteer.entity.VolunteerStatus;
import com.example.help_bridge.users.volunteer.event.VolunteerRegisteredEvent;
import com.example.help_bridge.users.volunteer.event.VolunteerRemovedEvent;
import com.example.help_bridge.users.volunteer.event.VolunteerUpdatedEmailEvent;
import com.example.help_bridge.users.volunteer.event.VolunteerUpdatedPhoneNumberEvent;
import com.example.help_bridge.users.volunteer.exception.DuplicateVolunteerException;
import com.example.help_bridge.users.volunteer.exception.VolunteerNotFoundException;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VolunteerServiceImplTest {

    private static final Long FUND_ID = 1L;
    private static final Long VOLUNTEER_ID = 3L;

    private static final String FIRST_NAME = "Anna";
    private static final String LAST_NAME = "Samana";
    private static final String EMAIL = "anna@gmail.com";
    private static final String PHONE = "+380501234567";

    @Mock
    private VolunteerRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private VolunteerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new VolunteerServiceImpl(repository, eventPublisher);
    }

    private static Volunteer volunteer(Long id, VolunteerStatus status) {
        return new Volunteer(id, FUND_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE, status);
    }

    private static RegisterVolunteerCommand registerCommand() {
        return new RegisterVolunteerCommand(FUND_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE);
    }

    private static UpdateVolunteerCommand updateCommand(String firstName, String email, String phone) {
        return new UpdateVolunteerCommand(VOLUNTEER_ID, FUND_ID, firstName, LAST_NAME, email, phone);
    }

    @Test
    void getFundVolunteers_returnsActiveVolunteers() {
        when(repository.findAllByFundIdAndStatus(FUND_ID, VolunteerStatus.ACTIVE))
                .thenReturn(List.of(volunteer(VOLUNTEER_ID, VolunteerStatus.ACTIVE)));

        List<VolunteerResponse> responses = service.getFundVolunteers(FUND_ID);

        assertEquals(1, responses.size());
        assertEquals(FIRST_NAME, responses.get(0).firstName());
    }

    @Test
    void registerVolunteer_savesAndPublishesEvent_whenNew() {
        when(repository.findByFundIdAndEmailIgnoreCase(FUND_ID, EMAIL)).thenReturn(Optional.empty());
        when(repository.findByFundIdAndPhone(FUND_ID, PHONE)).thenReturn(Optional.empty());
        when(repository.save(any(Volunteer.class))).thenAnswer(inv -> {
            Volunteer v = inv.getArgument(0);
            v.setId(VOLUNTEER_ID);
            return v;
        });

        VolunteerResponse response = service.registerVolunteer(registerCommand());

        assertNotNull(response);
        assertEquals(VOLUNTEER_ID, response.id());
        verify(eventPublisher).publishEvent(any(VolunteerRegisteredEvent.class));
    }

    @Test
    void updateVolunteer_updatesDataAndPublishesEvents() {
        Volunteer existing = volunteer(VOLUNTEER_ID, VolunteerStatus.ACTIVE);
        when(repository.findByFundIdAndIdAndStatus(FUND_ID, VOLUNTEER_ID, VolunteerStatus.ACTIVE))
                .thenReturn(Optional.of(existing));
        when(repository.save(any(Volunteer.class))).thenAnswer(inv -> inv.getArgument(0));

        VolunteerResponse response = service.updateVolunteer(updateCommand("NewName", "new@gmail.com", PHONE));

        assertEquals("NewName", response.firstName());
        verify(eventPublisher).publishEvent(any(VolunteerUpdatedEmailEvent.class));
    }

    @Test
    void removeVolunteer_setsStatusInactiveAndPublishesEvent() {
        Volunteer existing = volunteer(VOLUNTEER_ID, VolunteerStatus.ACTIVE);
        when(repository.findByFundIdAndIdAndStatus(FUND_ID, VOLUNTEER_ID, VolunteerStatus.ACTIVE))
                .thenReturn(Optional.of(existing));

        service.removeVolunteer(FUND_ID, VOLUNTEER_ID);

        assertEquals(VolunteerStatus.INACTIVE, existing.getStatus());
        verify(repository).save(existing);
        verify(eventPublisher).publishEvent(any(VolunteerRemovedEvent.class));
    }
}