package org.whispersystems.textsecuregcm.controllers;

import com.alibaba.fastjson.JSON;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.apache.ibatis.session.SqlSession;
import org.whispersystems.textsecuregcm.mybatis.entity.BaseModel;
import org.whispersystems.textsecuregcm.mybatis.entity.ResponseEntity;
import org.whispersystems.textsecuregcm.mybatis.mapper.AccountCoinBalanceMapper;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.util.HttpsUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.nio.charset.Charset;
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
    public static Charset DEF_CHARSET = Charset.forName("UTF-8");

    @GET
    @Path("/stores")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity stores() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(TOKEN, TOKEN_VALUE);
        var ret = HttpsUtils.Get(BTCPAY_URL + GET_STORE, headers);
        return new ResponseEntity(JSON.parseArray(ret, Map.class));
    }

    @POST
    @Path("/stores/{storeId}/invoices")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ResponseEntity createInvoice(@PathParam(value = "storeId") String storeId,
                                        @Auth Account account, @Context SqlSession session, @RequestBody Map<String, Object> body) {
        String uuid = account.getUuid().toString();
        //生成订单号
        String orderid = UUID.randomUUID().toString();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("orderId", orderid);
        headers.put(TOKEN, TOKEN_VALUE);
        headers.put("Content-Type", String.format("application/json; charset=%s", DEF_CHARSET.name()));
        String content = "";
        body.put("metadata", metadata);
        content = JSON.toJSONString(body);
        //生成btcpay订单
        var ret = HttpsUtils.JsonPost(BTCPAY_URL + "/api/v1/stores/" + storeId + "/invoices", headers, content);
        AccountCoinBalanceMapper mapper = session.getMapper(AccountCoinBalanceMapper.class);
        Map<String, String> map = JSON.parseObject(ret, Map.class);
        map.put("uuid", uuid);
        map.put("orderid", orderid);
        map.put("tableName", BTCPAY_TABLE_NAME);
        //保存signal平台订单
        mapper.insertBase(map);
        return new ResponseEntity(map);
    }

    @GET
    @Path("/stores/{storeId}/invoices/{invoiceId}/payment-methods")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity getAddress(@PathParam(value = "storeId") String storeId,
                                     @PathParam(value = "invoiceId") String invoiceId, @Auth Account account) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(TOKEN, TOKEN_VALUE);
        var ret = HttpsUtils.Get(BTCPAY_URL + "/api/v1/stores/" + storeId + "/invoices/" + invoiceId + "/payment-methods", headers);
        return new ResponseEntity(JSON.parseArray(ret, Map.class), 1, 1, 10);
    }

    @GET
    @Path("/stores/{storeId}/invoices/{invoiceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity getInvoice(@PathParam(value = "storeId") String storeId,
                                     @PathParam(value = "invoiceId") String invoiceId,
                                     @Auth Account account, @Context SqlSession session) {
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
        return new ResponseEntity(list, 1, 1, 10);
    }
}
