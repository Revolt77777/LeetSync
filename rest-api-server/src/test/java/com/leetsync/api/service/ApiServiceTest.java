package com.leetsync.api.service;

import com.leetsync.api.repository.ApiRepository;
import com.leetsync.shared.model.AcSubmission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiServiceTest {

    private ApiRepository repo;
    private ApiService service;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(ApiRepository.class);
        service = new ApiService(repo);
    }

    @Test
    void getAllSubmissions_delegatesToRepo() {
        List<AcSubmission> stub = List.of(new AcSubmission("testuser", "Two Sum", "two-sum", 1720000000L));
        Mockito.when(repo.findAll()).thenReturn(stub);

        List<AcSubmission> result = service.getAllSubmissions();

        assertThat(result).isEqualTo(stub);
        Mockito.verify(repo).findAll();
    }

    @Test
    void getSubmissionsByUsername_delegatesToRepo() {
        String username = "testuser";
        List<AcSubmission> stub = List.of(new AcSubmission(username, "Two Sum", "two-sum", 1720000000L));
        Mockito.when(repo.findByUsername(username)).thenReturn(stub);

        List<AcSubmission> result = service.getSubmissionsByUsername(username);

        assertThat(result).isEqualTo(stub);
        Mockito.verify(repo).findByUsername(username);
    }
}
