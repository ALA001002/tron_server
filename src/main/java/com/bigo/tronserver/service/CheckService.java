package com.bigo.tronserver.service;

import com.bigo.tronserver.FileUtil;
import com.bigo.tronserver.config.TrxConfig;
import com.bigo.tronserver.dao.BalanceRepository;
import com.bigo.tronserver.entity.Balance;
import com.bigo.tronserver.factory.ApiFactory;
import com.bigo.tronserver.model.ApiInstance;
import com.bigo.tronserver.model.TranAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

@Service
@Slf4j
public class CheckService extends Thread{

    private volatile static Queue<TranAddress> contractList = new LinkedList<>();

    private final static String FILE_NAME = "address_poll.txt";
    @Resource
    BalanceRepository balanceRepository;

    @Resource
    TrxConfig trxConfig;
    public void push(String txId,String address){
        push(TranAddress.builder().txId(txId).address(address).build());
    }
    public void push(TranAddress tranAddress){
        log.info("push TranAddress={}",tranAddress);
        contractList.offer(tranAddress);
    }

    @Override
    public void run() {
        log.info("check service 0");
        super.run();
        log.info("check service 1");
        try {
            Queue<TranAddress> tranAddresses = FileUtil.readData(FILE_NAME);
            if(!CollectionUtils.isEmpty(tranAddresses)) {
                contractList.addAll(tranAddresses);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String privateKey = trxConfig.getPrivateKey();
        log.info("check service 1");
        String endpoint = trxConfig.getEndpoint();
        log.info("check service 2");
        String collectContractAddress = trxConfig.getCollectContractAddress();
        ApiInstance apiInstance = ApiFactory.getInstance(privateKey,endpoint);
        log.info("check service");
        TranAddress tranAddress = contractList.poll();
        while (true) {
            if(tranAddress!=null && !StringUtils.isEmpty(tranAddress.getAddress())) {
                String address = tranAddress.getAddress();
                String txId = tranAddress.getTxId();
                log.info("checkService txId={},address={}",txId,address);
                BigInteger balance = apiInstance.queryTrc20Balance(collectContractAddress, address);
                log.info("checkService txId={},address={},balance={}",txId,address,balance);
                long trxBalance = apiInstance.queryBalance(address);
                Balance balanceData = balanceRepository.findFirstByContractAddressAndAddress(collectContractAddress, address);
                LocalDateTime now = LocalDateTime.now();
                if (balanceData == null) {
                    balanceData = new Balance();
                    balanceData.setCreateTime(now);
                    balanceData.setContractAddress(collectContractAddress);
                    balanceData.setAddress(address);
                }
                balanceData.setLastTxId(txId);
                balanceData.setUpdateTime(now);
                balanceData.setBalance(balance);
                balanceData.setTrxBalance(BigInteger.valueOf(trxBalance));
                balanceRepository.save(balanceData);
            }else{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("sleep error");
                }
            }
            tranAddress = contractList.poll();
            try {
                FileUtil.writeData(contractList,FILE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        contractList.offer(TranAddress.builder().address("123123").txId("123123").build());
        System.out.println(contractList.poll());
        System.out.println(contractList.poll());
        System.out.println(contractList.poll());
        System.out.println(contractList.poll());
    }

}
