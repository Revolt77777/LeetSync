package com.leetsync.api.controller;

import com.leetsync.api.service.ApiService;
import com.leetsync.shared.model.AcSubmission;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiController.class)
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiService apiService;          // mocked, no DB needed

    @Test
    void getSubmissionsByUsername_returnsList() throws Exception {
        String username = "testuser";
        List<AcSubmission> stub =
                List.of(new AcSubmission(username, "Two Sum", "two-sum", 1_620_000_000L));

        Mockito.when(apiService.getSubmissionsByUsername(username)).thenReturn(stub);

        mockMvc.perform(get("/" + username + "/acsubmissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].title").value("Two Sum"))
                .andExpect(jsonPath("$[0].username").value(username));
    }
}
