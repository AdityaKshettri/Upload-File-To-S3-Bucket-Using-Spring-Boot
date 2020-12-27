package com.aditya.project.util;

import com.aditya.project.exception.ErrorCatalog;
import com.aditya.project.exception.ServiceException;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.time.LocalDateTime;

@Slf4j
@Component
public class S3ConnectorUtil {

    private AmazonS3 amazonS3Client;

    @Value("${amazon.s3.properties.endpoint-url}")
    private String endpointUrl;

    @Value("${amazon.s3.properties.access-key}")
    private String accessKey;

    @Value("${amazon.s3.properties.secret-key}")
    private String secretKey;

    @Value("${amazon.s3.properties.bucket-name}")
    private String bucketName;

    @Value("${amazon.s3.properties.region-name}")
    private String regionName;

    @PostConstruct
    private void initializeAmazonS3Client() {
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        this.amazonS3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpointUrl, regionName))
                .build();
    }

    public String putS3Object(String fileName, String objectPath) {
        String fileUrl;
        try {
            String fileName_ = generateFileName(fileName);
            fileUrl = endpointUrl + "/" + bucketName + "/" + fileName_;
            deleteFileFromS3Bucket(fileUrl);
            TransferManager transferManager = TransferManagerBuilder.standard()
                    .withS3Client(amazonS3Client)
                    .build();
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName_, new File(objectPath))
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            request.setGeneralProgressListener(progressEvent -> log.info("Transferred Bytes : " + progressEvent.getBytesTransferred()));
            Upload upload = transferManager.upload(request);
            upload.waitForUploadResult();
        } catch (AmazonServiceException e) {
            log.error("The call was transferred successfully, but Amazon S3 could not process it!!", e);
            throw new ServiceException(ErrorCatalog.S3_CONNECTION__ERROR, e);
        } catch (SdkClientException e) {
            log.error("Amazon S3 could not be contacted for a response or the client couldn't parse it!!", e);
            throw new ServiceException(ErrorCatalog.S3_CONNECTION__ERROR, e);
        } catch (AmazonClientException e) {
            log.error("The call couldn't be transferred successfully to Amazon S3!!", e);
            throw new ServiceException(ErrorCatalog.S3_CONNECTION__ERROR, e);
        } catch (InterruptedException e) {
            log.error("Process Interrupted!!", e);
            throw new ServiceException(ErrorCatalog.INTERRUPTED_ERROR, e);
        } catch (Exception e) {
            log.error("Application error!!", e);
            throw new ServiceException(ErrorCatalog.APPLICATION_ERROR, e);
        }
        return fileUrl;
    }

    private String generateFileName(String fileName) {
        String dateTime = LocalDateTime.now().toString();
        dateTime = dateTime.substring(0, dateTime.indexOf('.'))
                .replace('-', '.')
                .replace('T', '-')
                .replace(':', '.');
        return fileName + "-" + dateTime;
        // FILENAME-yyyy.MM.dd-hh.mm-ss
    }

    private void deleteFileFromS3Bucket(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
        log.info("Successfully deleted file, if already exists!");
    }
}
