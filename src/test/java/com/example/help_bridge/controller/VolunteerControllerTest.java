package com.example.help_bridge.controller;

import com.example.help_bridge.dto.VolunteerRequest;
import com.example.help_bridge.dto.VolunteerResponse;
import com.example.help_bridge.service.VolunteerService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VolunteerController.class)
class VolunteerControllerTest {

    private static final String BASE_URL = "/api/funds/{fundId}/volunteers";

    private static final UUID FUND_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID VOLUNTEER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final String VALID_BODY = """
            {
              "firstName": "Anna",
              "lastName": "Samana",
              "email": "anna@gmail.com",
              "phoneNumber": "+380501234567"
            }
            """;

    private static final VolunteerRequest VALID_REQUEST =
            new VolunteerRequest("Anna", "Samana", "anna@gmail.com", "+380501234567");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VolunteerService service;

    private static VolunteerResponse volunteer(UUID id) {
        return new VolunteerResponse(id, FUND_ID, "Anna", "Samana", "anna@gmail.com", "+380501234567");
    }

    @Nested
    class GetAllFundVolunteers {

        @Test
        void returns200WithVolunteerList() throws Exception {
            UUID secondId = UUID.randomUUID();
            when(service.getFundVolunteers(FUND_ID))
                    .thenReturn(List.of(volunteer(VOLUNTEER_ID), volunteer(secondId)));

            mockMvc.perform(get(BASE_URL, FUND_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(VOLUNTEER_ID.toString()))
                    .andExpect(jsonPath("$[0].fundId").value(FUND_ID.toString()))
                    .andExpect(jsonPath("$[1].id").value(secondId.toString()));
        }

        @Test
        void returns200WithEmptyListWhenFundHasNoVolunteers() throws Exception {
            when(service.getFundVolunteers(FUND_ID)).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL, FUND_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    class GetFundVolunteerById {

        @Test
        void returns200WithVolunteer() throws Exception {
            when(service.getFundVolunteer(FUND_ID, VOLUNTEER_ID)).thenReturn(volunteer(VOLUNTEER_ID));

            mockMvc.perform(get(BASE_URL + "/{volunteerId}", FUND_ID, VOLUNTEER_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(VOLUNTEER_ID.toString()))
                    .andExpect(jsonPath("$.fundId").value(FUND_ID.toString()))
                    .andExpect(jsonPath("$.firstName").value("Anna"))
                    .andExpect(jsonPath("$.lastName").value("Samana"))
                    .andExpect(jsonPath("$.email").value("anna@gmail.com"))
                    .andExpect(jsonPath("$.phoneNumber").value("+380501234567"));
        }

        @Test
        void returns400WhenVolunteerIdIsNotUuid() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{volunteerId}", FUND_ID, "not-a-uuid"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(service);
        }
    }

    @Nested
    class AddVolunteer {

        @Test
        void returns201WithLocationHeaderAndBody() throws Exception {
            UUID createdId = UUID.randomUUID();
            when(service.addVolunteer(FUND_ID, VALID_REQUEST)).thenReturn(volunteer(createdId));

            mockMvc.perform(post(BASE_URL, FUND_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location",
                            "http://localhost/api/funds/" + FUND_ID + "/volunteers/" + createdId))
                    .andExpect(jsonPath("$.id").value(createdId.toString()))
                    .andExpect(jsonPath("$.email").value("anna@gmail.com"));

            verify(service).addVolunteer(FUND_ID, VALID_REQUEST);
        }

        @Test
        void returns400WithFieldErrorsWhenAllFieldsBlank() throws Exception {
            String body = """
                    {
                      "firstName": "",
                      "lastName": " ",
                      "email": "",
                      "phoneNumber": ""
                    }
                    """;

            mockMvc.perform(post(BASE_URL, FUND_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Validation Error"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value("Validation failed for request body"))
                    .andExpect(jsonPath("$.errors.firstName").exists())
                    .andExpect(jsonPath("$.errors.lastName").exists())
                    .andExpect(jsonPath("$.errors.email").exists())
                    .andExpect(jsonPath("$.errors.phoneNumber").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verifyNoInteractions(service);
        }

        @Test
        void returns400WhenFieldsAreMissing() throws Exception {
            mockMvc.perform(post(BASE_URL, FUND_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.firstName").value("First name cannot be blank"))
                    .andExpect(jsonPath("$.errors.lastName").value("Last name cannot be blank"))
                    .andExpect(jsonPath("$.errors.email").value("Email cannot be blank"))
                    .andExpect(jsonPath("$.errors.phoneNumber").value("Phone number cannot be blank"));

            verifyNoInteractions(service);
        }

        @Test
        void returns400WhenEmailIsInvalid() throws Exception {
            String body = """
                    {
                      "firstName": "Anna",
                      "lastName": "Samana",
                      "email": "not-an-email",
                      "phoneNumber": "+380501234567"
                    }
                    """;

            mockMvc.perform(post(BASE_URL, FUND_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").value("Enter correct email address"))
                    .andExpect(jsonPath("$.errors.firstName").doesNotExist());

            verifyNoInteractions(service);
        }

        @Test
        void returns400WhenPhoneNumberDoesNotMatchPattern() throws Exception {
            String body = """
                    {
                      "firstName": "Anna",
                      "lastName": "Samana",
                      "email": "anna@gmail.com",
                      "phoneNumber": "0501234567"
                    }
                    """;

            mockMvc.perform(post(BASE_URL, FUND_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.phoneNumber").value("Phone number must match +380XXXXXXXXX"));

            verifyNoInteractions(service);
        }

        @Test
        void returns400WhenFirstNameIsTooLong() throws Exception {
            String body = """
                    {
                      "firstName": "%s",
                      "lastName": "Samana",
                      "email": "anna@gmail.com",
                      "phoneNumber": "+380501234567"
                    }
                    """.formatted("a".repeat(51));

            mockMvc.perform(post(BASE_URL, FUND_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.firstName").value("First name must be at most 50 characters"));

            verifyNoInteractions(service);
        }

        @Test
        void returns400WhenJsonIsMalformed() throws Exception {
            mockMvc.perform(post(BASE_URL, FUND_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ \"firstName\": "))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(service);
        }

        @Test
        void returns400WhenBodyHasUnknownField() throws Exception {
            String body = """
                    {
                      "firstName": "Anna",
                      "lastName": "Samana",
                      "email": "anna@gmail.com",
                      "phoneNumber": "+380501234567",
                      "role": "ADMIN"
                    }
                    """;

            mockMvc.perform(post(BASE_URL, FUND_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(service);
        }

        @Test
        void returns415WhenContentTypeIsNotJson() throws Exception {
            mockMvc.perform(post(BASE_URL, FUND_ID)
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(VALID_BODY))
                    .andExpect(status().isUnsupportedMediaType());

            verifyNoInteractions(service);
        }
    }

    @Nested
    class UpdateVolunteer {

        @Test
        void returns200WithUpdatedVolunteer() throws Exception {
            when(service.updateVolunteer(FUND_ID, VOLUNTEER_ID, VALID_REQUEST)).thenReturn(volunteer(VOLUNTEER_ID));

            mockMvc.perform(put(BASE_URL + "/{volunteerId}", FUND_ID, VOLUNTEER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(VOLUNTEER_ID.toString()))
                    .andExpect(jsonPath("$.firstName").value("Anna"));

            verify(service).updateVolunteer(FUND_ID, VOLUNTEER_ID, VALID_REQUEST);
        }

        @Test
        void returns400WhenBodyIsInvalid() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{volunteerId}", FUND_ID, VOLUNTEER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation Error"));

            verifyNoInteractions(service);
        }
    }

    @Nested
    class RemoveVolunteer {

        @Test
        void returns204WithEmptyBody() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{volunteerId}", FUND_ID, VOLUNTEER_ID))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(service).removeVolunteer(FUND_ID, VOLUNTEER_ID);
        }
    }

    @Nested
    class ErrorHandling {

        @Test
        void returns500ProblemDetailWhenServiceThrowsUnexpectedException() throws Exception {
            when(service.getFundVolunteers(any())).thenThrow(new IllegalStateException("db is down"));

            mockMvc.perform(get(BASE_URL, FUND_ID))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Internal Server Error"))
                    .andExpect(jsonPath("$.detail").value("Internal Server Error"));
        }

        @Test
        void returns405WhenMethodIsNotSupported() throws Exception {
            mockMvc.perform(put(BASE_URL, FUND_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isMethodNotAllowed());

            verify(service, never()).updateVolunteer(eq(FUND_ID), any(), any());
        }
    }
}
