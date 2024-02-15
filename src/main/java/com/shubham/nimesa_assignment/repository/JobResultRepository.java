package com.shubham.nimesa_assignment.repository;

import com.shubham.nimesa_assignment.model.JobResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobResultRepository extends JpaRepository<JobResult, Long> {
    List<JobResult> findAllByServiceType(String service);
}
