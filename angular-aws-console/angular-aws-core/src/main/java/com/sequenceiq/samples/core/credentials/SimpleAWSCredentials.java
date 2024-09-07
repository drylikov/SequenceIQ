package com.sequenceiq.samples.core.credentials;

import com.amazonaws.auth.AWSCredentials;
import org.apache.commons.lang3.Validate;

/**
 * User: doktoric
 */
public class SimpleAWSCredentials implements AWSCredentials {
	private final String accessKey;
	private final String secretKey;

	public SimpleAWSCredentials(String accessKey, String secretKey) {
		Validate.notNull(accessKey, "accessKey can not be null!");
		Validate.notNull(secretKey, "secretKey can not be null!");
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}

	@Override
	public String getAWSAccessKeyId() {
		return accessKey;
	}

	@Override
	public String getAWSSecretKey() {
		return secretKey;
	}
}
