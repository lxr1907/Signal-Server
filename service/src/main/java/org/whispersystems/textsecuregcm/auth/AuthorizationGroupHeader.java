/**
 * Copyright (C) 2013 Open WhisperSystems
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.textsecuregcm.auth;


import org.signal.zkgroup.InvalidInputException;
import org.signal.zkgroup.InvalidRedemptionTimeException;
import org.signal.zkgroup.ServerSecretParams;
import org.signal.zkgroup.VerificationFailedException;
import org.signal.zkgroup.auth.AuthCredentialPresentation;
import org.signal.zkgroup.auth.ServerZkAuthOperations;
import org.signal.zkgroup.groups.GroupPublicParams;
import org.whispersystems.textsecuregcm.storage.GroupEntity;
import org.whispersystems.textsecuregcm.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * lxr 20210316
 */
public class AuthorizationGroupHeader {

    private final GroupEntity groupEntity;
    private final String password;

    private AuthorizationGroupHeader(GroupEntity groupEntity, String password) {
        this.groupEntity = groupEntity;
        this.password = password;
    }

    public static AuthorizationGroupHeader fromUserAndPassword(ServerZkAuthOperations zkAuthOperations, String user, String password) throws InvalidAuthorizationHeaderException {
        try {
            zkAuthOperations.verifyAuthCredentialPresentation(new GroupPublicParams(Base64.encodeBytesToBytes(user.getBytes(StandardCharsets.UTF_8))),
                    new AuthCredentialPresentation(Base64.encodeBytesToBytes(password.getBytes(StandardCharsets.UTF_8))));
            GroupEntity groupEntity = new GroupEntity();
            groupEntity.setGroupPublicParams(new GroupPublicParams(user.getBytes(StandardCharsets.UTF_8)));
            return new AuthorizationGroupHeader(groupEntity,
                    password);
        } catch (NumberFormatException | InvalidInputException nfe) {
            throw new InvalidAuthorizationHeaderException(nfe);
        } catch (VerificationFailedException e) {
            e.printStackTrace();
        } catch (InvalidRedemptionTimeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args)  {
        String serverSecret="ACyAUqsm7WRUIWlWPkizJ6xAq0ZjZmlNCnDo28GGifMMCjbOnPZIGsmd2ICN5EqL1BjqO/O9m0otsbLjz8CehwaovbaTOKnbJPaXoTPWkSDH9yPcOnmvj6xdm4DOiB37MVTut6DL9yHVdg7S/XnR2amQ6WoyGRI+MwPCFspVimwHKd4oYHDrKTjCndhZnKx2oQcgXVp05JtJfsIRBrBRNg8PEr6FAcIRWeCG6quLxIqfcuPf3Bdn1AgvQz5II369BZsU5rVKTnwjF6CZmVN8UO++JqaK9kqO9YLP3zpMLB4AEmnezjJ7kHFNQaVwVRm0p9Rt6q42P7UssjavjQXngQzEEMwqHt+K0EJuQcLP6K2OtCw4Ti/fFOZ1sUDFN7DVC/YNGrlVtwuNGiQSgYK/CduMeLLbPVejvNeS/vATQ9hOfipj8mR94eeS28PCVRXUpSRNMRc0WAjqurHi82XfIX+vw1vBwQ3XEK0uHIOtKzCh9QAMrxYJxHIdMjSV5aXIBL4xDBDGUP55J3FtzBn58jEMd8C2cwgfhfJGhcgXxh0A8Ne44mRVHX1jXSdtDQ2b+LSxyGrErI5hPgfImPpF3zqDNt4ddJU8EOv7W5orrnEOb9jCoKdc0lb5SWGthwXTDldR6IeiVdnCn7vQBFei9icAPMQ6O14c/dzZogRNIVEBBLaucQxAeWLjKBYdmleRWhYH6mOOW7nMD7GKBkwS3QAgQyAAqp2pFLsWv2Tn8WdcfieFCMaY/p1DTrP/x3zUCiC3TbjGXd0fi4/kpdW4C6QxS+HjHIEESCGCbnrdlSQKKoYxNSJtu4/NtctgFzxo7anCcfi/o8w2O+yJy5ghdgeW4lcDbAljgGucmQYyZ7W5zZsuHSbmH3b77p7nXGP/asSFJoOJXAGi8m2iXhVybpQ0AeyOi36an5E6vSOzzdEy4MoLOGYEd9irEB1caHrh5kmxUmmYyq4RC6HWu5wjJQvmQwtYqMd15Mk6wIJ7MDQIBrt4skD5RenrSWI7vfBnLg";
        String user="0057230ae5597cc1277fe397a03f9d0aa1c53bb6887bd1681a24caffe42d829bae0c4c75f994349897637b9a3e5d2f63c12c2242f6071626179cb8a23bba93d8677a25f1ee1d677ab90a08cc964acee05d4794961c6de90f704659775707570902";
        String password="008cdb5806947b1a5cf24a4e5f76c511e3e19d7e735490f47649ca525e048b6f029a2dd42bb7e3007d9102c4317c4d409d89e0a146f66579b45515c879b524312fca85bac71dddcef52434fc73936249ce7f1710fe79e83ae6dbfac4ee15a24f0e9e13784c9a55feaec943243da6d4536ab09086c769645b1c1f694075fce59f3e8432b1aadc30c5db9d208ed86931394450eb3ad23f498f7a420a5eb9e505eb1c5009b282cfd2a8ac24690f45da55c6841f80c153bd4193db52ac095c9b9c8c4ce000000000000000eb22294950649f0b66918a3834cd909ae1ed8b710af7d98def94c70d2c2efd0504151f5df573695c5c6310558d188e6805e9b6eb682daf4b2e8aced3a46af6057f23007e92a87795282a8132cc6db63b477c724787c4c918ef82799234db7c038867c703707a9ce9a283b1a92a28f611c28b7e709e5b13375cfab4c3be4be90c8157eb46b401eb90cfe61303bcd78ef92a32a355b6e79354211954cacb279b0770b0156d6cb4b00d2d50cb01644cffe9cef0225d3e86a861d011dcadc68d29071346c8efa2c2f7f990acabf8497f94ca1a090d3ea814c76ae3ea36c43455480f26f1e626fdffb8d81fae5fb16724149bc74ba0a825237858e969e2c4fa3a4a16381a977ec13eb02cf56c4be3697591135d07d5bc09ad93b38c8e852ff63bfb5a0f490000";
        ServerSecretParams zkSecretParams = new ServerSecretParams(serverSecret.getBytes());
        ServerZkAuthOperations zkAuthOperations = new ServerZkAuthOperations(zkSecretParams);
        try {
            zkAuthOperations.verifyAuthCredentialPresentation(new GroupPublicParams(Base64.encodeBytesToBytes(user.getBytes(StandardCharsets.UTF_8))),
                new AuthCredentialPresentation(Base64.encodeBytesToBytes(password.getBytes(StandardCharsets.UTF_8))));
        }catch (Exception e){
            System.out.println("error e:"+e.getMessage());
        }
        System.out.println("1234");

    }

    public GroupEntity getGroupEntity() {
        return groupEntity;
    }

    public String getPassword() {
        return password;
    }
}
