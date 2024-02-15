package com.shubham.nimesa_assignment.repository;

import com.shubham.nimesa_assignment.model.S3Files;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface S3FilesRepository extends JpaRepository<S3Files, Long> {
    List<S3Files> findByBucketName(String bucketName);
}
