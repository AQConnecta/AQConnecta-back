package com.aqConnecta.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.aqConnecta.config.AWSClientConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@RequiredArgsConstructor
@Service
public class DocumentoService {

    private final AmazonS3 amazonS3;
    private final AWSClientConfig awsClientConfig;

    public String upload(MultipartFile file) {
        File localFile = convertMultipartFileToFile(file);
        String bucketName = awsClientConfig.getBucketName();
        String fileName = file.getOriginalFilename();
        PutObjectRequest request = new PutObjectRequest(bucketName, fileName, localFile);
        PutObjectResult result = amazonS3.putObject(request);

        return amazonS3.getUrl(bucketName, fileName).toString();
    }

    private File convertMultipartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try {
            Files.copy(file.getInputStream(), convertedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return convertedFile;
    }

}
