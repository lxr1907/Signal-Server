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
package org.whispersystems.textsecuregcm.controllers;

import com.codahale.metrics.annotation.Timed;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.dropwizard.auth.Auth;
import org.signal.zkgroup.InvalidInputException;
import org.signal.zkgroup.auth.AuthCredentialPresentation;
import org.signal.zkgroup.groups.ClientZkGroupCipher;
import org.signal.zkgroup.groups.GroupSecretParams;
import org.signal.zkgroup.groups.ProfileKeyCiphertext;
import org.signal.zkgroup.groups.UuidCiphertext;
import org.signal.zkgroup.profiles.ProfileKeyCredentialPresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.proto.Group;
import org.whispersystems.textsecuregcm.proto.GroupAttributeBlob;
import org.whispersystems.textsecuregcm.proto.Member;
import org.whispersystems.textsecuregcm.redis.ReplicatedJedisPool;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.GroupEntity;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * lxr 20210316
 */
@Path("/v1/groups")
public class GroupController {

    private final Logger logger = LoggerFactory.getLogger(GroupController.class);
    protected final ReplicatedJedisPool cacheClient;

    public GroupController(ReplicatedJedisPool cacheClient) {
        this.cacheClient = cacheClient;
    }

    public static final String GROUP_REDIS_KEY = "group_";
    public static final String GROUP_ADMIN_REDIS_KEY = "group_admin_";
    public static final String GROUP_MEMBER_REDIS_KEY = "group_member_";

    @Timed
    @PUT
    @Consumes("application/x-protobuf")
    @Produces("application/x-protobuf")
    public Group saveGroup(@Auth GroupEntity groupEntity, Group group) throws InvalidInputException {
        var groupKey = groupEntity.getGroupPublicParams();
        System.out.println("group.title:" + group.getTitle().toByteArray().toString());
        System.out.println("group.avatar:" + group.getAvatar());
        System.out.println("group.getAccessControl:" + group.getAccessControl());
        List<org.whispersystems.textsecuregcm.proto.Member> memberList = new ArrayList<>();

        Group.Builder newGroupBuilder = group.toBuilder();
        int i = 0;
        for (Member m : group.getMembersList()) {
            ProfileKeyCredentialPresentation presentation = new ProfileKeyCredentialPresentation(m.getPresentation().toByteArray());
            UuidCiphertext uuidCiphertext = presentation.getUuidCiphertext();
            ProfileKeyCiphertext profileKeyCiphertext = presentation.getProfileKeyCiphertext();
            Member newMember = m.toBuilder()
                    .setUserId(ByteString.copyFrom(uuidCiphertext.serialize()))
                    .setProfileKey(ByteString.copyFrom(profileKeyCiphertext.serialize()))
                    .build();
            memberList.add(newMember);
            newGroupBuilder.setMembers(i, newMember);
            i++;
        }
        Group newGroup = newGroupBuilder.build();
        for (Member m : newGroup.getMembersList()) {
            System.out.println("group.members.role:" + m.getRole());
            System.out.println("group.members.presentation:" + m.getPresentation().toString());
            System.out.println("group.members.userid:" + m.getUserId());
            System.out.println("group.members.profileKey:" + m.getProfileKey());
        }
        try (Jedis jedis = cacheClient.getWriteResource()) {
            jedis.hset(GROUP_REDIS_KEY.getBytes(), groupKey.serialize(), newGroup.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newGroup;
    }

    @Timed
    @GET
    @Consumes("application/x-protobuf")
    @Produces("application/x-protobuf")
    public Group getGroup(@Auth GroupEntity groupEntity) {
        System.out.println("groupEntity:" + groupEntity);
        var groupKey = groupEntity.getGroupPublicParams();
        System.out.println("groupEntity.groupKey:" + groupKey);
        try (Jedis jedis = cacheClient.getReadResource()) {
            byte[] groupByte = jedis.hget(GROUP_REDIS_KEY.getBytes(), groupKey.serialize());
            Group group = null;
            try {
                group = Group.parseFrom(groupByte);
                System.out.println("group.title:" + group.getTitle().toByteArray().toString());
                System.out.println("group.avatar:" + group.getAvatar());
                System.out.println("group.getAccessControl:" + group.getAccessControl());
                System.out.println("group.members0.userid:" + group.getMembers(0).getUserId());
                return group;
            } catch (InvalidProtocolBufferException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Timed
    @PATCH
    @Consumes("application/x-protobuf")
    @Produces("application/x-protobuf")
    public Group patchGroup(@Auth GroupEntity groupEntity, Group group) throws InvalidInputException {
        var groupKey = groupEntity.getGroupPublicParams();
        Group.Builder newGroupBuilder = group.toBuilder();
        List<org.whispersystems.textsecuregcm.proto.Member> memberList = new ArrayList<>();
        int i = 0;
        for (Member m : group.getMembersList()) {
            ProfileKeyCredentialPresentation presentation = new ProfileKeyCredentialPresentation(m.getPresentation().toByteArray());
            UuidCiphertext uuidCiphertext = presentation.getUuidCiphertext();
            ProfileKeyCiphertext profileKeyCiphertext = presentation.getProfileKeyCiphertext();
            Member newMember = m.toBuilder()
                    .setUserId(ByteString.copyFrom(uuidCiphertext.serialize()))
                    .setProfileKey(ByteString.copyFrom(profileKeyCiphertext.serialize()))
                    .build();
            memberList.add(newMember);
            newGroupBuilder.setMembers(i, newMember);
            i++;
        }
        Group newGroup = newGroupBuilder.build();
        for (Member m : newGroup.getMembersList()) {
            System.out.println("group.members.role:" + m.getRole());
            System.out.println("group.members.presentation:" + m.getPresentation().toString());
            System.out.println("group.members.userid:" + m.getUserId());
            System.out.println("group.members.profileKey:" + m.getProfileKey());
        }
        try (Jedis jedis = cacheClient.getWriteResource()) {
            jedis.hset(GROUP_REDIS_KEY.getBytes(), groupKey.serialize(), newGroup.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return group;
    }

}
