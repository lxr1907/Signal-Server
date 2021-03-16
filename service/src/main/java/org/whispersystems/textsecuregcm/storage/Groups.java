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
package org.whispersystems.textsecuregcm.storage;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import org.whispersystems.textsecuregcm.storage.mappers.GroupRowMapper;
import org.whispersystems.textsecuregcm.util.Constants;
import org.whispersystems.textsecuregcm.util.SystemMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.codahale.metrics.MetricRegistry.name;

public class Groups {

  public static final String ID     = "id";
  public static final String UID    = "uuid";
  public static final String PUBLIC_KEY = "public_key";
  public static final String DATA   = "data";

  private static final ObjectMapper mapper = SystemMapper.getMapper();

  private final MetricRegistry metricRegistry        = SharedMetricRegistries.getOrCreate(Constants.METRICS_NAME);
  private final Timer          createTimer           = metricRegistry.timer(name(Groups.class, "create"          ));
  private final Timer          updateTimer           = metricRegistry.timer(name(Groups.class, "update"          ));
  private final Timer          getByNumberTimer      = metricRegistry.timer(name(Groups.class, "getByNumber"     ));
  private final Timer          getByUuidTimer        = metricRegistry.timer(name(Groups.class, "getByUuid"       ));
  private final Timer          getAllFromTimer       = metricRegistry.timer(name(Groups.class, "getAllFrom"      ));
  private final Timer          getAllFromOffsetTimer = metricRegistry.timer(name(Groups.class, "getAllFromOffset"));
  private final Timer          vacuumTimer           = metricRegistry.timer(name(Groups.class, "vacuum"          ));

  private final FaultTolerantDatabase database;

  public Groups(FaultTolerantDatabase database) {
    this.database = database;
    this.database.getDatabase().registerRowMapper(new GroupRowMapper());
  }

  public boolean create(GroupEntity Group) {
    return database.with(jdbi -> jdbi.inTransaction(TransactionIsolationLevel.SERIALIZABLE, handle -> {
      try (Timer.Context ignored = createTimer.time()) {
        UUID uuid = handle.createQuery("INSERT INTO groups (" + PUBLIC_KEY + ", " + UID + ", " + DATA + ") VALUES (:public_key, :uuid, CAST(:data AS json)) ON CONFLICT(number) DO UPDATE SET data = EXCLUDED.data RETURNING uuid")
                          .bind("public_key", Group.getGroupPublicParams())
                          .bind("uuid", Group.getUuid())
                          .bind("data", mapper.writeValueAsString(Group))
                          .mapTo(UUID.class)
                          .findOnly();

        boolean isNew = uuid.equals(Group.getUuid());
        Group.setUuid(uuid);
        return isNew;
      } catch (JsonProcessingException e) {
        throw new IllegalArgumentException(e);
      }
    }));
  }

  public void update(GroupEntity Group) {
    database.use(jdbi -> jdbi.useHandle(handle -> {
      try (Timer.Context ignored = updateTimer.time()) {
        handle.createUpdate("UPDATE groups SET " + DATA + " = CAST(:data AS json) WHERE " + UID + " = :uuid")
              .bind("uuid", Group.getUuid())
              .bind("data", mapper.writeValueAsString(Group))
              .execute();
      } catch (JsonProcessingException e) {
        throw new IllegalArgumentException(e);
      }
    }));
  }

  public Optional<GroupEntity> get(String public_key) {
    return database.with(jdbi -> jdbi.withHandle(handle -> {
      try (Timer.Context ignored = getByNumberTimer.time()) {
        return handle.createQuery("SELECT * FROM groups WHERE " + PUBLIC_KEY + " = :public_key")
                     .bind("public_key", public_key)
                     .mapTo(GroupEntity.class)
                     .findFirst();
      }
    }));
  }

  public Optional<GroupEntity> get(UUID uuid) {
    return database.with(jdbi -> jdbi.withHandle(handle -> {
      try (Timer.Context ignored = getByUuidTimer.time()) {
        return handle.createQuery("SELECT * FROM groups WHERE " + UID + " = :uuid")
                     .bind("uuid", uuid)
                     .mapTo(GroupEntity.class)
                     .findFirst();
      }
    }));
  }

  public List<GroupEntity> getAllFrom(UUID from, int length) {
    return database.with(jdbi -> jdbi.withHandle(handle -> {
      try (Timer.Context ignored = getAllFromOffsetTimer.time()) {
        return handle.createQuery("SELECT * FROM groups WHERE " + UID + " > :from ORDER BY " + UID + " LIMIT :limit")
                     .bind("from", from)
                     .bind("limit", length)
                     .mapTo(GroupEntity.class)
                     .list();
      }
    }));
  }

  public List<GroupEntity> getAllFrom(int length) {
    return database.with(jdbi -> jdbi.withHandle(handle -> {
      try (Timer.Context ignored = getAllFromTimer.time()) {
        return handle.createQuery("SELECT * FROM groups ORDER BY " + UID + " LIMIT :limit")
                     .bind("limit", length)
                     .mapTo(GroupEntity.class)
                     .list();
      }
    }));
  }

  public void vacuum() {
    database.use(jdbi -> jdbi.useHandle(handle -> {
      try (Timer.Context ignored = vacuumTimer.time()) {
        handle.execute("VACUUM Groups");
      }
    }));
  }

}
