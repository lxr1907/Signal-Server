/*
 * Copyright (C) 2013 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.textsecuregcm.s3;

import java.net.URL;
import java.util.Date;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

public class UrlSigner {

	private static final long DURATION = 60 * 60 * 1000;

	private final AWSCredentials credentials;
	private final String bucket;

	public UrlSigner(String accessKey, String accessSecret, String bucket) {
		this.credentials = new BasicAWSCredentials(accessKey, accessSecret);
		this.bucket = bucket;
	}

	public URL getPreSignedUrl(long attachmentId, HttpMethod method, boolean unaccelerated) {
		AmazonS3 client = new AmazonS3Client(credentials);
		client.setRegion(RegionUtils.getRegion("ap-northeast-1"));
		GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, String.valueOf(attachmentId),
				method);

		request.setExpiration(new Date(System.currentTimeMillis() + DURATION));
		request.setContentType("application/octet-stream");

		if (unaccelerated) {
			client.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build());
		} else {
			client.setS3ClientOptions(S3ClientOptions.builder().setAccelerateModeEnabled(true).build());
		}

		return client.generatePresignedUrl(request);
	}

	public static void main(String[] args) {

//awsAttachments: # AWS S3 configuration
//    accessKey: AKIAUHWNR6PWT6ZAPWV6
//    accessSecret: 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
//    bucket: signallxrtalk
//    region: ap-northeast-1
		String accessKey = "AKIAUHWNR6PWT6ZAPWV6";
		String accessSecret = "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";
		String attachmentId = "6730113429384350585";
		String bucket = "signallxrtalk";
		String region = " ap-northeast-1";
//		AmazonS3 client = new AmazonS3Client(new BasicAWSCredentials(accessKey, accessSecret));
//		client.setRegion(RegionUtils.getRegion("ap-northeast-1"));
//		GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, String.valueOf(attachmentId),
//				HttpMethod.GET);
//
//		request.setExpiration(new Date(System.currentTimeMillis() + DURATION));
//		request.setContentType("application/octet-stream");

		// client.setS3ClientOptions(S3ClientOptions.builder().setAccelerateModeEnabled(true).build());

//		String url = client.generatePresignedUrl(request).toString();
//		System.out.println(url);
		// String url = client
		// .generatePresignedUrl(bucket, attachmentId, new
		// Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
		// .toString();
		// System.out.println(url);

		// Create an S3Presigner using the default region and credentials.
		// This is usually done at application startup, because creating a presigner can
		// be expensive.
		Regions clientRegion = Regions.AP_NORTHEAST_1;
		String bucketName = bucket;
		String objectKey = "6730113429384350585";

		try {
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(clientRegion)
					.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, accessSecret))).build();
			// Set the presigned URL to expire after one hour.
			java.util.Date expiration = new java.util.Date();
			long expTimeMillis = expiration.getTime();
			expTimeMillis += 1000 * 60 * 60;
			expiration.setTime(expTimeMillis);

			// Generate the presigned URL.
			System.out.println("Generating pre-signed URL.");
			GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName,
					objectKey).withMethod(HttpMethod.GET).withExpiration(expiration);
			generatePresignedUrlRequest.setContentType("application/octet-stream");
			URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

			System.out.println("" + url.toString());
		} catch (AmazonServiceException e) {
			// The call was transmitted successfully, but Amazon S3 couldn't process
			// it, so it returned an error response.
			e.printStackTrace();
		} catch (SdkClientException e) {
			// Amazon S3 couldn't be contacted for a response, or the client
			// couldn't parse the response from Amazon S3.
			e.printStackTrace();
		}

	}
}
