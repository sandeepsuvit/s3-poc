package com.needle.storage.resources;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.needle.storage.service.UploadService;

@RestController
public class FileController {
	@Autowired
	UploadService uploadService;
	
	@PostMapping("/upload-file")
    public PutObjectResult uploadFile(@RequestParam("file") MultipartFile file) throws IllegalStateException, IOException {
		return uploadService.uploadFile(file);
    }
}
