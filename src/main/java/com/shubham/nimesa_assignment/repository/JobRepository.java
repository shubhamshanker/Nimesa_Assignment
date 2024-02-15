package com.shubham.nimesa_assignment.repository;

import com.shubham.nimesa_assignment.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {

}
