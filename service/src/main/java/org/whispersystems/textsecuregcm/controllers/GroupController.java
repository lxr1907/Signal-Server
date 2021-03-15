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
import org.whispersystems.textsecuregcm.redis.ReplicatedJedisPool;
import org.whispersystems.textsecuregcm.storage.Account;

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
  public Group saveGroup(@Auth Account account, Group group
  ) {
    cacheClient.getWriteResource().hset(GROUP_REDIS_KEY.getBytes(), group.getTitle().toByteArray(), group.toByteArray());
    cacheClient.getWriteResource().sadd((GROUP_ADMIN_REDIS_KEY + account.getUuid()).getBytes(),
        group.getTitle().toByteArray());
    return group;
  }

  @Timed
  @GET
  @Consumes("application/x-protobuf")
  @Produces("application/x-protobuf")
  public Group getGroup(@Auth Account account) {
    Set<Group> groupSet = new HashSet<>();
    Set<byte[]> groups = cacheClient.getWriteResource().smembers((GROUP_ADMIN_REDIS_KEY + account.getUuid()).getBytes());
    for (byte[] groupTitleByte : groups) {
      try {
        byte[] groupByte = cacheClient.getWriteResource().hget(GROUP_REDIS_KEY.getBytes(), groupTitleByte);
        Group group = Group.parseFrom(groupByte);
        groupSet.add(group);
      } catch (InvalidProtocolBufferException e) {
        e.printStackTrace();
      }
    }
    if (groupSet != null && groupSet.size() > 0) {
      List<Group> list = new ArrayList(groupSet);
      return list.get(0);
    }
    return null;
  }

  @Timed
  @PATCH
  @Consumes("application/x-protobuf")
  @Produces("application/x-protobuf")
  public Group patchGroup(@Auth Account account, Group group) {
    cacheClient.getWriteResource().hset(GROUP_REDIS_KEY.getBytes(), group.getTitle().toByteArray(), group.toByteArray());
    return group;
  }

}
