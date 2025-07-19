package com.leetsync.etl.service;

import com.leetsync.etl.model.AcSubmissionRecord;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.util.HadoopOutputFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ParquetFileWriter {
    
    private static final Logger log = LoggerFactory.getLogger(ParquetFileWriter.class);
    
    private static final Schema SCHEMA = new Schema.Parser().parse("""
        {
            "type": "record",
            "name": "AcSubmissionRecord",
            "fields": [
                {"name": "username", "type": "string"},
                {"name": "title", "type": "string"},
                {"name": "titleSlug", "type": "string"},
                {"name": "timestamp", "type": "long"},
                {"name": "runtimeMs", "type": ["null", "int"], "default": null},
                {"name": "memoryMb", "type": ["null", "double"], "default": null},
                {"name": "difficultyLevel", "type": ["null", "int"], "default": null},
                {"name": "tags", "type": ["null", {"type": "array", "items": "string"}], "default": null},
                {"name": "acRate", "type": ["null", "double"], "default": null},
                {"name": "totalAccepted", "type": ["null", "long"], "default": null},
                {"name": "totalSubmitted", "type": ["null", "long"], "default": null}
            ]
        }
        """);

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
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = String.format("/tmp/acsubmissions/%s/part-%s.parquet", dateStr, UUID.randomUUID());
        
        // Ensure directory exists
        java.nio.file.Path parentDir = java.nio.file.Paths.get(fileName).getParent();
        if (parentDir != null) {
            java.nio.file.Files.createDirectories(parentDir);
        }

        Path path = new Path(fileName);
        Configuration conf = new Configuration();
        
        try (org.apache.parquet.hadoop.ParquetWriter<GenericRecord> writer = 
                AvroParquetWriter.<GenericRecord>builder(HadoopOutputFile.fromPath(path, conf))
                    .withSchema(SCHEMA)
                    .withCompressionCodec(CompressionCodecName.SNAPPY)
                    .build()) {
            
            for (AcSubmissionRecord record : records) {
                GenericRecord avroRecord = convertToAvroRecord(record);
                writer.write(avroRecord);
            }
            
            log.info("Wrote {} records to {}", records.size(), fileName);
            return fileName;
        }
    }
    
    private GenericRecord convertToAvroRecord(AcSubmissionRecord record) {
        GenericRecord avroRecord = new GenericData.Record(SCHEMA);
        
        avroRecord.put("username", record.getUsername());
        avroRecord.put("title", record.getTitle());
        avroRecord.put("titleSlug", record.getTitleSlug());
        avroRecord.put("timestamp", record.getTimestamp());
        avroRecord.put("runtimeMs", record.getRuntimeMs());
        avroRecord.put("memoryMb", record.getMemoryMb());
        avroRecord.put("difficultyLevel", record.getDifficultyLevel());
        avroRecord.put("tags", record.getTags());
        avroRecord.put("acRate", record.getAcRate());
        avroRecord.put("totalAccepted", record.getTotalAccepted());
        avroRecord.put("totalSubmitted", record.getTotalSubmitted());
        
        return avroRecord;
    }
}