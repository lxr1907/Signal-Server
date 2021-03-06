
package org.whispersystems.textsecuregcm.controllers;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.mybatis.entity.BaseModel;
import org.whispersystems.textsecuregcm.mybatis.mapper.AccountCoinBalanceMapper;
import org.whispersystems.textsecuregcm.redis.ReplicatedJedisPool;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.AccountCoinBalance;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * lxr 20210323
 */
@Path("/v1/coin")
public class AccountCoinController {

    private final Logger logger = LoggerFactory.getLogger(AccountCoinController.class);
    protected final ReplicatedJedisPool cacheClient;

    public AccountCoinController(ReplicatedJedisPool cacheClient) {
        this.cacheClient = cacheClient;
    }

    @Timed
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String,Object>  getAccountCoin(@Auth Account account, @Context SqlSession session) {
        Map<String,Object> ret=new HashMap<>();
        String uuid = account.getUuid().toString();
        AccountCoinBalanceMapper users = session.getMapper(AccountCoinBalanceMapper.class);
        AccountCoinBalance sacBalance = users.findByUUid(uuid);
        if (sacBalance == null) {
            Map<String, String> map = new HashMap<>();
            map.put("uuid", uuid);
            map.put("coin_name", "SAC");
            map.put("tableName", "accounts_coin_balance");
            users.insertBase(map);
        }
        List<AccountCoinBalance> list = new ArrayList<>();
         sacBalance= users.findByUUid(uuid);
        sacBalance.setPercent("100.00");
        sacBalance.setSymbol("ethusdt");
        sacBalance.setPeriod("1day");
        list.add(sacBalance);
        list.add(generateAccountCoinBalance("BTC"));
        list.add(generateAccountCoinBalance("DASH"));
        list.add(generateAccountCoinBalance("ETH"));
        list.add(generateAccountCoinBalance("LTC"));
        ret.put("totalBalance",100);
        ret.put("coin",list);
        return ret;
    }

    private AccountCoinBalance generateAccountCoinBalance(String coin_name) {
        AccountCoinBalance accountCoinBalance = new AccountCoinBalance();
        accountCoinBalance.setCoin_name(coin_name);
        accountCoinBalance.setBalance("0");
        accountCoinBalance.setPercent("0.00");
        accountCoinBalance.setSymbol(coin_name.toLowerCase() + "usdt");
        accountCoinBalance.setPeriod("1day");
        return accountCoinBalance;
    }
    @Timed
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{uuid}")
    public AccountCoinBalance getAccountCoin(@PathParam("uuid") String uuid, @Context SqlSession session) {
        AccountCoinBalanceMapper users = session.getMapper(AccountCoinBalanceMapper.class);
        AccountCoinBalance ret = users.findByUUid(uuid);
        if (ret == null) {
            Map<String, String> map = new HashMap<>();
            map.put("uuid", uuid);
            map.put("coin_name", "SAC");
            map.put("tableName", "accounts_coin_balance");
            users.insertBase(map);
        }
        ret = users.findByUUid(uuid);
        return ret;
    }

    @Timed
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/coinList")
    public List<Map> getCoinList(@Context SqlSession session) {
        AccountCoinBalanceMapper users = session.getMapper(AccountCoinBalanceMapper.class);
        Map<String, String> map = new HashMap<>();
        map.put("tableName", "coin_manage");
        BaseModel baseModel = new BaseModel();
        baseModel.setPageNo(1);
        baseModel.setPageSize(100);
        List<Map> ret = users.selectBaseList(map, baseModel);
        return ret;
    }

}
