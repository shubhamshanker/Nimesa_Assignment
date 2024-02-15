package com.shubham.nimesa_assignment.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.shubham.nimesa_assignment.JobStatus;
import com.shubham.nimesa_assignment.model.S3Files;
import com.shubham.nimesa_assignment.repository.JobRepository;
import com.shubham.nimesa_assignment.repository.S3FilesRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.shubham.nimesa_assignment.model.Job;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;



@Service
public class S3Service {

    private final JobRepository jobRepository;
    private final S3FilesRepository s3FilesRepository;
    private final AmazonS3 amazonS3;

    public S3Service(JobRepository jobRepository, S3FilesRepository s3FilesRepository, AmazonS3 amazonS3) {
        this.jobRepository = jobRepository;
        this.s3FilesRepository = s3FilesRepository;
        this.amazonS3 = amazonS3;
    }

    @Async
    public CompletableFuture<Long> discoverS3BucketObjects(String bucketName) {
        Job job = new Job();
        job.setJobStatus(JobStatus.IN_PROGRESS);
        job.setBucketName(bucketName);
        job = jobRepository.save(job);

        try {
            ListObjectsV2Result result = amazonS3.listObjectsV2(bucketName);

            List<S3Files> s3Files = new ArrayList<>();
            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                S3Files s3File = new S3Files();
                s3File.setBucketName(bucketName);
                s3File.setFileName(objectSummary.getKey());
                s3File.setJob(job);
                s3Files.add(s3File);
            }

            List<S3Files> existantFiles = s3FilesRepository.findByBucketName(bucketName);

            Set<String> existantFileNames = existantFiles.stream()
                    .map(S3Files::getFileName)
                    .collect(Collectors.toSet());

            List<S3Files> filteredFiles = s3Files.stream()
                    .filter(file -> !existantFileNames.contains(file.getFileName()))
                    .toList();

            s3FilesRepository.saveAll(filteredFiles);

            job.setJobStatus(JobStatus.SUCCESS);
            jobRepository.save(job);
        }
        catch (Exception e) {
            job.setJobStatus(JobStatus.FAILED);
            jobRepository.save(job);
            return CompletableFuture.completedFuture(job.getJobId());
        }

        return CompletableFuture.completedFuture(job.getJobId());
    }

    public Integer getS3BucketObjectsCount(String bucketName) {
        List<S3Files> existantFiles = s3FilesRepository.findByBucketName(bucketName);
        return existantFiles.size();
    }

    public List<String> getS3BucketObjectsLike(String bucketName, String pattern) {
        List<S3Files> existentFiles = s3FilesRepository.findByBucketName(bucketName);
        Pattern compiledPattern = Pattern.compile(pattern);

        return existentFiles.stream()
                .map(S3Files::getFileName)
                .filter(fileName -> compiledPattern.matcher(fileName).find())
                .collect(Collectors.toList());
    }
}
