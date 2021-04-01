package org.whispersystems.textsecuregcm.push;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.DeliveryPriority;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.bouncycastle.openssl.PEMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.util.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static com.codahale.metrics.MetricRegistry.name;

public class RetryingApnsClient {

  private static final Logger logger = LoggerFactory.getLogger(RetryingApnsClient.class);

  private final ApnsClient apnsClient;

  RetryingApnsClient(String apnCertificate, String apnKey, boolean sandbox)
      throws IOException
  {
    MetricRegistry                      metricRegistry  = SharedMetricRegistries.getOrCreate(Constants.METRICS_NAME);


    this.apnsClient = new ApnsClientBuilder().setClientCredentials(initializeCertificate(apnCertificate),
                                                                   initializePrivateKey(apnKey), null)
                                             .setApnsServer(sandbox ? ApnsClientBuilder.DEVELOPMENT_APNS_HOST : ApnsClientBuilder.PRODUCTION_APNS_HOST)
                                             .build();
  }

  @VisibleForTesting
  public RetryingApnsClient(ApnsClient apnsClient) {
    this.apnsClient = apnsClient;
  }

  ListenableFuture<ApnResult> send(final String apnId, final String topic, final String payload, final Date expiration) {
    SettableFuture<ApnResult>  result       = SettableFuture.create();
    SimpleApnsPushNotification notification = new SimpleApnsPushNotification(apnId, topic, payload,
        Instant.ofEpochMilli(expiration.getTime()), DeliveryPriority.IMMEDIATE);

    apnsClient.sendNotification(notification);//.addListener(new ResponseHandler(result));

    return result;
  }

  void disconnect() {
    apnsClient.close();
  }

  private static X509Certificate initializeCertificate(String pemCertificate) throws IOException {
    PEMReader reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(pemCertificate.getBytes())));
    return (X509Certificate) reader.readObject();
  }

  private static PrivateKey initializePrivateKey(String pemKey) throws IOException {
    PEMReader reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(pemKey.getBytes())));
    return ((KeyPair) reader.readObject()).getPrivate();
  }

  private static final class ResponseHandler implements GenericFutureListener<Future<PushNotificationResponse<SimpleApnsPushNotification>>> {

    private final SettableFuture<ApnResult> future;

    private ResponseHandler(SettableFuture<ApnResult> future) {
      this.future = future;
    }

    @Override
    public void operationComplete(io.netty.util.concurrent.Future<PushNotificationResponse<SimpleApnsPushNotification>> result) {
//      try {
//        PushNotificationResponse<SimpleApnsPushNotification> response = result.get();
//
//        if (response.isAccepted()) {
//          future.set(new ApnResult(ApnResult.Status.SUCCESS, null));
//        } else if ("Unregistered".equals(response.getRejectionReason()) ||
//                   "BadDeviceToken".equals(response.getRejectionReason()))
//        {
//          future.set(new ApnResult(ApnResult.Status.NO_SUCH_USER, response.getRejectionReason()));
//        } else {
//          logger.warn("Got APN failure: " + response.getRejectionReason());
//          future.set(new ApnResult(ApnResult.Status.GENERIC_FAILURE, response.getRejectionReason()));
//        }
//
//      } catch (InterruptedException e) {
//        logger.warn("Interrupted exception", e);
//        future.setException(e);
//      } catch (ExecutionException e) {
//        logger.warn("Execution exception", e.getMessage());
//        future.setException(e.getCause());
//      }
    }
  }

  public static class ApnResult {
    public enum Status {
      SUCCESS, NO_SUCH_USER, GENERIC_FAILURE
    }

    private final Status status;
    private final String reason;

    ApnResult(Status status, String reason) {
      this.status = status;
      this.reason = reason;
    }

    public Status getStatus() {
      return status;
    }

    public String getReason() {
      return reason;
    }
  }

}
