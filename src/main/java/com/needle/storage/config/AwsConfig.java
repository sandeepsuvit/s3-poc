package com.needle.storage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

@Configuration
public class AwsConfig {
	@Value("${cloud.aws.region.static}")
	private String region;

	@Value("${cloud.aws.credentials.access-key}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret-key}")
	private String secretKey;

	@Value("${spring.profiles.active:unknown}")
	private String activeProfile;

	/* Used only when using development environment */
	private static final String LOCALSTACK_URL = "http://localhost:4566";

	/**
	 * QueueMessagingTemplate is a template provider for message conversions used
	 * when using Queues
	 * 
	 * @return
	 */
	@Bean
	public QueueMessagingTemplate queueMessagingTemplate() {
		return new QueueMessagingTemplate(amazonSqs());
	}

	/**
	 * AmazonSQSAsync is an interface for accessing the SQS asynchronously. Each
	 * asynchronous method will return a Java Future object representing the
	 * asynchronous operation.
	 * 
	 * @return
	 */
	@Bean
	@Primary
	public AmazonSQSAsync amazonSqs() {
		// Note: This is to be used only for local development
		if (StringUtils.pathEquals(activeProfile, "loc")) {
			// @formatter:off
			return AmazonSQSAsyncClientBuilder.standard().withCredentials(credentialsProvider())
					.withEndpointConfiguration(endpointConfiguration())
					.build();
			// @formatter:on
		}

		// @formatter:off
		return AmazonSQSAsyncClientBuilder.standard()
				.withRegion(region)
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
				.build();
		// @formatter:on
	}

	@Bean
	public AmazonS3 s3client() {
		// Note: This is to be used only for local development
		if (StringUtils.pathEquals(activeProfile, "loc")) {
			// @formatter:off
			return AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider())
					.withEndpointConfiguration(endpointConfiguration())
					.withPathStyleAccessEnabled(true)
					.build();
			// @formatter:on
		}

		// @formatter:off
		return AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider())
				.withPathStyleAccessEnabled(true)
				.build();
		// @formatter:off
	}
	
	private AWSCredentialsProvider credentialsProvider() {
		return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
	}

	/**
	 * Used only for development purpose
	 * 
	 * @return
	 */
	private EndpointConfiguration endpointConfiguration() {
		return new AwsClientBuilder.EndpointConfiguration(LOCALSTACK_URL, Regions.fromName(region).toString());
	}
}
