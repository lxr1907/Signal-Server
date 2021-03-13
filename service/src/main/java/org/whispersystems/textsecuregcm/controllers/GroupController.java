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


@Path("/v1/groups")
public class GroupController {

  private final Logger logger = LoggerFactory.getLogger(GroupController.class);
  protected final ReplicatedJedisPool cacheClient;

  public GroupController(ReplicatedJedisPool cacheClient) {
    this.cacheClient = cacheClient;
  }

  public static final String GROUP_REDIS_KEY = "group_";

  @Timed
  @PUT
  @Consumes("application/x-protobuf")
  @Produces("application/x-protobuf")
  public Group saveGroup(@Auth Account account, Group body
  ) {
    cacheClient.getWriteResource().hset(GROUP_REDIS_KEY.getBytes(), body.getTitle().toByteArray(), body.toByteArray());
    return body;
  }

  @Timed
  @GET
  @Consumes("application/x-protobuf")
  @Produces("application/x-protobuf")
  public Group getGroup(@Auth Account account, Group body) {
    Group bodyRet = null;
    byte[] byteGroup = cacheClient.getWriteResource().hget(GROUP_REDIS_KEY.getBytes(), body.getTitle().toByteArray());
    try {
      bodyRet = Group.parseFrom(byteGroup);
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }

    return bodyRet;
  }

  @Timed
  @PATCH
  @Consumes("application/x-protobuf")
  @Produces("application/x-protobuf")
  public Group patchGroup(@Auth Account account, Group body) {
    cacheClient.getWriteResource().hset(GROUP_REDIS_KEY.getBytes(), body.getTitle().toByteArray(), body.toByteArray());
    return body;
  }

}
