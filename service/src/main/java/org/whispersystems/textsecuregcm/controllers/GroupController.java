
package org.whispersystems.textsecuregcm.controllers;

import com.codahale.metrics.annotation.Timed;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.dropwizard.auth.Auth;
import org.signal.zkgroup.InvalidInputException;
import org.signal.zkgroup.NotarySignature;
import org.signal.zkgroup.ServerSecretParams;
import org.signal.zkgroup.auth.AuthCredentialPresentation;
import org.signal.zkgroup.groups.ClientZkGroupCipher;
import org.signal.zkgroup.groups.GroupSecretParams;
import org.signal.zkgroup.groups.ProfileKeyCiphertext;
import org.signal.zkgroup.groups.UuidCiphertext;
import org.signal.zkgroup.profiles.ProfileKeyCredentialPresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.proto.*;
import org.whispersystems.textsecuregcm.redis.ReplicatedJedisPool;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.GroupEntity;
import org.whispersystems.textsecuregcm.util.Util;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import java.time.Instant;
import java.util.*;


/**
 * lxr 20210316
 */
@Path("/v1/groups")
public class GroupController {

    private final Logger logger = LoggerFactory.getLogger(GroupController.class);
    protected final ReplicatedJedisPool cacheClient;
    private ServerSecretParams serverSecretParams;

    public GroupController(ReplicatedJedisPool cacheClient, ServerSecretParams serverSecretParams) {
        this.cacheClient = cacheClient;
        this.serverSecretParams = serverSecretParams;
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
                System.out.println("group.members.size:" + group.getMembersList().size());
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
    public GroupChange patchGroup(@Auth GroupEntity groupEntity, GroupChange.Actions actions) {
        Group group = getGroup(groupEntity);
        System.out.println("groupChange.actions:" + actions);
        NotarySignature notarySignature = serverSecretParams.sign(actions.toByteArray());
        ByteString signature = ByteString.copyFrom(notarySignature.serialize());
        GroupChange.Builder newGroupChange = GroupChange.newBuilder();
        newGroupChange
                .setActions(ByteString.copyFrom(actions.toByteArray()))
                .setChangeEpoch(Util.currentDaysSinceEpoch())
                .setServerSignature(signature).build();
        System.out.println("end groupChange.getActions:" + newGroupChange.getActions());
        System.out.println("end groupChange.getChangeEpoch:" + newGroupChange.getChangeEpoch());
        System.out.println("end groupChange.getServerSignature:" + newGroupChange.getServerSignature());

        ByteString title = actions.getModifyTitle().getTitle();
        System.out.println("groupChange.getModifyTitle:" + title);
        Group.Builder builder = group.toBuilder();
        builder.setTitle(title);
        Group groupNew = builder.build();
        try (Jedis jedis = cacheClient.getWriteResource()) {
            jedis.hset(GROUP_REDIS_KEY.getBytes(), groupNew.getPublicKey().toByteArray(), groupNew.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newGroupChange.build();
    }

}
