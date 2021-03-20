
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
       var groupChangesBuilder= GroupChanges.newBuilder();
       var action= GroupChange.Actions.newBuilder().setSourceUuid(ByteString.copyFrom(groupEntity.getAuthCredentialPresentation().getUuidCiphertext().serialize())).setModifyTitle( GroupChange.Actions.ModifyTitleAction.newBuilder()
            .setTitle(group.getTitle()).build());
        groupChangesBuilder.addGroupChanges(GroupChanges.GroupChangeState.newBuilder()
            .setGroupState(newGroup)
            .setGroupChange(GroupChange.newBuilder().setActions(action.build().toByteString()).build()
            ).build());
        ;
        try (Jedis jedis = cacheClient.getWriteResource()) {
            jedis.hset(GROUP_REDIS_KEY.getBytes(), groupKey.serialize(), newGroup.toByteArray());
            jedis.hset(GROUP_CHANGE_REDIS_KEY.getBytes(),groupKey.serialize(), groupChangesBuilder.build().toByteArray());
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
    public GroupChanges getGroupLogs(@Auth GroupEntity groupEntity, @PathParam("index") int index) {
        System.out.println("getGroupLogs index:" + index);
        System.out.println("getGroupLogs:" + groupEntity);
        System.out.println("getGroupLogs:" + groupEntity);
        var groupKey = groupEntity.getGroupPublicParams();
        System.out.println("getGroupLogs.redisKey:" + groupKey);
        var groupChangesBuilder=GroupChanges.newBuilder();
        try (Jedis jedis = cacheClient.getReadResource()) {
            byte[] groupChangesByte = jedis.hget(GROUP_CHANGE_REDIS_KEY.getBytes(), groupKey.serialize());
            GroupChanges groupChanges = null;
            try {
                if (groupChangesByte == null || groupChangesByte.length == 0) {
                    System.out.println("error getGroupLogs.groupChange empty !");
                    return groupChangesBuilder.build();
                }
                groupChanges = GroupChanges.parseFrom(groupChangesByte);
                groupChangesBuilder.addGroupChanges(groupChanges.getGroupChanges(index));
                return groupChangesBuilder.build();
            } catch (InvalidProtocolBufferException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        System.out.println("error getGroupLogs.groupChange empty !");
        return groupChangesBuilder.build();
    }
//    private byte[] getGroupLogsKey(GroupPublicParams groupPublicParams) {
//        return byteMergerAll(GROUP_CHANGE_REDIS_KEY.getBytes(),groupPublicParams.serialize());
//    }
    private static byte[] byteMergerAll(byte[]... values) {
        int length_byte = 0;
        for (int i = 0; i < values.length; i++) {
            length_byte += values[i].length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < values.length; i++) {
            byte[] b = values[i];
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }
    @Timed
    @PATCH
    @Consumes("application/x-protobuf")
    @Produces("application/x-protobuf")
    public GroupChange patchGroup(@Auth GroupEntity groupEntity, GroupChange.Actions inputActions) throws InvalidInputException {
        Group group = getGroup(groupEntity);
        System.out.println("groupChange.actions:" + inputActions);
        System.out.println("group.getDisappearingMessagesTimer:" + group.getDisappearingMessagesTimer());
        GroupChange.Actions.Builder actionsBuilder = inputActions.toBuilder();
        actionsBuilder.setSourceUuid(ByteString.copyFrom(groupEntity.getAuthCredentialPresentation().getUuidCiphertext().serialize()));
        for(var addMemberBuilder:actionsBuilder.getAddMembersBuilderList()){
            byte[]presentationByte=addMemberBuilder.getAdded().getPresentation().toByteArray();
            ProfileKeyCredentialPresentation presentation = new ProfileKeyCredentialPresentation(presentationByte);
            UuidCiphertext uuidCiphertext = presentation.getUuidCiphertext();
            ProfileKeyCiphertext profileKeyCiphertext = presentation.getProfileKeyCiphertext();
            Member newMember = addMemberBuilder.getAdded().toBuilder()
                .setUserId(ByteString.copyFrom(uuidCiphertext.serialize()))
                .setProfileKey(ByteString.copyFrom(profileKeyCiphertext.serialize()))
                .build();
            addMemberBuilder.setAdded(newMember).build();
        }

        GroupChange.Actions actions = actionsBuilder.build();
        NotarySignature notarySignature = serverSecretParams.sign(actions.toByteArray());
        ByteString signature = ByteString.copyFrom(notarySignature.serialize());
        GroupChange.Builder newGroupChange = GroupChange.newBuilder();
        GroupChange groupChange = newGroupChange
                .setActions(ByteString.copyFrom(actions.toByteArray()))
                .setChangeEpoch(0)
                .setServerSignature(signature).build();

        System.out.println("end groupChange.getActions:" + newGroupChange.getActions());
        System.out.println("end groupChange.getChangeEpoch:" + newGroupChange.getChangeEpoch());
        System.out.println("end groupChange.getServerSignature:" + newGroupChange.getServerSignature());

        Group.Builder builder = group.toBuilder();
        //版本号加一
        builder.setRevision(group.getRevision()+1);
        //取出所有变更项，同步到group对象中，再存入redis。
        if (actions.getModifyTitle() != null && actions.getModifyTitle().getTitle() != null
            && actions.getModifyTitle().getTitle().size()!=0 ) {
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
                ProfileKeyCredentialPresentation presentation = new ProfileKeyCredentialPresentation(member.getPresentation().toByteArray());
                UuidCiphertext uuidCiphertext = presentation.getUuidCiphertext();
                ProfileKeyCiphertext profileKeyCiphertext = presentation.getProfileKeyCiphertext();
                Member newMember = member.toBuilder()
                    .setUserId(ByteString.copyFrom(uuidCiphertext.serialize()))
                    .setProfileKey(ByteString.copyFrom(profileKeyCiphertext.serialize()))
                    .build();
                builder.addMembers(newMember);
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
               for(int i=0;i< builder.getMembersList().size();i++){
                   var member=builder.getMembersList().get(i);
                   if(member.getUserId().equals(deletedUserId)){
                       builder.removeMembers(i);
                   }
               }
            }
        }

        var modifyAvatar=actions.getModifyAvatar();
        if(modifyAvatar!=null&&modifyAvatar.getAvatar()!=null){
            builder.setAvatar(modifyAvatar.getAvatar());
        }

        var modifyMemberRolesList= actions.getModifyMemberRolesList();
        if (modifyMemberRolesList != null && modifyMemberRolesList.size() != 0) {
            modifyMemberRolesList.forEach(modifyMember -> {
                for (int i = 0; i < builder.getMembersList().size(); i++) {
                    var member = builder.getMembersList().get(i);
                    if (member.getUserId().equals(modifyMember.getUserId())) {
                        var memberNew = member.toBuilder().setRole(modifyMember.getRole()).build();
                        builder.setMembers(i, memberNew);
                    }
                }
            });
        }
        Group groupNew = builder.build();
        var groupKey = groupEntity.getGroupPublicParams();

        //保存同步到redis
        try (Jedis jedis = cacheClient.getWriteResource()) {
            jedis.hset(GROUP_REDIS_KEY.getBytes(), groupNew.getPublicKey().toByteArray(), groupNew.toByteArray());
            byte[] groupChangesByte = jedis.hget(GROUP_CHANGE_REDIS_KEY.getBytes(),groupKey.serialize());
            GroupChanges groupChanges = null;
            try {
                if (groupChangesByte != null && groupChangesByte.length != 0) {
                    groupChanges = GroupChanges.parseFrom(groupChangesByte);
                    var groupChangesNew=groupChanges.toBuilder();
                    groupChangesNew.addGroupChanges(GroupChanges.GroupChangeState.newBuilder()
                        .setGroupChange(groupChange)
                        .setGroupState(groupNew)
                        .build());
                    jedis.hset(GROUP_CHANGE_REDIS_KEY.getBytes(),groupKey.serialize(),groupChangesNew.build().toByteArray());
                }
            } catch (InvalidProtocolBufferException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // serverPublicParams.verifySignature(newGroupChange.getActions().toByteArray(), notarySignature);
        return groupChange;
    }

}
