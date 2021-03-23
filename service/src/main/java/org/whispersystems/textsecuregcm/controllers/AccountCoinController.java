
package org.whispersystems.textsecuregcm.controllers;

import com.codahale.metrics.annotation.Timed;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.mybatis.mapper.AccountCoinBalanceMapper;
import org.whispersystems.textsecuregcm.redis.ReplicatedJedisPool;
import org.whispersystems.textsecuregcm.storage.AccountCoinBalance;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;


/**
 * lxr 20210323
 */
@Path("/v1/accounts/coin")
public class AccountCoinController {

    private final Logger logger = LoggerFactory.getLogger(AccountCoinController.class);
    protected final ReplicatedJedisPool cacheClient;

    public AccountCoinController(ReplicatedJedisPool cacheClient) {
        this.cacheClient = cacheClient;
    }

    @Timed
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{uuid}")
    public AccountCoinBalance getAccountCoin(@PathParam("uuid") String uuid, @Context SqlSession session) {//@Auth Account account
        AccountCoinBalanceMapper users = session.getMapper(AccountCoinBalanceMapper.class);
        return users.findByUUid(uuid);
    }

}
