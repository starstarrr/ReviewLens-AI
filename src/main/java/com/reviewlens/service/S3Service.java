package com.reviewlens.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
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

    /**
     * Uploads a JSON report to S3.
     *
     * @param objectKey   S3 object key
     * @param jsonContent JSON content
     * @return uploaded object key
     */
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

    /**
     * Downloads a JSON report from S3.
     *
     * @param objectKey S3 object key
     * @return JSON content
     */
    public String downloadJson(String objectKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);

            return response.asUtf8String();

        } catch (S3Exception exception) {
            throw new IllegalStateException(
                    "Failed to download JSON report from S3: "
                            + exception.awsErrorDetails().errorMessage(),
                    exception);
        }
    }
}