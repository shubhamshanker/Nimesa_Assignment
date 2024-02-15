package com.shubham.nimesa_assignment.controller;

import com.shubham.nimesa_assignment.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class S3Controller {
    @Autowired
    private S3Service s3Service;

    @PostMapping("/s3-objects")
    public ResponseEntity<Long> getS3BucketObjects(@RequestParam String bucketName) {
        try {
            Long jobId = s3Service.discoverS3BucketObjects(bucketName).get();
            return new ResponseEntity<>(jobId, HttpStatus.OK);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/s3-objects/count")
    public ResponseEntity<Integer> getS3BucketObjectsCount(@RequestParam String bucketName) {
        try {
            Integer count = s3Service.getS3BucketObjectsCount(bucketName);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/s3-objects/like")
    public ResponseEntity<List<String>> GetS3BucketObjectlike(@RequestParam String bucketName,
                                                              @RequestParam String pattern) {
        try {
            List<String> similarFiles = s3Service.getS3BucketObjectsLike(bucketName, pattern);
            return new ResponseEntity<>(similarFiles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
