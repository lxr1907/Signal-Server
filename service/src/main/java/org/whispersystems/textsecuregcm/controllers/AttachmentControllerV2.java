package org.whispersystems.textsecuregcm.controllers;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.whispersystems.textsecuregcm.entities.AttachmentDescriptorV2;
import org.whispersystems.textsecuregcm.limits.RateLimiter;
import org.whispersystems.textsecuregcm.limits.RateLimiters;
import org.whispersystems.textsecuregcm.s3.PolicySigner;
import org.whispersystems.textsecuregcm.s3.PostPolicyGenerator;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.util.Pair;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.auth.Auth;

@Path("/v2/attachments")
public class AttachmentControllerV2 extends AttachmentControllerBase {

	private final PostPolicyGenerator policyGenerator;
	private final PolicySigner policySigner;
	private final RateLimiter rateLimiter;

	public AttachmentControllerV2(RateLimiters rateLimiters, String accessKey, String accessSecret, String region,
			String bucket) {
		this.rateLimiter = rateLimiters.getAttachmentLimiter();
		this.policyGenerator = new PostPolicyGenerator(region, bucket, accessKey);
		this.policySigner = new PolicySigner(accessSecret, region);
	}

	@Timed
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/form/upload")
	public AttachmentDescriptorV2 getAttachmentUploadForm(@Auth Account account) throws RateLimitExceededException {
		rateLimiter.validate(account.getNumber());

		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		long attachmentId = generateAttachmentId();
		String objectName = String.valueOf(attachmentId);
		Pair<String, String> policy = policyGenerator.createFor(now, String.valueOf(objectName), 100 * 1024 * 1024);
		String signature = policySigner.getSignature(now, policy.second());

		return new AttachmentDescriptorV2(attachmentId, objectName, policy.first(), "private", "AWS4-HMAC-SHA256",
				now.format(PostPolicyGenerator.AWS_DATE_TIME), policy.second(), signature);
	}

	public static void main(String[] args) {
		// awsAttachments: # AWS S3 configuration
//  accessKey: AKIAUHWNR6PWT6ZAPWV6
//  accessSecret: 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
//  bucket: signallxrtalk
//  region: ap-northeast-1
		String accessKey = "AKIAUHWNR6PWT6ZAPWV6";
		String accessSecret = "LO4Yq8Mequl6TrYJlTQC4Kaz7vfz";
		String attachmentId = "6730113429384350585";
		String bucket = "signallxrtalk";
		String region = " ap-northeast-1";
		PostPolicyGenerator policyGenerator = new PostPolicyGenerator(region, bucket, accessKey);
		PolicySigner  policySigner= new PolicySigner(accessSecret, region);
		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		String objectName = String.valueOf(attachmentId);
		Pair<String, String> policy = policyGenerator.createFor(now, String.valueOf(objectName), 100 * 1024 * 1024);
//		String signature = policySigner.getSignature(now, policy.second());
		Timestamp timestamp = new Timestamp(2021,3,6,5,59,18,0);
		 
        LocalDateTime localDateTimeNoTimeZone = timestamp.toLocalDateTime();
 
        ZonedDateTime zonedDateTime1 = localDateTimeNoTimeZone.atZone(ZoneId.of("Z"));
		String signature = policySigner.getSignature(zonedDateTime1,"AWS4-HMAC-SHA256\n"
				+ "20210306T055918Z"
				+ "\n20210306/ap-northeast-1/s3/aws4_request"
				+ "\n1a4547f2968df64e3c38b9ec7159312af06e53b33fb2567fb99e1cbf7a0ac9fc");
		System.out.println(signature);
	}
}
