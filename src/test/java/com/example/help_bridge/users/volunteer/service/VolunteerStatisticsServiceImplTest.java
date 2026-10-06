package com.example.help_bridge.users.volunteer.service;

import com.example.help_bridge.fundraising.fundraiser.entity.AssignmentStatus;
import com.example.help_bridge.fundraising.fundraiser.repository.FundraiserAssignmentRepository;
import com.example.help_bridge.users.volunteer.dto.request.VolunteerStatisticsRequest;
import com.example.help_bridge.users.volunteer.dto.response.VolunteerStatisticsResponse;
import com.example.help_bridge.users.volunteer.exception.VolunteerNotFoundException;
import com.example.help_bridge.users.volunteer.repository.VolunteerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VolunteerStatisticsServiceImplTest {

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private FundraiserAssignmentRepository assignmentRepository;

    @InjectMocks
    private VolunteerStatisticsServiceImpl service;

    @Test
    void returnsCountForPeriodWithInclusiveBounds() {
        when(volunteerRepository.existsById(3L)).thenReturn(true);
        when(assignmentRepository.countDistinctFundraisersFinishedBetween(
                3L, AssignmentStatus.COMPLETED,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 9, 2, 0, 0)))
                .thenReturn(7L);

        VolunteerStatisticsResponse result = service.getClosedFundraisersStatistics(3L,
                new VolunteerStatisticsRequest(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 1)));

        assertThat(result.volunteerId()).isEqualTo(3L);
        assertThat(result.closedFundraisersCount()).isEqualTo(7L);
    }

    @Test
    void throwsNotFound_whenVolunteerMissing() {
        when(volunteerRepository.existsById(3L)).thenReturn(false);

        assertThatThrownBy(() -> service.getClosedFundraisersStatistics(3L,
                new VolunteerStatisticsRequest(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 1))))
                .isInstanceOf(VolunteerNotFoundException.class);

        verifyNoInteractions(assignmentRepository);
    }
}
