package com.needle.storage.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.PutObjectResult;

public interface UploadService {
	void downloadFile(String keyName);
	
	PutObjectResult uploadFile(MultipartFile file) throws IllegalStateException, IOException;

}
