package com.example.help_bridge.users.fundrepresentative.service;

import com.example.help_bridge.fundraising.fund.entity.Fund;
import com.example.help_bridge.fundraising.fund.exception.FundNotFoundException;
import com.example.help_bridge.fundraising.fund.repository.FundRepository;
import com.example.help_bridge.users.fundrepresentative.dto.request.FundRepresentativeRequest;
import com.example.help_bridge.users.fundrepresentative.dto.request.FundRepresentativeUpdateRequest;
import com.example.help_bridge.users.fundrepresentative.dto.response.FundRepresentativeResponse;
import com.example.help_bridge.users.fundrepresentative.entity.FundRepresentative;
import com.example.help_bridge.users.fundrepresentative.exception.DuplicateFundRepresentativeException;
import com.example.help_bridge.users.fundrepresentative.exception.FundRepresentativeNotFoundException;
import com.example.help_bridge.users.fundrepresentative.repository.FundRepresentativeRepository;
import com.example.help_bridge.users.fundrepresentative.service.impl.FundRepresentativeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundRepresentativeServiceImplTest {

    @Mock
    private FundRepresentativeRepository repository;

    @Mock
    private FundRepository fundRepository;

    private FundRepresentativeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FundRepresentativeServiceImpl(repository, fundRepository);
    }

    private Fund fund() {
        Fund fund = new Fund();
        fund.setId(1L);
        return fund;
    }

    private FundRepresentative representative(Long id) {
        FundRepresentative rep = new FundRepresentative(
                "Daria", "Chorna", "chorna@mail.com", "+380501111111", "hash", fund());
        ReflectionTestUtils.setField(rep, "id", id);
        return rep;
    }

    @Test
    void create_savesRepresentative_andDoesNotStorePlainPassword() {
        FundRepresentativeRequest request = new FundRepresentativeRequest(
                "Daria", "Chorna", "chorna@mail.com", "+380501111111", "Secret123!", 1L);
        when(repository.existsByEmail("chorna@mail.com")).thenReturn(false);
        when(fundRepository.findById(1L)).thenReturn(Optional.of(fund()));
        when(repository.save(any(FundRepresentative.class))).thenAnswer(invocation -> {
            FundRepresentative saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 5L);
            return saved;
        });

        FundRepresentativeResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.fundId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("chorna@mail.com");

        ArgumentCaptor<FundRepresentative> captor = ArgumentCaptor.forClass(FundRepresentative.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isNotEqualTo("Secret123!");
    }

    @Test
    void create_throwsDuplicate_whenEmailExists() {
        FundRepresentativeRequest request = new FundRepresentativeRequest(
                "Daria", "Chorna", "chorna@mail.com", "+380501111111", "Secret123!", 1L);
        when(repository.existsByEmail("chorna@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(DuplicateFundRepresentativeException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void create_throwsFundNotFound_whenFundMissing() {
        FundRepresentativeRequest request = new FundRepresentativeRequest(
                "Daria", "Chorna", "chorna@mail.com", "+380501111111", "Secret123!", 99L);
        when(repository.existsByEmail("chorna@mail.com")).thenReturn(false);
        when(fundRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(FundNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void getById_returnsRepresentative_whenExists() {
        when(repository.findByIdWithFund(10L)).thenReturn(Optional.of(representative(10L)));

        FundRepresentativeResponse response = service.getById(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.fundId()).isEqualTo(1L);
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        when(repository.findByIdWithFund(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(FundRepresentativeNotFoundException.class);
    }

    @Test
    void getAll_returnsMappedRepresentatives() {
        when(repository.findAllWithFund()).thenReturn(List.of(representative(10L), representative(11L)));

        List<FundRepresentativeResponse> result = service.getAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void getByFundId_returnsRepresentatives_whenFundExists() {
        when(fundRepository.existsById(1L)).thenReturn(true);
        when(repository.findByFundIdWithFund(1L)).thenReturn(List.of(representative(10L)));

        List<FundRepresentativeResponse> result = service.getByFundId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).fundId()).isEqualTo(1L);
    }

    @Test
    void getByFundId_throwsFundNotFound_whenFundMissing() {
        when(fundRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.getByFundId(99L))
                .isInstanceOf(FundNotFoundException.class);

        verify(repository, never()).findByFundIdWithFund(any());
    }

    @Test
    void update_changesFields() {
        FundRepresentative rep = representative(10L);
        when(repository.findByIdWithFund(10L)).thenReturn(Optional.of(rep));
        when(repository.existsByEmailAndIdNot("new@mail.com", 10L)).thenReturn(false);

        FundRepresentativeResponse response = service.update(10L, new FundRepresentativeUpdateRequest(
                "Olena", "Koval", "new@mail.com", "+380502222222"));

        assertThat(response.firstName()).isEqualTo("Olena");
        assertThat(response.lastName()).isEqualTo("Koval");
        assertThat(response.email()).isEqualTo("new@mail.com");
        assertThat(response.phone()).isEqualTo("+380502222222");
    }

    @Test
    void update_throwsNotFound_whenMissing() {
        when(repository.findByIdWithFund(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new FundRepresentativeUpdateRequest(
                "Olena", "Koval", "new@mail.com", "+380502222222")))
                .isInstanceOf(FundRepresentativeNotFoundException.class);
    }

    @Test
    void update_throwsDuplicate_whenEmailTakenByAnother() {
        when(repository.findByIdWithFund(10L)).thenReturn(Optional.of(representative(10L)));
        when(repository.existsByEmailAndIdNot("taken@mail.com", 10L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(10L, new FundRepresentativeUpdateRequest(
                "Olena", "Koval", "taken@mail.com", "+380502222222")))
                .isInstanceOf(DuplicateFundRepresentativeException.class);
    }

    @Test
    void delete_deletesById_whenExists() {
        when(repository.existsById(10L)).thenReturn(true);

        service.delete(10L);

        verify(repository).deleteById(10L);
    }

    @Test
    void delete_throwsNotFound_whenMissing() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(FundRepresentativeNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}