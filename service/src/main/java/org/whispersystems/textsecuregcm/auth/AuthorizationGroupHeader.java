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


import liquibase.pro.packaged.G;
import org.signal.zkgroup.InvalidInputException;
import org.signal.zkgroup.InvalidRedemptionTimeException;
import org.signal.zkgroup.VerificationFailedException;
import org.signal.zkgroup.auth.AuthCredentialPresentation;
import org.signal.zkgroup.auth.ServerZkAuthOperations;
import org.signal.zkgroup.groups.GroupPublicParams;
import org.whispersystems.textsecuregcm.storage.GroupEntity;
import org.whispersystems.textsecuregcm.util.Base64;
import org.whispersystems.textsecuregcm.util.Util;

import java.io.IOException;
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
            zkAuthOperations.verifyAuthCredentialPresentation(new GroupPublicParams(user.getBytes(StandardCharsets.UTF_8)),
                    new AuthCredentialPresentation(password.getBytes(StandardCharsets.UTF_8)));
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

//    public static AuthorizationGroupHeader fromFullHeader(String header) throws InvalidAuthorizationHeaderException {
//        try {
//            if (header == null) {
//                throw new InvalidAuthorizationHeaderException("Null header");
//            }
//
//            String[] headerParts = header.split(" ");
//
//            if (headerParts == null || headerParts.length < 2) {
//                throw new InvalidAuthorizationHeaderException("Invalid authorization header: " + header);
//            }
//
//            if (!"Basic".equals(headerParts[0])) {
//                throw new InvalidAuthorizationHeaderException("Unsupported authorization method: " + headerParts[0]);
//            }
//
//            String concatenatedValues = new String(Base64.decode(headerParts[1]));
//
//            if (Util.isEmpty(concatenatedValues)) {
//                throw new InvalidAuthorizationHeaderException("Bad decoded value: " + concatenatedValues);
//            }
//
//            String[] credentialParts = concatenatedValues.split(":");
//
//            if (credentialParts == null || credentialParts.length < 2) {
//                throw new InvalidAuthorizationHeaderException("Badly formated credentials: " + concatenatedValues);
//            }
//
//            return fromUserAndPassword(credentialParts[0], credentialParts[1]);
//        } catch (IOException ioe) {
//            throw new InvalidAuthorizationHeaderException(ioe);
//        }
//    }

    public GroupEntity getGroupEntity() {
        return groupEntity;
    }

    public String getPassword() {
        return password;
    }
}
