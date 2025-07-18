package com.leetsync.etl.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class S3Service {
    
    private static final Logger log = LoggerFactory.getLogger(S3Service.class);
    
    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public void uploadParquetFile(String localFilePath, long timestampFromFirstRecord) throws IOException {
        // Generate S3 key with date partitioning
        LocalDate date = LocalDate.ofEpochDay(timestampFromFirstRecord / 86400);
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String s3Key = String.format("acsubmissions/%s/part-%s.parquet", dateStr, UUID.randomUUID());
        
        byte[] fileContent = Files.readAllBytes(Paths.get(localFilePath));
        
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType("application/octet-stream")
                .build();
        
        s3Client.putObject(putRequest, RequestBody.fromBytes(fileContent));
        
        log.info("Uploaded {} bytes to s3://{}/{}", fileContent.length, bucketName, s3Key);
        
        // Clean up temp file
        Files.deleteIfExists(Paths.get(localFilePath));
    }
}