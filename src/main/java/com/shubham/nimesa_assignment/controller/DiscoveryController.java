package com.shubham.nimesa_assignment.controller;

import com.shubham.nimesa_assignment.JobStatus;
import com.shubham.nimesa_assignment.service.DiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DiscoveryController {

    @Autowired
    private DiscoveryService discoveryService;

    @PostMapping("/discover-services")
    public ResponseEntity<Long> discoverServices(@RequestBody List<String> services) {
        Long jobId = discoveryService.startDiscovery(services);
        return new ResponseEntity<>(jobId, HttpStatus.ACCEPTED);
    }

    @GetMapping("/job-status/")
    public ResponseEntity<JobStatus> getJobStatus(@RequestParam Long jobId)
    {
       JobStatus status = discoveryService.getJobStatus(jobId);
       return new ResponseEntity<>(status, HttpStatus.ACCEPTED);
    }

    @GetMapping("/discover-results")
    public ResponseEntity<List<String>> getResults(@RequestParam String service) {
        List<String> jobId = discoveryService.getResults(service);
        return new ResponseEntity<>(jobId, HttpStatus.ACCEPTED);
    }
}