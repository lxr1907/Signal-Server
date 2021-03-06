package org.whispersystems.textsecuregcm.sms;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class Example {
  // Find your Account Sid and Token at twilio.com/user/account
  public static final String ACCOUNT_SID = "AC854183d264ba2198f8051790c58e1a25";
  public static final String AUTH_TOKEN = "0abfc7b56b7840b5c042a31279bfbf1d";

  public static void main(String[] args) {
    Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

    Message message = Message.creator(new PhoneNumber("+17209034247"),
        new PhoneNumber("+8617348500087"),
        "This is the ship that made the Kessel Run in fourteen parsecs?").create();
    System.out.println(message.getSid());
  }
}