package com.example.help_bridge.volunteer.service;

import com.example.help_bridge.volunteer.command.RegisterVolunteerCommand;
import com.example.help_bridge.volunteer.command.UpdateVolunteerCommand;
import com.example.help_bridge.volunteer.dto.response.VolunteerResponse;
import com.example.help_bridge.volunteer.entity.Volunteer;
import com.example.help_bridge.volunteer.entity.VolunteerStatus;
import com.example.help_bridge.volunteer.event.VolunteerRegisteredEvent;
import com.example.help_bridge.volunteer.event.VolunteerRemovedEvent;
import com.example.help_bridge.volunteer.event.VolunteerUpdatedEmailEvent;
import com.example.help_bridge.volunteer.event.VolunteerUpdatedPhoneNumberEvent;
import com.example.help_bridge.volunteer.exception.DuplicateVolunteerException;
import com.example.help_bridge.volunteer.exception.VolunteerNotFoundException;
import com.example.help_bridge.volunteer.repository.VolunteerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VolunteerServiceImplTest {

    // Навмисно різні значення, щоб тести ловили переплутані id / fundId
    private static final Long FUND_ID = 1L;
    private static final Long VOLUNTEER_ID = 3L;
    private static final Long OTHER_VOLUNTEER_ID = 4L;

    private static final String FIRST_NAME = "Anna";
    private static final String LAST_NAME = "Samana";
    private static final String EMAIL = "anna@gmail.com";
    private static final String PHONE = "+380501234567";

    private static final String NEW_EMAIL = "anna.new@gmail.com";
    private static final String NEW_PHONE = "+380671112233";

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

    private static Volunteer volunteer(Long id, String email, String phone, VolunteerStatus status) {
        return new Volunteer(id, FUND_ID, "Olena", "Koval", email, phone, status);
    }

    private static RegisterVolunteerCommand registerCommand() {
        return new RegisterVolunteerCommand(FUND_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE);
    }

    private static UpdateVolunteerCommand updateCommand(String firstName, String email, String phone) {
        return new UpdateVolunteerCommand(VOLUNTEER_ID, FUND_ID, firstName, LAST_NAME, email, phone);
    }

    private void saveReturnsArgumentWithId(Long id) {
        when(repository.save(any(Volunteer.class))).thenAnswer(inv -> {
            Volunteer v = inv.getArgument(0);
            if (v.getId() == null) {
                v.setId(id);
            }
            return v;
        });
    }

    private void saveReturnsArgument() {
        when(repository.save(any(Volunteer.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Volunteer capturedSavedVolunteer() {
        ArgumentCaptor<Volunteer> captor = ArgumentCaptor.forClass(Volunteer.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Nested
    class GetFundVolunteers {

        @Test
        void mapsEveryVolunteerOfFundToResponse() {
            when(repository.findAllByFund(FUND_ID)).thenReturn(List.of(
                    volunteer(VOLUNTEER_ID, VolunteerStatus.ACTIVE),
                    volunteer(OTHER_VOLUNTEER_ID, NEW_EMAIL, NEW_PHONE, VolunteerStatus.ACTIVE)));

            List<VolunteerResponse> result = service.getFundVolunteers(FUND_ID);

            assertEquals(List.of(
                    new VolunteerResponse(VOLUNTEER_ID, FUND_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE),
                    new VolunteerResponse(OTHER_VOLUNTEER_ID, FUND_ID, "Olena", "Koval", NEW_EMAIL, NEW_PHONE)
            ), result);
        }

        @Test
        void returnsEmptyListWhenFundHasNoVolunteers() {
            when(repository.findAllByFund(FUND_ID)).thenReturn(List.of());

            assertTrue(service.getFundVolunteers(FUND_ID).isEmpty());
        }
    }

    @Nested
    class GetFundVolunteer {

        @Test
        void returnsVolunteerWithCorrectIdAndFundId() {
            when(repository.findById(FUND_ID, VOLUNTEER_ID))
                    .thenReturn(Optional.of(volunteer(VOLUNTEER_ID, VolunteerStatus.ACTIVE)));

            VolunteerResponse response = service.getFundVolunteer(FUND_ID, VOLUNTEER_ID);

            assertEquals(new VolunteerResponse(VOLUNTEER_ID, FUND_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE), response);
        }

        @Test
        void throwsNotFoundWhenVolunteerIsAbsent() {
            when(repository.findById(FUND_ID, VOLUNTEER_ID)).thenReturn(Optional.empty());

            assertThrows(VolunteerNotFoundException.class,
                    () -> service.getFundVolunteer(FUND_ID, VOLUNTEER_ID));
        }
    }

    @Nested
    class RegisterVolunteer {

        @Test
        void savesNewActiveVolunteerOfCommandFund() {
            saveReturnsArgumentWithId(VOLUNTEER_ID);

            VolunteerResponse response = service.registerVolunteer(registerCommand());

            Volunteer saved = capturedSavedVolunteer();
            assertEquals(FUND_ID, saved.getFundId());
            assertEquals(FIRST_NAME, saved.getFirstName());
            assertEquals(LAST_NAME, saved.getLastName());
            assertEquals(EMAIL, saved.getEmail());
            assertEquals(PHONE, saved.getPhoneNumber());
            assertEquals(VolunteerStatus.ACTIVE, saved.getStatus());

            assertEquals(new VolunteerResponse(VOLUNTEER_ID, FUND_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE), response);
        }

        @Test
        void publishesRegisteredEventAfterSaving() {
            saveReturnsArgumentWithId(VOLUNTEER_ID);

            service.registerVolunteer(registerCommand());

            InOrder inOrder = inOrder(repository, eventPublisher);
            inOrder.verify(repository).save(any(Volunteer.class));
            inOrder.verify(eventPublisher).publishEvent(new VolunteerRegisteredEvent(FUND_ID, FIRST_NAME, EMAIL));
        }

        @Test
        void throwsDuplicateWhenActiveVolunteerHasSameEmail() {
            when(repository.findByEmail(FUND_ID, EMAIL))
                    .thenReturn(Optional.of(volunteer(OTHER_VOLUNTEER_ID, VolunteerStatus.ACTIVE)));

            assertThrows(DuplicateVolunteerException.class, () -> service.registerVolunteer(registerCommand()));

            verify(repository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void storesEmailInLowerCase() {
            saveReturnsArgumentWithId(VOLUNTEER_ID);

            VolunteerResponse response = service.registerVolunteer(
                    new RegisterVolunteerCommand(FUND_ID, FIRST_NAME, LAST_NAME, "Anna@Gmail.COM", PHONE));

            assertEquals(EMAIL, capturedSavedVolunteer().getEmail());
            assertEquals(EMAIL, response.email());
            verify(eventPublisher).publishEvent(new VolunteerRegisteredEvent(FUND_ID, FIRST_NAME, EMAIL));
        }

        @Test
        void throwsDuplicateWhenActiveVolunteerHasSameEmailInDifferentCase() {
            when(repository.findByEmail(FUND_ID, EMAIL))
                    .thenReturn(Optional.of(volunteer(OTHER_VOLUNTEER_ID, VolunteerStatus.ACTIVE)));

            assertThrows(DuplicateVolunteerException.class, () -> service.registerVolunteer(
                    new RegisterVolunteerCommand(FUND_ID, FIRST_NAME, LAST_NAME, "ANNA@gmail.com", PHONE)));

            verify(repository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void throwsDuplicateWhenActiveVolunteerHasSamePhoneNumber() {
            when(repository.findByPhoneNumber(FUND_ID, PHONE))
                    .thenReturn(Optional.of(volunteer(OTHER_VOLUNTEER_ID, NEW_EMAIL, PHONE, VolunteerStatus.ACTIVE)));

            assertThrows(DuplicateVolunteerException.class, () -> service.registerVolunteer(registerCommand()));

            verify(repository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void throwsDuplicateWhenEmailIsFreeButPhoneBelongsToActiveVolunteerAndEmailToInactive() {
            when(repository.findByEmail(FUND_ID, EMAIL))
                    .thenReturn(Optional.of(volunteer(VOLUNTEER_ID, EMAIL, NEW_PHONE, VolunteerStatus.INACTIVE)));
            when(repository.findByPhoneNumber(FUND_ID, PHONE))
                    .thenReturn(Optional.of(volunteer(OTHER_VOLUNTEER_ID, NEW_EMAIL, PHONE, VolunteerStatus.ACTIVE)));

            assertThrows(DuplicateVolunteerException.class, () -> service.registerVolunteer(registerCommand()));

            verify(repository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void reactivatesInactiveVolunteerFoundByEmailInsteadOfCreatingNewOne() {
            Volunteer inactive = volunteer(VOLUNTEER_ID, EMAIL, NEW_PHONE, VolunteerStatus.INACTIVE);
            when(repository.findByEmail(FUND_ID, EMAIL)).thenReturn(Optional.of(inactive));
            saveReturnsArgument();

            VolunteerResponse response = service.registerVolunteer(registerCommand());

            Volunteer saved = capturedSavedVolunteer();
            assertEquals(VOLUNTEER_ID, saved.getId());
            assertEquals(FUND_ID, saved.getFundId());
            assertEquals(VolunteerStatus.ACTIVE, saved.getStatus());
            assertEquals(FIRST_NAME, saved.getFirstName());
            assertEquals(LAST_NAME, saved.getLastName());
            assertEquals(PHONE, saved.getPhoneNumber());

            assertEquals(new VolunteerResponse(VOLUNTEER_ID, FUND_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE), response);
            verify(eventPublisher).publishEvent(new VolunteerRegisteredEvent(FUND_ID, FIRST_NAME, EMAIL));
        }

        @Test
        void reactivatesInactiveVolunteerFoundByPhoneNumber() {
            Volunteer inactive = volunteer(VOLUNTEER_ID, NEW_EMAIL, PHONE, VolunteerStatus.INACTIVE);
            when(repository.findByPhoneNumber(FUND_ID, PHONE)).thenReturn(Optional.of(inactive));
            saveReturnsArgument();

            VolunteerResponse response = service.registerVolunteer(registerCommand());

            Volunteer saved = capturedSavedVolunteer();
            assertEquals(VOLUNTEER_ID, saved.getId());
            assertEquals(VolunteerStatus.ACTIVE, saved.getStatus());
            assertEquals(EMAIL, saved.getEmail());

            assertEquals(new VolunteerResponse(VOLUNTEER_ID, FUND_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE), response);
            verify(eventPublisher).publishEvent(new VolunteerRegisteredEvent(FUND_ID, FIRST_NAME, EMAIL));
        }

        @Test
        void reactivatesWhenEmailAndPhoneBelongToSameInactiveVolunteer() {
            Volunteer inactive = volunteer(VOLUNTEER_ID, VolunteerStatus.INACTIVE);
            when(repository.findByEmail(FUND_ID, EMAIL)).thenReturn(Optional.of(inactive));
            when(repository.findByPhoneNumber(FUND_ID, PHONE)).thenReturn(Optional.of(inactive));
            saveReturnsArgument();

            service.registerVolunteer(registerCommand());

            Volunteer saved = capturedSavedVolunteer();
            assertEquals(VOLUNTEER_ID, saved.getId());
            assertEquals(VolunteerStatus.ACTIVE, saved.getStatus());
        }

        @Test
        void throwsDuplicateWhenEmailAndPhoneBelongToDifferentInactiveVolunteers() {
            when(repository.findByEmail(FUND_ID, EMAIL))
                    .thenReturn(Optional.of(volunteer(VOLUNTEER_ID, EMAIL, NEW_PHONE, VolunteerStatus.INACTIVE)));
            when(repository.findByPhoneNumber(FUND_ID, PHONE))
                    .thenReturn(Optional.of(volunteer(OTHER_VOLUNTEER_ID, NEW_EMAIL, PHONE, VolunteerStatus.INACTIVE)));

            assertThrows(DuplicateVolunteerException.class, () -> service.registerVolunteer(registerCommand()));

            verify(repository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void doesNotPublishEventWhenSavingFails() {
            when(repository.save(any(Volunteer.class))).thenThrow(new IllegalStateException("storage is down"));

            assertThrows(IllegalStateException.class, () -> service.registerVolunteer(registerCommand()));

            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    class UpdateVolunteer {

        private Volunteer existing;

        @BeforeEach
        void setUpExisting() {
            existing = volunteer(VOLUNTEER_ID, VolunteerStatus.ACTIVE);
        }

        @Test
        void throwsNotFoundWhenVolunteerIsAbsent() {
            when(repository.findById(FUND_ID, VOLUNTEER_ID)).thenReturn(Optional.empty());

            assertThrows(VolunteerNotFoundException.class,
                    () -> service.updateVolunteer(updateCommand(FIRST_NAME, NEW_EMAIL, PHONE)));

            verify(repository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void updatesExistingVolunteerKeepingIdFundAndStatus() {
            when(repository.findById(FUND_ID, VOLUNTEER_ID)).thenReturn(Optional.of(existing));
            saveReturnsArgument();

            VolunteerResponse response = service.updateVolunteer(updateCommand("Hanna", NEW_EMAIL, NEW_PHONE));

            Volunteer saved = capturedSavedVolunteer();
            assertEquals(VOLUNTEER_ID, saved.getId());
            assertEquals(FUND_ID, saved.getFundId());
            assertEquals(VolunteerStatus.ACTIVE, saved.getStatus());
            assertEquals("Hanna", saved.getFirstName());
            assertEquals(NEW_EMAIL, saved.getEmail());
            assertEquals(NEW_PHONE, saved.getPhoneNumber());

            assertEquals(new VolunteerResponse(VOLUNTEER_ID, FUND_ID, "Hanna", LAST_NAME, NEW_EMAIL, NEW_PHONE), response);
        }

        @Test
        void throwsDuplicateWhenEmailBelongsToAnotherVolunteer() {
            when(repository.findById(FUND_ID, VOLUNTEER_ID)).thenReturn(Optional.of(existing));
            when(repository.findByEmail(FUND_ID, NEW_EMAIL))
                    .thenReturn(Optional.of(volunteer(OTHER_VOLUNTEER_ID, NEW_EMAIL, NEW_PHONE, VolunteerStatus.ACTIVE)));

            assertThrows(DuplicateVolunteerException.class,
                    () -> service.updateVolunteer(updateCommand("Hanna", NEW_EMAIL, PHONE)));

            verify(repository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void throwsDuplicateWhenEmailBelongsToAnotherInactiveVolunteer() {
            // Неактивний запис буде відновлено при повторній реєстрації за цим email,
            // тому займати його контакти не можна
            when(repository.findById(FUND_ID, VOLUNTEER_ID)).thenReturn(Optional.of(existing));
            when(repository.findByEmail(FUND_ID, NEW_EMAIL))
                    .thenReturn(Optional.of(volunteer(OTHER_VOLUNTEER_ID, NEW_EMAIL, NEW_PHONE, VolunteerStatus.INACTIVE)));

            assertThrows(DuplicateVolunteerException.class,
                    () -> service.updateVolunteer(updateCommand(FIRST_NAME, NEW_EMAIL, PHONE)));

            verify(repository, never()).save(any());
        }

        @Test
        void throwsDuplicateWhenEmailOfAnotherVolunteerDiffersOnlyInCase() {
            when(repository.findById(FUND_ID, VOLUNTEER_ID)).thenReturn(Optional.of(existing));
            when(repository.findByEmail(FUND_ID, NEW_EMAIL))
                    .thenReturn(Optional.of(volunteer(OTHER_VOLUNTEER_ID, NEW_EMAIL, NEW_PHONE, VolunteerStatus.ACTIVE)));

            assertThrows(DuplicateVolunteerException.class,
                    () -> service.updateVolunteer(updateCommand(FIRST_NAME, "Anna.New@Gmail.com", PHONE)));

            verify(repository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void changingOnlyEmailCaseIsNotEmailChange() {
            when(repository.findById(FUND_ID, VOLUNTEER_ID)).thenReturn(Optional.of(existing));
            saveReturnsArgument();

            VolunteerResponse response = service.updateVolunteer(updateCommand(FIRST_NAME, "ANNA@gmail.com", PHONE));

            assertEquals(EMAIL, capturedSavedVolunteer().getEmail());
            assertEquals(EMAIL, response.email());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void throwsDuplicateWhenPhoneNumberBelongsToAnotherVolunteer() {
            when(repository.findById(FUND_ID, VOLUNTEER_ID)).thenReturn(Optional.of(existing));
            when(repository.findByPhoneNumber(FUND_ID, NEW_PHONE))
                    .thenReturn(Optional.of(volunteer(OTHER_VOLUNTEER_ID, NEW_EMAIL, NEW_PHONE, VolunteerStatus.ACTIVE)));

            assertThrows(DuplicateVolunteerException.class,
                    () -> service.updateVolunteer(updateCommand(FIRST_NAME, EMAIL, NEW_PHONE)));

            verify(repository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void rejectedUpdateDoesNotModifyStoredVolunteer() {
            // Репозиторій повертає посилання на збережений об'єкт — часткова зміна до винятку зіпсувала б дані
            when(repository.findById(FUND_ID, VOLUNTEER_ID)).thenReturn(Optional.of(existing));
            when(repository.findByPhoneNumber(FUND_ID, NEW_PHONE))
                    .thenReturn(Optional.of(volunteer(OTHER_VOLUNTEER_ID, NEW_EMAIL, NEW_PHONE, VolunteerStatus.ACTIVE)));

            assertThrows(DuplicateVolunteerException.class,
                    () -> service.updateVolunteer(updateCommand("Hanna", NEW_EMAIL, NEW_PHONE)));

            assertEquals(FIRST_NAME, existing.getFirstName());
            assertEquals(EMAIL, existing.getEmail());
            assertEquals(PHONE, existing.getPhoneNumber());
        }

        @Test
        void keepingOwnEmailAndPhoneIsNotDuplicate() {
            when(repository.findById(FUND_ID, VOLUNTEER_ID)).thenReturn(Optional.of(existing));
            when(repository.findByEmail(FUND_ID, EMAIL)).thenReturn(Optional.of(existing));
            when(repository.findByPhoneNumber(FUND_ID, PHONE)).thenReturn(Optional.of(existing));
            saveReturnsArgument();

            VolunteerResponse response = service.updateVolunteer(updateCommand("Hanna", EMAIL, PHONE));

            assertEquals("Hanna", response.firstName());
            verify(repository).save(existing);
        }

        @Test
        void publishesNoEventsWhenContactsAreUnchanged() {
            when(repository.findById(FUND_ID, VOLUNTEER_ID)).thenReturn(Optional.of(existing));
            saveReturnsArgument();

            service.updateVolunteer(updateCommand("Hanna", EMAIL, PHONE));

            verifyNoInteractions(eventPublisher);
        }

        @Test
        void publishesOnlyEmailEventAfterSavingWhenEmailChanged() {
            when(repository.findById(FUND_ID, VOLUNTEER_ID)).thenReturn(Optional.of(existing));
            saveReturnsArgument();

            service.updateVolunteer(updateCommand(FIRST_NAME, NEW_EMAIL, PHONE));

            InOrder inOrder = inOrder(repository, eventPublisher);
            inOrder.verify(repository).save(any(Volunteer.class));
            inOrder.verify(eventPublisher).publishEvent(new VolunteerUpdatedEmailEvent(FIRST_NAME, NEW_EMAIL));
            verify(eventPublisher, never()).publishEvent(any(VolunteerUpdatedPhoneNumberEvent.class));
        }

        @Test
        void publishesOnlyPhoneEventAfterSavingWhenPhoneChanged() {
            when(repository.findById(FUND_ID, VOLUNTEER_ID)).thenReturn(Optional.of(existing));
            saveReturnsArgument();

            service.updateVolunteer(updateCommand(FIRST_NAME, EMAIL, NEW_PHONE));

            InOrder inOrder = inOrder(repository, eventPublisher);
            inOrder.verify(repository).save(any(Volunteer.class));
            inOrder.verify(eventPublisher).publishEvent(new VolunteerUpdatedPhoneNumberEvent(FIRST_NAME, NEW_PHONE, EMAIL));
            verify(eventPublisher, never()).publishEvent(any(VolunteerUpdatedEmailEvent.class));
        }

        @Test
        void publishesBothEventsWhenEmailAndPhoneChanged() {
            when(repository.findById(FUND_ID, VOLUNTEER_ID)).thenReturn(Optional.of(existing));
            saveReturnsArgument();

            service.updateVolunteer(updateCommand(FIRST_NAME, NEW_EMAIL, NEW_PHONE));

            verify(eventPublisher).publishEvent(new VolunteerUpdatedEmailEvent(FIRST_NAME, NEW_EMAIL));
            verify(eventPublisher).publishEvent(new VolunteerUpdatedPhoneNumberEvent(FIRST_NAME, NEW_PHONE, NEW_EMAIL));
            verify(eventPublisher, times(2)).publishEvent(any(Object.class));
        }
    }

    @Nested
    class RemoveVolunteer {

        // lenient: сервіс може перевіряти існування через existsById або одразу через findById —
        // тест перевіряє поведінку, а не спосіб пошуку

        @Test
        void throwsNotFoundWhenVolunteerIsAbsent() {
            lenient().when(repository.existsById(FUND_ID, VOLUNTEER_ID)).thenReturn(false);
            lenient().when(repository.findById(FUND_ID, VOLUNTEER_ID)).thenReturn(Optional.empty());

            assertThrows(VolunteerNotFoundException.class,
                    () -> service.removeVolunteer(FUND_ID, VOLUNTEER_ID));

            verify(repository, never()).deleteById(any(), any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        void deletesVolunteerAndThenPublishesRemovedEvent() {
            // Мок поводиться як справжній репозиторій: після м'якого видалення
            // волонтер більше не знаходиться ні через findById, ні через existsById
            Volunteer stored = volunteer(VOLUNTEER_ID, VolunteerStatus.ACTIVE);
            lenient().when(repository.existsById(FUND_ID, VOLUNTEER_ID))
                    .thenAnswer(inv -> stored.isActive());
            lenient().when(repository.findById(FUND_ID, VOLUNTEER_ID))
                    .thenAnswer(inv -> stored.isActive() ? Optional.of(stored) : Optional.empty());
            doAnswer(inv -> {
                stored.setStatus(VolunteerStatus.INACTIVE);
                return null;
            }).when(repository).deleteById(FUND_ID, VOLUNTEER_ID);

            service.removeVolunteer(FUND_ID, VOLUNTEER_ID);

            // Доменна подія фіксує факт, що вже відбувся, — публікується після зміни стану
            InOrder inOrder = inOrder(repository, eventPublisher);
            inOrder.verify(repository).deleteById(FUND_ID, VOLUNTEER_ID);
            inOrder.verify(eventPublisher).publishEvent(new VolunteerRemovedEvent(FIRST_NAME, EMAIL));
        }
    }
}
