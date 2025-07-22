package com.leetsync.etl.service;

import blue.strategic.parquet.Dehydrator;
import blue.strategic.parquet.ParquetWriter;
import com.leetsync.etl.model.AcSubmissionRecord;
import org.apache.parquet.schema.LogicalTypeAnnotation;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ParquetFileWriter {
    
    private static final Logger log = LoggerFactory.getLogger(ParquetFileWriter.class);
    
    // Define Parquet schema for AcSubmissionRecord
    private static final MessageType SCHEMA = Types.buildMessage()
            .required(PrimitiveType.PrimitiveTypeName.BINARY).as(LogicalTypeAnnotation.stringType()).named("username")
            .required(PrimitiveType.PrimitiveTypeName.BINARY).as(LogicalTypeAnnotation.stringType()).named("title")
            .required(PrimitiveType.PrimitiveTypeName.BINARY).as(LogicalTypeAnnotation.stringType()).named("titleSlug")
            .required(PrimitiveType.PrimitiveTypeName.INT64).named("timestamp")
            .optional(PrimitiveType.PrimitiveTypeName.INT32).named("runtimeMs")
            .optional(PrimitiveType.PrimitiveTypeName.DOUBLE).named("memoryMb")
            .optional(PrimitiveType.PrimitiveTypeName.INT32).named("difficultyLevel")
            .optional(PrimitiveType.PrimitiveTypeName.BINARY).as(LogicalTypeAnnotation.stringType()).named("tags")
            .optional(PrimitiveType.PrimitiveTypeName.DOUBLE).named("acRate")
            .optional(PrimitiveType.PrimitiveTypeName.INT64).named("totalAccepted")
            .optional(PrimitiveType.PrimitiveTypeName.INT64).named("totalSubmitted")
            .named("AcSubmissionRecord");
    
    // Dehydrator to convert AcSubmissionRecord to Parquet fields
    private static final Dehydrator<AcSubmissionRecord> DEHYDRATOR = (record, valueWriter) -> {
        valueWriter.write("username", record.getUsername());
        valueWriter.write("title", record.getTitle());
        valueWriter.write("titleSlug", record.getTitleSlug());
        valueWriter.write("timestamp", record.getTimestamp());
        
        if (record.getRuntimeMs() != null) {
            valueWriter.write("runtimeMs", record.getRuntimeMs());
        }
        if (record.getMemoryMb() != null) {
            valueWriter.write("memoryMb", record.getMemoryMb());
        }
        if (record.getDifficultyLevel() != null) {
            valueWriter.write("difficultyLevel", record.getDifficultyLevel());
        }
        if (record.getTags() != null && record.getTags().length > 0) {
            valueWriter.write("tags", String.join(",", record.getTags()));
        }
        if (record.getAcRate() != null) {
            valueWriter.write("acRate", record.getAcRate());
        }
        if (record.getTotalAccepted() != null) {
            valueWriter.write("totalAccepted", record.getTotalAccepted());
        }
        if (record.getTotalSubmitted() != null) {
            valueWriter.write("totalSubmitted", record.getTotalSubmitted());
        }
    };

    public String writeToTempFile(List<AcSubmissionRecord> records) throws IOException {
        if (records.isEmpty()) {
            log.warn("No records to write");
            return null;
        }

        // Generate temp file path with date partitioning structure using Seattle timezone
        ZoneId seattleZone = ZoneId.of("America/Los_Angeles");
        LocalDate date = Instant.ofEpochSecond(records.getFirst().getTimestamp())
                .atZone(seattleZone)
                .toLocalDate();
        String hivePartitionPath = String.format("year=%d/month=%02d/day=%02d", 
            date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        String fileName = String.format("/tmp/acsubmissions/%s/part-%s.parquet", hivePartitionPath, UUID.randomUUID());
        
        // Ensure directory exists
        java.nio.file.Path parentDir = Paths.get(fileName).getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }

        File outputFile = new File(fileName);
        
        // Write using parquet-floor API
        try (ParquetWriter<AcSubmissionRecord> writer = ParquetWriter.writeFile(SCHEMA, outputFile, DEHYDRATOR)) {
            for (AcSubmissionRecord record : records) {
                writer.write(record);
            }
        }
        
        log.info("Wrote {} records to {}", records.size(), fileName);
        return fileName;
    }
}