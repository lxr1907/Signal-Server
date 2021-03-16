/*
 * Copyright (C) 2013-2018 Signal
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.auth.AmbiguousIdentifier;
import org.whispersystems.textsecuregcm.redis.ReplicatedJedisPool;
import org.whispersystems.textsecuregcm.util.Constants;
import org.whispersystems.textsecuregcm.util.SystemMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * lxr 20210316
 */
public class GroupsManager {

    private static final MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate(Constants.METRICS_NAME);
    private static final Timer createTimer = metricRegistry.timer(name(GroupsManager.class, "create"));
    private static final Timer updateTimer = metricRegistry.timer(name(GroupsManager.class, "update"));
    private static final Timer getByNumberTimer = metricRegistry.timer(name(GroupsManager.class, "getByNumber"));
    private static final Timer getByUuidTimer = metricRegistry.timer(name(GroupsManager.class, "getByUuid"));

    private static final Timer redisSetTimer = metricRegistry.timer(name(GroupsManager.class, "redisSet"));
    private static final Timer redisNumberGetTimer = metricRegistry.timer(name(GroupsManager.class, "redisNumberGet"));
    private static final Timer redisUuidGetTimer = metricRegistry.timer(name(GroupsManager.class, "redisUuidGet"));

    private final Logger logger = LoggerFactory.getLogger(GroupsManager.class);

    private final Groups groups;
    private final ReplicatedJedisPool cacheClient;
    private final ObjectMapper mapper;

    public GroupsManager(Groups groups, ReplicatedJedisPool cacheClient) {
        this.groups = groups;
        this.cacheClient = cacheClient;
        this.mapper = SystemMapper.getMapper();
    }

    public boolean create(GroupEntity group) {
        try (Timer.Context ignored = createTimer.time()) {
            boolean freshUser = databaseCreate(group);
            redisSet(group);
            return freshUser;
        }
    }

    public void update(GroupEntity group) {
        try (Timer.Context ignored = updateTimer.time()) {
            redisSet(group);
            databaseUpdate(group);
        }
    }

    public Optional<GroupEntity> get(AmbiguousIdentifier identifier) {
        if (identifier.hasNumber()) return get(identifier.getNumber());
        else if (identifier.hasUuid()) return get(identifier.getUuid());
        else throw new AssertionError();
    }

    public Optional<GroupEntity> get(String number) {
        try (Timer.Context ignored = getByNumberTimer.time()) {
            Optional<GroupEntity> group = redisGet(number);

            if (!group.isPresent()) {
                group = databaseGet(number);
                group.ifPresent(value -> redisSet(value));
            }

            return group;
        }
    }

    public Optional<GroupEntity> get(UUID uuid) {
        try (Timer.Context ignored = getByUuidTimer.time()) {
            Optional<GroupEntity> group = redisGet(uuid);

            if (!group.isPresent()) {
                group = databaseGet(uuid);
                group.ifPresent(value -> redisSet(value));
            }

            return group;
        }
    }


    public List<GroupEntity> getAllFrom(int length) {
        return groups.getAllFrom(length);
    }

    public List<GroupEntity> getAllFrom(UUID uuid, int length) {
        return groups.getAllFrom(uuid, length);
    }


    private String getGroupMapKey(String number) {
        return "GroupMap::" + number;
    }

    private String getGroupEntityKey(UUID uuid) {
        return "Group3::" + uuid.toString();
    }

    private void redisSet(GroupEntity group) {
        try (Jedis jedis = cacheClient.getWriteResource();
             Timer.Context ignored = redisSetTimer.time()) {
            jedis.set(getGroupMapKey(group.getGroupPublicParams().toString()), group.getUuid().toString());
            jedis.set(getGroupEntityKey(group.getUuid()), mapper.writeValueAsString(group));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Optional<GroupEntity> redisGet(String number) {
        try (Jedis jedis = cacheClient.getReadResource();
             Timer.Context ignored = redisNumberGetTimer.time()) {
            String uuid = jedis.get(getGroupMapKey(number));

            if (uuid != null) return redisGet(jedis, UUID.fromString(uuid));
            else return Optional.empty();
        } catch (IllegalArgumentException e) {
            logger.warn("Deserialization error", e);
            return Optional.empty();
        } catch (JedisException e) {
            logger.warn("Redis failure", e);
            return Optional.empty();
        }
    }

    private Optional<GroupEntity> redisGet(UUID uuid) {
        try (Jedis jedis = cacheClient.getReadResource()) {
            return redisGet(jedis, uuid);
        }
    }

    private Optional<GroupEntity> redisGet(Jedis jedis, UUID uuid) {
        try (Timer.Context ignored = redisUuidGetTimer.time()) {
            String json = jedis.get(getGroupEntityKey(uuid));

            if (json != null) {
                GroupEntity group = mapper.readValue(json, GroupEntity.class);
                group.setUuid(uuid);

                return Optional.of(group);
            }

            return Optional.empty();
        } catch (IOException e) {
            logger.warn("Deserialization error", e);
            return Optional.empty();
        } catch (JedisException e) {
            logger.warn("Redis failure", e);
            return Optional.empty();
        }
    }

    private Optional<GroupEntity> databaseGet(String number) {
        return groups.get(number);
    }

    private Optional<GroupEntity> databaseGet(UUID uuid) {
        return groups.get(uuid);
    }

    private boolean databaseCreate(GroupEntity group) {
        return groups.create(group);
    }

    private void databaseUpdate(GroupEntity group) {
        groups.update(group);
    }
}
