package com.bigo.tronserver.service;

import com.bigo.tronserver.config.TrxConfig;
import com.bigo.tronserver.dao.BalanceRepository;
import com.bigo.tronserver.dao.TronAddressRepository;
import com.bigo.tronserver.entity.Balance;
import com.bigo.tronserver.entity.TronAddress;
import com.bigo.tronserver.factory.ApiFactory;
import com.bigo.tronserver.model.ApiInstance;
import com.bigo.tronserver.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Response;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class CheckBalanceService {

    @Resource
    TronAddressRepository tronAddressRepository;

    @Resource
    BalanceRepository balanceRepository;

    @Resource
    TransferService transferService;

    @Resource
    TronService tronService;

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Value("${wallet.minUsdt:1}")
    int minUsdt;

    Queue<String> ADDRESS_QUEUE = new ConcurrentLinkedQueue<>();
    @Resource
    TrxConfig trxConfig;
    public List<Balance> queryBalances(){
        return balanceRepository.findAllByBalanceGreaterThan(BigInteger.ZERO);
    }


    private static volatile Integer index = 0;

    public Result sendFeeAndCollect(String address) {
        Balance balance = balanceRepository.findFirstByAddress(address);
        String result = transferService.sendFeeAndCollect(balance, BigInteger.TEN.pow(6).divide(BigInteger.valueOf(2)));
        return Result.success(result);
    }
    public void startCollect() {
        startCollect(null);
    }
    public void startCollect(Integer pMinUsdt) {
        new Thread(){
            @Override
            public void run() {
                List<Balance> all = balanceRepository.findAllByBalanceGreaterThan(BigInteger.ONE);
                log.info("startCollect all={}",all.size());
                for (Balance e : all) {
                    try {
                        Integer status = e.getStatus();
                        log.info("collect status={}", status);
                        String txId = transferService.sendFeeAndCollect(e, BigInteger.TEN.pow(6).multiply(BigInteger.valueOf(pMinUsdt==null?minUsdt:pMinUsdt)));
                        log.info("collect address={},txId={}", e.getAddress(), txId);
                    } catch (Exception a) {
                        log.info("collect error={}", a);
                    }
                }
            }
        }.start();
    }

    public Result checkStatus(String txId) {
        ApiInstance apiInstance = tronService.getApiInstance();
        int i = apiInstance.queryStatus(txId);
        return Result.success();
    }

    public Byte checkStatusValue(String txId){
        ApiInstance apiInstance = tronService.getApiInstance();
        byte i = apiInstance.queryStatus(txId);
        return i;
    }

    public Result checkAccount(String address) {
        ApiInstance apiInstance = tronService.getApiInstance();
        return Result.success(apiInstance.getAccount(address));
    }

    public BigInteger queryUSDTBalance(String fromAddress){
        ApiInstance instance = trxConfig.getInstance();
        String collectContractAddress = trxConfig.getCollectContractAddress();
        BigInteger balance = instance.queryTrc20Balance(collectContractAddress, fromAddress);
        return balance;
    }

    public class QueryBalanceTask implements Runnable{
        ApiInstance instance;

        public QueryBalanceTask(ApiInstance instance) {
            this.instance = instance;
        }

        @Override
        public void run() {
            while (true) {
                String address = ADDRESS_QUEUE.poll();
                while (!StringUtils.isEmpty(address)) {
                    index++;
                    log.info("checkBalance address={},index={}", address, index);
                    try {
                        String collectContractAddress = trxConfig.getCollectContractAddress();
                        BigInteger balance = instance.queryTrc20Balance(collectContractAddress, address);
                        Balance tronBalance = balanceRepository.findFirstByContractAddressAndAddress(collectContractAddress, address);
                        if (tronBalance == null) {
                            tronBalance = new Balance();
                            tronBalance.setCreateTime(LocalDateTime.now());
                            tronBalance.setAddress(address);
                            tronBalance.setContractAddress(collectContractAddress);
                        }
                        tronBalance.setBalance(balance);
                        if(balance!=null && balance.compareTo(BigInteger.TEN.pow(6))>0) {
                            tronBalance.setStatus(null);
                        }
                        tronBalance.setUpdatedAt(LocalDateTime.now());
                        balanceRepository.save(tronBalance);
                    } catch (Exception error) {
                        log.error("checkBalance address={},e={}", address, error);
                    }
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    address = ADDRESS_QUEUE.poll();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void checkBalance(){
        List<TronAddress> all = tronAddressRepository.findAll();
        ApiInstance instance = trxConfig.getInstance();
        log.info("checkBalance size={}",all.size());
        all.forEach(e->{
            String address = e.getAddress();
            ADDRESS_QUEUE.add(address);
        });
        index = 0;
        for (int i=0;i<10;i++){
            executorService.submit(new QueryBalanceTask(instance));
        }
        log.info("checkBalance end");
    }

    public static void main(String[] args) {
        List<Integer> a = new ArrayList<>();
        a.add(123);
        a.add(12);
        a.forEach(e->{
            System.out.println(e);
            return;
        });
//        ApiInstance instance = ApiFactory.getInstance("1d414c1e039c4644a139fbee1997cf681b26fe2ebf61dace017175c4c8260c2a", "182.160.13.52:50051");
////        long tLg851kuRLznR6nzbrQpj7tLn7aArZnAyB = instance.queryBalance("TY9WydLaaxeG3LaCHub9RUgvxA2h6Cc4YY");
//        byte i = instance.queryStatus("2b8c68988519a1af57692e46c06d75b3e580e22d939aec080ad7dcca249afdde");
//        System.out.println(i);
//        System.out.println(tLg851kuRLznR6nzbrQpj7tLn7aArZnAyB);
    }
}
