package com.leetsync.api.controller;

import com.leetsync.api.service.ApiService;
import com.leetsync.shared.model.AcSubmission;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST endpoints for accepted-submission data. */
@RestController
@RequestMapping("/AcSubmissions")
public class ApiController {

    private final ApiService service;

    public ApiController(ApiService service) {
        this.service = service;
    }

    @GetMapping
    public List<AcSubmission> all() {
        return service.getAllSubmissions();
    }

    @GetMapping("/{problemId}")
    public List<AcSubmission> byProblem(@PathVariable("problemId") long problemId) {
        return service.getByProblemId(problemId);
    }
}
