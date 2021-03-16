package org.whispersystems.textsecuregcm.storage.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.signal.zkgroup.InvalidInputException;
import org.signal.zkgroup.groups.GroupPublicParams;
import org.whispersystems.textsecuregcm.storage.GroupEntity;
import org.whispersystems.textsecuregcm.storage.Groups;
import org.whispersystems.textsecuregcm.util.SystemMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class GroupRowMapper implements RowMapper<GroupEntity> {

  private static ObjectMapper mapper = SystemMapper.getMapper();

  @Override
  public GroupEntity map(ResultSet resultSet, StatementContext ctx) throws SQLException {
    try {
      GroupEntity Group = mapper.readValue(resultSet.getString(Groups.DATA), GroupEntity.class);
      Group.setGroupPublicParams(new GroupPublicParams(resultSet.getString(Groups.PUBLIC_KEY).getBytes(StandardCharsets.UTF_8)));
      Group.setUuid(UUID.fromString(resultSet.getString(Groups.UID)));
      return Group;
    } catch (IOException | InvalidInputException e) {
      throw new SQLException(e);
    }
  }
}