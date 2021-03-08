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
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Builder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

@Configuration
public class AwsServicesConfig {
	@Value("${cloud.aws.region.static}")
	private String region;

	@Value("${cloud.aws.credentials.access-key}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret-key}")
	private String secretKey;

	@Value("${spring.profiles.active:unknown}")
	private String activeProfile;

	/*
	 * Used only for local development when using localstack <br/> Please refer to
	 * the doc on localstack under `docs/localstack.md` to lean more on how to use
	 * it for local development.
	 */
	@Value("${localstack.url:#{''}}")
	private String localStackUrl;

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
		return apply(AmazonSQSAsyncClientBuilder.standard()).build();
	}

	/**
	 * AmazonS3 is an interface for accessing the S3. Each method will return a Java
	 * object representing the operation.
	 * 
	 * @return
	 */
	@Bean
	public AmazonS3 s3client() {
		return ((AmazonS3Builder<AmazonS3ClientBuilder, AmazonS3>) apply(AmazonS3ClientBuilder.standard()))
				.withPathStyleAccessEnabled(true).build();
	}

	/**
	 * Generic utility to configure the builder
	 * 
	 * @param <T>
	 * @param <C>
	 * @param builder
	 * @return
	 */
	private <T extends AwsClientBuilder<T, C>, C> AwsClientBuilder<T, C> apply(AwsClientBuilder<T, C> builder) {
		// Used for local development
		if (StringUtils.pathEquals(activeProfile, "loc")) {
			return builder.withEndpointConfiguration(endpointConfiguration()).withCredentials(credentialsProvider());
		}
		// Used in production
		return builder.withRegion(region).withCredentials(credentialsProvider());
	}

	/**
	 * Credentials provider for aws
	 * 
	 * @return
	 */
	private AWSCredentialsProvider credentialsProvider() {
		return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
	}

	/**
	 * Used only for development purpose
	 * 
	 * @return
	 */
	private EndpointConfiguration endpointConfiguration() {
		return new AwsClientBuilder.EndpointConfiguration(localStackUrl, region);
	}
}
