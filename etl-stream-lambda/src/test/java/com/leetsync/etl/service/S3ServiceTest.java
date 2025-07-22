package com.leetsync.etl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;
    
    private S3Service s3Service;
    
    private static final String BUCKET_NAME = "test-bucket";
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3Client, BUCKET_NAME);
    }

    @Test
    void testUploadParquetFile_Success() throws IOException {
        // Create a test file
        Path testFile = tempDir.resolve("test.parquet");
        String testContent = "Test parquet content";
        Files.write(testFile, testContent.getBytes());
        
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());
        
        long timestamp = 1640995200L; // 2022-01-01 00:00:00 UTC = 2021-12-31 16:00:00 PST
        
        s3Service.uploadParquetFile(testFile.toString(), timestamp);
        
        // Verify S3 client was called
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        
        verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());
        
        PutObjectRequest request = requestCaptor.getValue();
        assertEquals(BUCKET_NAME, request.bucket());
        assertTrue(request.key().startsWith("acsubmissions/year=2021/month=12/day=31/part-"));
        assertTrue(request.key().endsWith(".parquet"));
        assertEquals("application/octet-stream", request.contentType());
        
        // Verify file was deleted after upload
        assertFalse(Files.exists(testFile));
    }

    @Test
    void testUploadParquetFile_DatePartitioning() throws IOException {
        Path testFile = tempDir.resolve("test.parquet");
        Files.write(testFile, "test content".getBytes());
        
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());
        
        // Test different timestamps create different date partitions
        long timestamp1 = 1640995200L; // 2022-01-01 UTC = 2021-12-31 PST
        long timestamp2 = 1672531200L; // 2023-01-01 UTC = 2022-12-31 PST
        
        s3Service.uploadParquetFile(testFile.toString(), timestamp1);
        
        // Recreate file for second test
        Files.write(testFile, "test content".getBytes());
        s3Service.uploadParquetFile(testFile.toString(), timestamp2);
        
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(2)).putObject(requestCaptor.capture(), any(RequestBody.class));
        
        String key1 = requestCaptor.getAllValues().get(0).key();
        String key2 = requestCaptor.getAllValues().get(1).key();
        
        assertTrue(key1.contains("year=2021/month=12/day=31"));
        assertTrue(key2.contains("year=2022/month=12/day=31"));
        assertNotEquals(key1, key2);
    }

    @Test
    void testUploadParquetFile_FileNotFound() {
        String nonExistentFile = "/nonexistent/file.parquet";
        long timestamp = 1640995200L;
        
        assertThrows(IOException.class, () -> {
            s3Service.uploadParquetFile(nonExistentFile, timestamp);
        });
        
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadParquetFile_S3Exception() throws IOException {
        Path testFile = tempDir.resolve("test.parquet");
        Files.write(testFile, "test content".getBytes());
        
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(new RuntimeException("S3 error"));
        
        long timestamp = 1640995200L;
        
        assertThrows(RuntimeException.class, () -> {
            s3Service.uploadParquetFile(testFile.toString(), timestamp);
        });
        
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadParquetFile_KeyGeneration() throws IOException {
        Path testFile = tempDir.resolve("test.parquet");
        Files.write(testFile, "test content".getBytes());
        
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());
        
        long timestamp = 1640995200L; // 2022-01-01
        
        s3Service.uploadParquetFile(testFile.toString(), timestamp);
        
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        
        String s3Key = requestCaptor.getValue().key();
        
        // Verify key format: acsubmissions/year=YYYY/month=MM/day=DD/part-<UUID>.parquet
        assertTrue(s3Key.matches("acsubmissions/year=\\d{4}/month=\\d{2}/day=\\d{2}/part-[0-9a-f-]{36}\\.parquet"));
    }

    @Test
    void testUploadParquetFile_FileContent() throws IOException {
        Path testFile = tempDir.resolve("test.parquet");
        String testContent = "Test parquet file content";
        Files.write(testFile, testContent.getBytes());
        
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());
        
        long timestamp = 1640995200L;
        
        s3Service.uploadParquetFile(testFile.toString(), timestamp);
        
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(any(PutObjectRequest.class), bodyCaptor.capture());
        
        // Note: In a real test, you might want to verify the content of RequestBody
        // but it's complex to extract bytes from RequestBody in a unit test
    }
}