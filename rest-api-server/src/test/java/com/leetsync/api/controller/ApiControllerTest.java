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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiController.class)
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiService apiService;          // mocked, no DB needed

    @Test
    void allSubmissions_returnsList() throws Exception {
        List<AcSubmission> stub =
                List.of(new AcSubmission(1L, "Two Sum", "two-sum", 1_620_000_000L));

        Mockito.when(apiService.getAllSubmissions()).thenReturn(stub);

        mockMvc.perform(get("/submissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].title").value("Two Sum"));
    }

    @Test
    void byProblemId_returnsMatchingList() throws Exception {
        Mockito.when(apiService.getByProblemId(anyLong()))
                .thenReturn(List.of(new AcSubmission(2L, "Add Two Numbers",
                        "add-two-numbers", 1_620_360_000L)));

        mockMvc.perform(get("/submissions/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titleSlug").value("add-two-numbers"));
    }
}
