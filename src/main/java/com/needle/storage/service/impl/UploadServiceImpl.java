package com.needle.storage.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.needle.storage.service.UploadService;
import com.needle.storage.utils.StorageException;
import com.needle.storage.utils.Utility;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

	@Autowired
	private AmazonS3 s3client;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	@Value("${s3.storage.location}")
	private Path rootLocation;

	@Override
	public void downloadFile(String keyName) {
		try {
			System.out.println("Downloading an object");
			S3Object s3object = s3client.getObject(new GetObjectRequest(bucketName, keyName));
			System.out.println("Content-Type: " + s3object.getObjectMetadata().getContentType());
			Utility.displayText(s3object.getObjectContent());
			log.info("===================== Import File - Done! =====================");
		} catch (AmazonServiceException ase) {
			log.info("Caught an AmazonServiceException from GET requests, rejected reasons:");
			log.info("Error Message:    " + ase.getMessage());
			log.info("HTTP Status Code: " + ase.getStatusCode());
			log.info("AWS Error Code:   " + ase.getErrorCode());
			log.info("Error Type:       " + ase.getErrorType());
			log.info("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			log.info("Caught an AmazonClientException: ");
			log.info("Error Message: " + ace.getMessage());
		} catch (IOException ioe) {
			log.info("IOE Error Message: " + ioe.getMessage());
		}
	}

	@Override
	public PutObjectResult uploadFile(MultipartFile file) throws IllegalStateException, IOException {
		String filename = generateFileName(file);
		String fileUrl = rootLocation + "/" + generateFileName(file);

		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file " + filename);
			}

			// Get the file to upload and store in temp location
			File fileToUpload = convertMultiPartToFile(file);

			PutObjectResult result = uploadFileTos3bucket(fileUrl, fileToUpload);

			// Remove file from temp location
			cleanUp(fileToUpload);

			return result;
		} catch (AmazonServiceException ase) {
			log.info("Caught an AmazonServiceException from PUT requests, rejected reasons:");
			log.info("Error Message:    " + ase.getMessage());
			log.info("HTTP Status Code: " + ase.getStatusCode());
			log.info("AWS Error Code:   " + ase.getErrorCode());
			log.info("Error Type:       " + ase.getErrorType());
			log.info("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			log.info("Caught an AmazonClientException: ");
			log.info("Error Message: " + ace.getMessage());
		}
		return null;
	}

	/**
	 * Remove file reference from the temp directory
	 * 
	 * @param path
	 * @throws IOException
	 */
	private void cleanUp(File file) throws IOException {
		// Remove file from temp location
		if (file.exists()) {
			Files.delete(Paths.get(file.getPath()));
		}
	}

	/**
	 * Convert multipart file to file object
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}

	/**
	 * Generate file name
	 * 
	 * @param multiPart
	 * @return
	 */
	private String generateFileName(MultipartFile multiPart) {
		String filename = StringUtils.cleanPath(multiPart.getOriginalFilename());
		return new StringBuilder().append(new Date().getTime()).append("-").append(filename.replace(" ", "_"))
				.toString();
	}

	/**
	 * Upload file to s3
	 * 
	 * @param fileName
	 * @param file
	 * @return
	 */
	private PutObjectResult uploadFileTos3bucket(String fileName, File file) {
		return s3client.putObject(new PutObjectRequest(bucketName, fileName, file));
	}

	/**
	 * Delete file from s3
	 * 
	 * @param fileUrl
	 * @return
	 */
	private String deleteFileFromS3Bucket(String fileUrl) {
		String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
		s3client.deleteObject(new DeleteObjectRequest(bucketName + "/", fileName));
		return "Successfully deleted";
	}

}
