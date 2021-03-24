package org.whispersystems.textsecuregcm.mybatis.mapper;

import org.apache.ibatis.annotations.Param;
import org.whispersystems.textsecuregcm.storage.AccountCoinBalance;

import java.util.Map;

public interface AccountCoinBalanceMapper {
    AccountCoinBalance findByUUid(@Param("uuid") String uuid);

    void insertBase(@Param("map") Map map);

    void updateBaseByPrimaryKey(@Param("map") Map map);
}
