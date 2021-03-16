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
package org.whispersystems.textsecuregcm.auth;

import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.signal.zkgroup.auth.ServerZkAuthOperations;
import org.whispersystems.textsecuregcm.storage.GroupEntity;
import org.whispersystems.textsecuregcm.storage.GroupsManager;

import java.util.Optional;

/**
 * lxr 20210316
 */
public class GroupAuthenticator extends BaseGroupAuthenticator implements Authenticator<BasicCredentials, GroupEntity> {

  public GroupAuthenticator(GroupsManager accountsManager, ServerZkAuthOperations zkAuthOperations) {
    super(accountsManager,zkAuthOperations);
  }

  @Override
  public Optional<GroupEntity> authenticate(BasicCredentials basicCredentials) {
    return super.authenticate(basicCredentials, true);
  }

}
