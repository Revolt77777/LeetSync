package com.leetsync.api.service;

import com.leetsync.api.repository.ApiRepository;
import com.leetsync.shared.model.AcSubmission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;

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
        List<AcSubmission> stub = List.of(new AcSubmission());
        Mockito.when(repo.findAll()).thenReturn(stub);

        List<AcSubmission> result = service.getAllSubmissions();

        assertThat(result).isEqualTo(stub);
        Mockito.verify(repo).findAll();
    }

    @Test
    void getByProblemId_passesThrough() {
        long pid = 42L;
        Mockito.when(repo.findByProblemId(eq(pid)))
                .thenReturn(List.of(new AcSubmission()));

        List<AcSubmission> result = service.getByProblemId(pid);

        assertThat(result).hasSize(1);
        Mockito.verify(repo).findByProblemId(pid);
    }
}
