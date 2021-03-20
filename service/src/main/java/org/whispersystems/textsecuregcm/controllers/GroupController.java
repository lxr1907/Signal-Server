
package org.whispersystems.textsecuregcm.controllers;

import com.codahale.metrics.annotation.Timed;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.dropwizard.auth.Auth;
import org.signal.zkgroup.*;
import org.signal.zkgroup.auth.AuthCredentialPresentation;
import org.signal.zkgroup.groups.*;
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
    private ServerPublicParams serverPublicParams;

    public GroupController(ReplicatedJedisPool cacheClient, ServerSecretParams serverSecretParams, ServerPublicParams serverPublicParams) {
        this.cacheClient = cacheClient;
        this.serverSecretParams = serverSecretParams;
        this.serverPublicParams = serverPublicParams;
    }

    public static final String GROUP_REDIS_KEY = "group_";
    public static final String GROUP_CHANGE_REDIS_KEY = "group_change_";
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
            System.out.println("group.getDisappearingMessagesTimer:" + group.getDisappearingMessagesTimer());
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
                System.out.println("group.getDisappearingMessagesTimer:" + group.getDisappearingMessagesTimer());
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
    @GET
    @Consumes("application/x-protobuf")
    @Produces("application/x-protobuf")
    @Path("/logs/{index}")
    public GroupChange getGroupLogs(@Auth GroupEntity groupEntity, @PathParam("index") int index) {
        System.out.println("getGroupLogs index:" + index);
        System.out.println("getGroupLogs:" + groupEntity);
        System.out.println("getGroupLogs:" + groupEntity);
        var groupKey = groupEntity.getGroupPublicParams();
        System.out.println("getGroupLogs.redisKey:" + getGroupLogsKey(groupKey));
        try (Jedis jedis = cacheClient.getReadResource()) {
            byte[] groupByte = jedis.lindex(getGroupLogsKey(groupKey).getBytes(), index);
            GroupChange groupChange = null;
            try {
                if (groupByte == null || groupByte.length == 0) {
                    return null;
                }
                groupChange = GroupChange.parseFrom(groupByte);
                System.out.println("groupChange.getActions:" + groupChange.getActions());
                System.out.println("groupChange.getServerSignature:" + groupChange.getServerSignature());
                return groupChange;
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

    private String getGroupLogsKey(GroupPublicParams groupPublicParams) {
        return GROUP_CHANGE_REDIS_KEY + new String(groupPublicParams.serialize());
    }

    @Timed
    @PATCH
    @Consumes("application/x-protobuf")
    @Produces("application/x-protobuf")
    public GroupChange patchGroup(@Auth GroupEntity groupEntity, GroupChange.Actions inputActions) throws InvalidProtocolBufferException {
        Group group = getGroup(groupEntity);
        System.out.println("groupChange.actions:" + inputActions);
        System.out.println("group.getDisappearingMessagesTimer:" + group.getDisappearingMessagesTimer());
        GroupChange.Actions.Builder actionsBuilder = inputActions.toBuilder();
        actionsBuilder.setSourceUuid(ByteString.copyFrom(groupEntity.getAuthCredentialPresentation().getUuidCiphertext().serialize()));
        GroupChange.Actions actions = actionsBuilder.build();
        NotarySignature notarySignature = serverSecretParams.sign(actions.toByteArray());
        ByteString signature = ByteString.copyFrom(notarySignature.serialize());
        GroupChange.Builder newGroupChange = GroupChange.newBuilder();
        GroupChange groupChange = newGroupChange
                .setActions(ByteString.copyFrom(actions.toByteArray()))
                .setChangeEpoch(Util.currentDaysSinceEpoch())
                .setServerSignature(signature).build();

        System.out.println("end groupChange.getActions:" + newGroupChange.getActions());
        System.out.println("end groupChange.getChangeEpoch:" + newGroupChange.getChangeEpoch());
        System.out.println("end groupChange.getServerSignature:" + newGroupChange.getServerSignature());

        Group.Builder builder = group.toBuilder();
        //取出所有变更项，同步到group对象中，再存入redis。
        if (actions.getModifyTitle() != null && actions.getModifyTitle().getTitle() != null) {
            //实际取出群名称，写入redis
            ByteString title = actions.getModifyTitle().getTitle();
            System.out.println("groupChange.getModifyTitle:" + title);
            builder.setTitle(title);
        }

        var addMemberActionsList = actions.getAddMembersList();
        if (addMemberActionsList != null && addMemberActionsList.size() != 0) {
            System.out.println("groupChange.addMemberActionsList.size:" + addMemberActionsList.size());
            for (var addMemberAction : addMemberActionsList) {
                var member = addMemberAction.getAdded();
                builder.addMembers(member);
            }
        }

        var peddingMemberActionList=actions.getAddPendingMembersList();
        if (peddingMemberActionList != null && peddingMemberActionList.size() != 0) {
            System.out.println("groupChange.peddingMemberActionList.size:" + peddingMemberActionList.size());
            for (var peddingMemberAction : peddingMemberActionList) {
                var member = peddingMemberAction.getAdded();
                builder.addPendingMembers(member);
            }
        }
        var deleteMembersActionList=actions.getDeleteMembersList();
        if (deleteMembersActionList != null && deleteMembersActionList.size() != 0) {
            System.out.println("groupChange.deleteMembersActionList.size:" + deleteMembersActionList.size());
            for (var deleteMemberAction : deleteMembersActionList) {
                var deletedUserId = deleteMemberAction.getDeletedUserId();
                int index=0;
               for(int i=0;i< builder.getMembersList().size();i++){
                   var member=builder.getMembersList().get(i);
                   if(member.getUserId().equals(deletedUserId)){
                       index=i;
                       break;
                   }
               }
                builder.removeMembers(index);
            }
        }

        var modifyAvatar=actions.getModifyAvatar();
        if(modifyAvatar!=null&&modifyAvatar.getAvatar()!=null){
            builder.setAvatar(modifyAvatar.getAvatar());
        }

        Group groupNew = builder.build();
        var groupKey = groupEntity.getGroupPublicParams();
        try (Jedis jedis = cacheClient.getWriteResource()) {
            jedis.hset(GROUP_REDIS_KEY.getBytes(), groupNew.getPublicKey().toByteArray(), groupNew.toByteArray());
            System.out.println("getGroupLogs.redisKey:" + getGroupLogsKey(groupKey));
            jedis.lpush(getGroupLogsKey(groupKey).getBytes(), groupChange.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // serverPublicParams.verifySignature(newGroupChange.getActions().toByteArray(), notarySignature);
        return groupChange;
    }

}
