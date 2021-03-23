package org.whispersystems.textsecuregcm.mappers;

import org.apache.ibatis.annotations.Param;
import org.whispersystems.textsecuregcm.storage.AccountCoinBalance;

public interface AccountCoinBalanceMapper {
    AccountCoinBalance findByUUid(@Param("uuid") String uuid);

    void addAccountCoin(@Param("accountCoinBalance") AccountCoinBalance accountCoinBalance);
}
