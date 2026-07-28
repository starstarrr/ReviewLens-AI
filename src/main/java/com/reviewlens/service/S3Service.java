package com.reviewlens.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.charset.StandardCharsets;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(
            S3Client s3Client,
            @Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public String uploadJson(String objectKey, String jsonContent) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType("application/json")
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromString(
                            jsonContent,
                            StandardCharsets.UTF_8));

            return objectKey;
        } catch (S3Exception exception) {
            throw new IllegalStateException(
                    "Failed to upload JSON report to S3: "
                            + exception.awsErrorDetails().errorMessage(),
                    exception);
        }
    }
}
