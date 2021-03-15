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
import com.google.protobuf.InvalidProtocolBufferException;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.proto.Group;
import org.whispersystems.textsecuregcm.proto.GroupAttributeBlob;
import org.whispersystems.textsecuregcm.redis.ReplicatedJedisPool;
import org.whispersystems.textsecuregcm.storage.Account;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
  public Group saveGroup(HttpServletRequest request, Group group
  ) {
    var groupKey=group.getPublicKey();
    cacheClient.getWriteResource().hset(GROUP_REDIS_KEY.getBytes(), groupKey.toByteArray(), group.toByteArray());
    String auth=request.getHeader("Authorization");
    System.out.println("saveGroup auth:"+auth);
//    cacheClient.getWriteResource().sadd((GROUP_ADMIN_REDIS_KEY + account.getUuid()).getBytes(),
//            groupKey.toByteArray());
    return group;
  }

  @Timed
  @GET
  @Consumes("application/x-protobuf")
  @Produces("application/x-protobuf")
  public GroupAttributeBlob getGroup(@Auth Account account) {
    com.google.protobuf.ByteString groupKey = null;
    byte[] groupByte = cacheClient.getWriteResource().hget(GROUP_REDIS_KEY.getBytes(), groupKey.toByteArray());
    Group group = null;
    try {
      group = Group.parseFrom(groupByte);
      return GroupAttributeBlob.newBuilder().setAvatar(group.getAvatarBytes())
              .setTitle(group.getTitle().toString()).setDisappearingMessagesDuration(3).build();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Timed
  @PATCH
  @Consumes("application/x-protobuf")
  @Produces("application/x-protobuf")
  public Group patchGroup(@Auth Account account, Group group) {
    cacheClient.getWriteResource().hset(GROUP_REDIS_KEY.getBytes(), group.getPublicKey().toByteArray(), group.toByteArray());
    return group;
  }

}
