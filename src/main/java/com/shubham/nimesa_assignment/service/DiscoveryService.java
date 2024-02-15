package com.shubham.nimesa_assignment.service;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.shubham.nimesa_assignment.JobStatus;
import com.shubham.nimesa_assignment.model.Job;
import com.shubham.nimesa_assignment.model.JobResult;
import com.shubham.nimesa_assignment.repository.JobRepository;
import com.shubham.nimesa_assignment.repository.JobResultRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class DiscoveryService {
    private final JobRepository jobRepository;
    private final JobResultRepository jobResultRepository;

    private final AmazonS3 amazonS3;

    private final AmazonEC2 amazonEC2;

    public DiscoveryService(JobRepository jobRepository, JobResultRepository jobResultRepository, AmazonS3 amazonS3, AmazonEC2 amazonEC2) {
        this.jobRepository = jobRepository;
        this.jobResultRepository = jobResultRepository;
        this.amazonS3 = amazonS3;
        this.amazonEC2 = amazonEC2;
    }

    @Async
    public CompletableFuture<List<String>> discoverEC2Instances() {

        List<String> instanceIds = new ArrayList<>();
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        DescribeInstancesResult response = amazonEC2.describeInstances(request);
        for (Reservation reservation : response.getReservations()) {
            for (Instance instance : reservation.getInstances()) {
                instanceIds.add(instance.getInstanceId());
            }
        }
        return CompletableFuture.completedFuture(instanceIds);
    }

    @Async
    public CompletableFuture<List<String>> discoverS3Buckets() {
        List<Bucket> buckets = amazonS3.listBuckets();
        List<String> bucketNames = buckets.stream().map(Bucket::getName).collect(Collectors.toList());
        return CompletableFuture.completedFuture(bucketNames);

    }

    public Long startDiscovery(List<String> services) {
        Job job = new Job();
        job.setJobStatus(JobStatus.IN_PROGRESS);
        job = jobRepository.save(job);
        Long jobId = job.getJobId();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        if (services.contains("EC2")) {
            Job finalJob = job;
            discoverEC2Instances().thenAcceptAsync(instances -> addResults(instances, finalJob, "EC2"));
        }
        if (services.contains("S3")) {
            Job finalJob = job;
            discoverS3Buckets().thenAcceptAsync(buckets -> addResults(buckets, finalJob, "S3"));
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.thenRunAsync(() -> {
            Job jobToUpdate = jobRepository.findById(jobId).orElseThrow(() -> new IllegalStateException("Job not found"));
            jobToUpdate.setJobStatus(JobStatus.SUCCESS);
            jobRepository.save(jobToUpdate);
        }).exceptionally(ex -> {
            Job jobToUpdate = jobRepository.findById(jobId).orElseThrow(() -> new IllegalStateException("Job not found"));
            jobToUpdate.setJobStatus(JobStatus.FAILED);
            jobRepository.save(jobToUpdate);
            return null;
        });

        return jobId;

    }

    @Async
    protected void addResults(List<String> instances, Job job, String serviceType) {
        List<String> existantInstances = getResults(serviceType);

        List<JobResult> newJobResults = instances.stream()
                .filter(instance -> !existantInstances.contains(instance))
                .map(instance -> JobResult.builder().resultData(instance).serviceType(serviceType).job(job).build())
                .collect(Collectors.toList());

        if (!newJobResults.isEmpty()) {
            jobResultRepository.saveAll(newJobResults);
        }
    }

    public JobStatus getJobStatus(Long jobId) {
        return jobRepository.findById(jobId)
                .map(Job::getJobStatus)
                .orElseThrow(() -> new EntityNotFoundException("Job with ID " + jobId + " not found."));
    }

    public List<String> getResults(String service) {
        List<JobResult> results = jobResultRepository.findAllByServiceType(service);
        return results.stream().map(JobResult::getResultData).toList();
    }
}
