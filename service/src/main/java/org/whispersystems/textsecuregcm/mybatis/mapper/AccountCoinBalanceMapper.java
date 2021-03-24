package org.whispersystems.textsecuregcm.mybatis.mapper;

import org.apache.ibatis.annotations.Param;
import org.whispersystems.textsecuregcm.mybatis.entity.BaseModel;
import org.whispersystems.textsecuregcm.storage.AccountCoinBalance;

import java.util.List;
import java.util.Map;

public interface AccountCoinBalanceMapper {
    AccountCoinBalance findByUUid(@Param("uuid") String uuid);

    void insertBase(@Param("map") Map map);

    void updateBaseByPrimaryKey(@Param("map") Map map);

    List<Map> selectBaseList(@Param("map") Map map, @Param("baseModel") BaseModel baseModel);

    int selectBaseCount(@Param("map") Map map);
}
