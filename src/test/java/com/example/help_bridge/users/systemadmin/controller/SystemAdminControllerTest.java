package com.example.help_bridge.users.systemadmin.controller;

import com.example.help_bridge.users.systemadmin.dto.response.SystemAdminResponse;
import com.example.help_bridge.users.systemadmin.exception.DuplicateSystemAdminException;
import com.example.help_bridge.users.systemadmin.exception.SystemAdminNotFoundException;
import com.example.help_bridge.users.systemadmin.service.SystemAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemAdminController.class)
class SystemAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SystemAdminService service;

    private static final String VALID_CREATE = """
            {"email":"admin@help.ua","password":"secret123","firstName":"Daria","lastName":"Chorna"}
            """;

    @Test
    void create_valid_returns201WithLocationAndNoPassword() throws Exception {
        when(service.createSystemAdmin(any()))
                .thenReturn(new SystemAdminResponse(1L, "admin@help.ua", "Daria", "Chorna"));

        mockMvc.perform(post("/system-admins").contentType(MediaType.APPLICATION_JSON).content(VALID_CREATE))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/system-admins/1"))
                .andExpect(jsonPath("$.email").value("admin@help.ua"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void create_invalidEmailAndShortPassword_returns400() throws Exception {
        String invalid = """
                {"email":"not-an-email","password":"123","firstName":"Daria","lastName":"Chorna"}
                """;

        mockMvc.perform(post("/system-admins").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_duplicate_returns409() throws Exception {
        when(service.createSystemAdmin(any())).thenThrow(new DuplicateSystemAdminException("admin@help.ua"));

        mockMvc.perform(post("/system-admins").contentType(MediaType.APPLICATION_JSON).content(VALID_CREATE))
                .andExpect(status().isConflict());
    }

    @Test
    void getAll_returns200WithList() throws Exception {
        when(service.getAllSystemAdmins())
                .thenReturn(List.of(new SystemAdminResponse(1L, "admin@help.ua", "Daria", "Chorna")));

        mockMvc.perform(get("/system-admins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(service.getSystemAdminById(99L)).thenThrow(new SystemAdminNotFoundException(99L));

        mockMvc.perform(get("/system-admins/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_valid_returns200() throws Exception {
        when(service.updateSystemAdmin(any(), any()))
                .thenReturn(new SystemAdminResponse(1L, "admin@help.ua", "Olena", "Shevchenko"));

        mockMvc.perform(put("/system-admins/1").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Olena","lastName":"Shevchenko"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Olena"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/system-admins/1"))
                .andExpect(status().isNoContent());
        verify(service).deleteSystemAdminById(1L);
    }
}