package com.leetsync.api.service;

import com.leetsync.api.repository.ApiRepository;
import com.leetsync.shared.model.AcSubmission;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiService {

    private final ApiRepository repository;

    public ApiService(ApiRepository repository) {
        this.repository = repository;
    }

    public List<AcSubmission> getAllSubmissions() {
        return repository.findAll();
    }

    public List<AcSubmission> getByProblemId(long problemId) {
        return repository.findByProblemId(problemId);
    }
}
