package com.leetsync.api.controller;

import com.leetsync.api.service.ApiService;
import com.leetsync.shared.model.AcSubmission;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST endpoints for user-specific data. */
@RestController
public class ApiController {

    private final ApiService service;

    public ApiController(ApiService service) {
        this.service = service;
    }

    @GetMapping("/{username}/acsubmissions")
    public List<AcSubmission> getSubmissionsByUsername(@PathVariable String username) {
        return service.getSubmissionsByUsername(username);
    }
}
