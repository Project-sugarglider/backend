package com.projectsugarglider.datainitialize.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projectsugarglider.datainitialize.orchestrator.BasicDataOrchestrator;
import com.projectsugarglider.datainitialize.orchestrator.BasicDataRunResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/BasicData")
@RequiredArgsConstructor
public class BasicDataOrchestratorController {

    private final BasicDataOrchestrator orchestrator;

    @PostMapping("/UpdateAll")
    public ResponseEntity<BasicDataRunResult> updateAll(
            @RequestParam(name = "stopOnFailure", defaultValue = "true") boolean stopOnFailure
    ) {
        String requestId = UUID.randomUUID().toString().replace("-", "");
        BasicDataRunResult result = orchestrator.runAll(requestId, stopOnFailure);
        return ResponseEntity.ok(result);
    }
}