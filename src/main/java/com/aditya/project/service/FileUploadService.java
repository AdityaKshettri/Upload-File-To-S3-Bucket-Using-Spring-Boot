package com.aditya.project.service;

import com.aditya.project.util.S3ConnectorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private static final String FILE_NAME = "file.text";

    private final S3ConnectorUtil s3ConnectorUtil;

    public void uploadFileToS3Bucket() {
        String fileUrl = s3ConnectorUtil.putS3Object(FILE_NAME, Paths.get(FILE_NAME).toString());
        log.info("File uploaded successfully on S3 Bucket at : " + fileUrl);
    }
}
