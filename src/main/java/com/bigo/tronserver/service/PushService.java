package com.bigo.tronserver.service;

import com.alibaba.fastjson.JSON;
import com.bigo.tronserver.dao.PushLogRepository;
import com.bigo.tronserver.entity.PushLog;
import com.bigo.tronserver.entity.Transaction;
import com.bigo.tronserver.event.PushData;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class PushService {
    @Value("${trx.push.url:}")
    String pushUrl;

    @Resource
    PushLogRepository pushLogRepository;

    public void pushBalanceChangeByTransaction(Transaction transaction,Integer uid){
        pushBalanceChange(transaction.getTxid(), uid,transaction.getAmount(),transaction.getType(),transaction.getSymbol(),false,transaction.getFromAddress(),transaction.getToAddress());
    }

    public void pushBalanceChange(String txId,Integer uid, BigDecimal balance,Byte type,String symbol,Boolean testNet,String fromAddress,String toAddress){
        push(PushData.builder().txId(txId).uid(uid).test(testNet).balance(balance).type(type).symbol(symbol).fromAddress(fromAddress).address(toAddress).method("changeBalance").build());
    }

    public void pushAddress(Integer uid, String address){
        push(PushData.builder().uid(uid).address(address).method("address").build());
    }

    public void push(PushData pushData){
        if(StringUtils.isEmpty(pushUrl)){
            log.error("pushUrl is null");
        }
        String postBody = JSON.toJSONString(pushData);
        log.info("push pushUrl={},body={}",pushUrl,pushData);
        PushLog pushLog = new PushLog();
        String body = null;
        try {
            HttpResponse<String> stringHttpResponse = Unirest.post(pushUrl).body(postBody).header("content-type", "application/json").asString();
            body = stringHttpResponse.getBody();
        }catch (Exception e){
            log.error("error={}",e);
        }
        Byte status = 0;
        if("SUCCESS".equals(body)){
            status=1;
        }
        pushLog.setPushData(postBody);
        pushLog.setUid(pushData.getUid());
        pushLog.setAddress(pushData.getAddress());
        pushLog.setSuccess(status);
        pushLog.setResult(body);
        pushLog.setCreateTime(LocalDateTime.now());
        pushLogRepository.save(pushLog);
    }
}
