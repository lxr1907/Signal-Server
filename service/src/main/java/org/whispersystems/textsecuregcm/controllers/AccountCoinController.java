
package org.whispersystems.textsecuregcm.controllers;

import com.codahale.metrics.annotation.Timed;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.dropwizard.auth.Auth;
import org.apache.ibatis.session.SqlSession;
import org.signal.zkgroup.InvalidInputException;
import org.signal.zkgroup.NotarySignature;
import org.signal.zkgroup.ServerPublicParams;
import org.signal.zkgroup.ServerSecretParams;
import org.signal.zkgroup.groups.ProfileKeyCiphertext;
import org.signal.zkgroup.groups.UuidCiphertext;
import org.signal.zkgroup.profiles.ProfileKeyCredentialPresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.mappers.AccountCoinBalanceMapper;
import org.whispersystems.textsecuregcm.proto.Group;
import org.whispersystems.textsecuregcm.proto.GroupChange;
import org.whispersystems.textsecuregcm.proto.GroupChanges;
import org.whispersystems.textsecuregcm.proto.Member;
import org.whispersystems.textsecuregcm.redis.ReplicatedJedisPool;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.AccountCoinBalance;
import org.whispersystems.textsecuregcm.storage.GroupEntity;
import redis.clients.jedis.Jedis;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;


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
