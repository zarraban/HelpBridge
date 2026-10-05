package com.example.help_bridge.users.fundrepresentative.controller;

import com.example.help_bridge.fundraising.fund.exception.FundNotFoundException;
import com.example.help_bridge.users.fundrepresentative.dto.response.FundRepresentativeResponse;
import com.example.help_bridge.users.fundrepresentative.exception.DuplicateFundRepresentativeException;
import com.example.help_bridge.users.fundrepresentative.exception.FundRepresentativeNotFoundException;
import com.example.help_bridge.users.fundrepresentative.service.FundRepresentativeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FundRepresentativeController.class)
class FundRepresentativeControllerTest {

    private static final String URL = "/api/fund-representatives";

    private static final String VALID_CREATE = """
            {"firstName":"Daria","lastName":"Chorna","email":"chorna@mail.com",
             "phone":"+380501111111","password":"Secret123!","fundId":1}
            """;

    private static final String VALID_UPDATE = """
            {"firstName":"Olena","lastName":"Koval","email":"olena@mail.com","phone":"+380502222222"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FundRepresentativeService service;

    private FundRepresentativeResponse sample() {
        return new FundRepresentativeResponse(1L, "Daria", "Chorna", "chorna@mail.com", "+380501111111", 1L);
    }

    @Test
    void create_valid_returns201WithoutPassword() throws Exception {
        when(service.create(any())).thenReturn(sample());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_CREATE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("chorna@mail.com"))
                .andExpect(jsonPath("$.fundId").value(1))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void create_invalidBody_returns400() throws Exception {
        String invalid = """
                {"firstName":"","lastName":"Chorna","email":"not-an-email",
                 "phone":"+380501111111","password":"123","fundId":null}
                """;

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_duplicateEmail_returns409() throws Exception {
        when(service.create(any())).thenThrow(new DuplicateFundRepresentativeException("chorna@mail.com"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_CREATE))
                .andExpect(status().isConflict());
    }

    @Test
    void create_fundNotFound_returns404() throws Exception {
        when(service.create(any())).thenThrow(new FundNotFoundException(1L));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_CREATE))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_returns200() throws Exception {
        when(service.getById(1L)).thenReturn(sample());

        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(service.getById(99L)).thenThrow(new FundRepresentativeNotFoundException(99L));

        mockMvc.perform(get(URL + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_returns200WithList() throws Exception {
        when(service.getAll()).thenReturn(List.of(sample()));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByFund_returns200WithList() throws Exception {
        when(service.getByFundId(1L)).thenReturn(List.of(sample()));

        mockMvc.perform(get(URL + "/fund/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fundId").value(1));
    }

    @Test
    void getByFund_fundNotFound_returns404() throws Exception {
        when(service.getByFundId(99L)).thenThrow(new FundNotFoundException(99L));

        mockMvc.perform(get(URL + "/fund/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_valid_returns200() throws Exception {
        when(service.update(any(), any())).thenReturn(
                new FundRepresentativeResponse(1L, "Olena", "Koval", "olena@mail.com", "+380502222222", 1L));

        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Olena"));
    }

    @Test
    void update_invalidBody_returns400() throws Exception {
        String invalid = """
                {"firstName":"Olena","lastName":"Koval","email":"bad","phone":""}
                """;

        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete(URL + "/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new FundRepresentativeNotFoundException(99L)).when(service).delete(99L);

        mockMvc.perform(delete(URL + "/99"))
                .andExpect(status().isNotFound());
    }
}