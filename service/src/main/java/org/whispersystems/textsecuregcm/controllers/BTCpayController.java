package org.whispersystems.textsecuregcm.controllers;

import com.alibaba.fastjson.JSON;
import io.dropwizard.auth.Auth;
import org.apache.ibatis.session.SqlSession;
import org.whispersystems.textsecuregcm.mybatis.entity.BaseModel;
import org.whispersystems.textsecuregcm.mybatis.mapper.AccountCoinBalanceMapper;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.util.HttpsUtils;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/v1/btcpay")
public class BTCpayController {

    public static final String BTCPAY_URL = "https://btcpay.lxrtalk.com";
    public static final String GET_STORE = "/api/v1/stores";
    public static final String TOKEN = "Authorization";
    public static final String TOKEN_VALUE = "token a56fb37c559c6c0d45b31e79ebf11100792f56ea";
    public static final String BTCPAY_TABLE_NAME = "accounts_invoices";

    @GET
    @Path("/stores")
    public Object stores() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(TOKEN, TOKEN_VALUE);
        var ret = HttpsUtils.Get(BTCPAY_URL + GET_STORE, headers);
        return JSON.parse(ret);
    }

    @POST
    @Path("/stores/{storeId}/invoices")
    public Map<String, String> createInvoice(@PathParam(value = "storeId") String storeId, @Auth Account account, @Context SqlSession session) {
        String uuid = account.getUuid().toString();
        String orderid = UUID.randomUUID().toString();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(TOKEN, TOKEN_VALUE);
        //第一步生成btcpay订单
        var ret = HttpsUtils.Get(BTCPAY_URL + "/api/v1/stores/" + storeId + "/invoices", headers);
        AccountCoinBalanceMapper mapper = session.getMapper(AccountCoinBalanceMapper.class);
        Map<String, String> map = JSON.parseObject(ret, Map.class);
        map.put("uuid", uuid);
        map.put("orderid", orderid);
        map.put("tableName", BTCPAY_TABLE_NAME);
        //第二步生成signal平台订单
        mapper.insertBase(map);
        return map;
    }

    @GET
    @Path("/invoices/{invoiceId}")
    public List<Map> getInvoice(@PathParam(value = "invoiceId") String invoiceId, @Auth Account account, @Context SqlSession session) {
        String uuid = account.getUuid().toString();
        AccountCoinBalanceMapper mapper = session.getMapper(AccountCoinBalanceMapper.class);
        Map<String, String> map = new HashMap<>();
        map.put("uuid", uuid);
        map.put("id", invoiceId);
        map.put("tableName", BTCPAY_TABLE_NAME);
        BaseModel baseModel = new BaseModel();
        baseModel.setPageNo(1);
        baseModel.setPageSize(100);
        List<Map> list = mapper.selectBaseList(map, baseModel);
        return list;
    }
}
