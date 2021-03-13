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
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.auth.AmbiguousIdentifier;
import org.whispersystems.textsecuregcm.storage.Account;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;


@Path("/v1/groups")
public class GroupController {

  private final Logger logger = LoggerFactory.getLogger(GroupController.class);

  public GroupController() {
  }

  @Timed
  @PUT
  public String saveGroup(@Auth Optional<Account> source, String body
  ) {
    System.out.println(body);
    logger.warn(body);
    return body;
  }

  @Timed
  @GET
  public String getGroup(@Auth Account account, String body) {
    System.out.println(body);
    logger.warn(body);
    return body;
  }

  @Timed
  @PATCH
  public String patchGroup(@Auth Account account, String body) {
    System.out.println(body);
    logger.warn(body);
    return body;
  }

}
