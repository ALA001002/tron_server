package com.bigo.tronserver.service;

import com.alibaba.fastjson.JSONObject;
import com.bigo.tronserver.dao.BalanceRepository;
import com.bigo.tronserver.dao.TransactionRepository;
import com.bigo.tronserver.dao.TronAddressRepository;
import com.bigo.tronserver.entity.Balance;
import com.bigo.tronserver.entity.Transaction;
import com.bigo.tronserver.entity.TronAddress;
import com.bigo.tronserver.exception.TransferException;
import com.bigo.tronserver.model.ApiInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class TransferService {

//    @Value("${api.key:}")
//    String apiKey;

    @Value("${wallet.feePrivateKey}")
    String feePrivateKey;

    @Value("${wallet.collectAddress}")
    String collectAddress;

    @Value("${tron.endpoint}")
    String endpoint;

    @Resource
    TronService tronService;

    @Resource
    TransactionRepository transactionRepository;

    @Resource
    BalanceRepository balanceRepository;

    @Resource
    TronAddressRepository tronAddressRepository;

    @Value("${wallet.minUsdt:0}")
    BigDecimal minUsdt;

    @Resource
    TronFreezeService tronFreezeService;

    private final static int ACTIVE_ENERGY = 0;//25000;//非第一次转账
    private final static BigInteger FOUR_TRX = BigInteger.TEN.pow(5);

    public void sendFee(Transaction transaction, String contractAddress, String toAddress, TronAddress address) {
        BigDecimal minUsdtValue = Optional.ofNullable(minUsdt).orElse(BigDecimal.ZERO).multiply(BigDecimal.TEN.pow(6));
        log.info("sendFee minUsdtValue={}", minUsdtValue);
        sendFee(transaction, contractAddress, toAddress, address, minUsdtValue.toBigInteger());
    }

    public static void main(String[] args) {

    }

    public void sendFee(Transaction transaction, String contractAddress, String toAddress, TronAddress address, BigInteger minUsdt) {
        ApiInstance instance = tronService.getApiInstance(feePrivateKey);
        String feeAddress = instance.getAddress();
        BigInteger originAmount = transaction.getOriginAmount();
        BigInteger amount = originAmount;
        try {
            amount = instance.queryTrc20Balance(contractAddress, toAddress);
        } catch (Exception e) {
            log.info("sendFee={},contractAddress={},toAddress={},address={}", amount, contractAddress, toAddress, address);
        }
        log.info("sendFee address={},amount={},minUsdt={}", address, amount, minUsdt);
        if (amount == null || amount.compareTo(minUsdt) < 0) {
            return;
        }
        boolean active = Optional.ofNullable(address.getActive()).orElse(false);
        Long energy = instance.calcEnergyUsed(toAddress, collectAddress, contractAddress, amount);
        BigInteger trx = BigInteger.valueOf(5);
        String rechargeTxid = transaction.getTxid();
        long trxEnergy = 0;
        try {
            trxEnergy = instance.calcTrxEnergy();
            log.info("sendFee toAddress={}, amount={},trxEnergy={}", toAddress, amount, trxEnergy);
            long bandWidth = instance.calcTrc20BandWidth(toAddress, collectAddress, contractAddress, amount);
            log.info("sendFee collectAddress={},feeAddress={},toAddress={},energy={},trx:{},bandWidth={}", collectAddress, feeAddress, address, energy, trx, bandWidth);
            if (!active) {
                instance.transferFee(feeAddress, toAddress, BigInteger.valueOf(1));
            }
            boolean flag = tronFreezeService.fillEnergyOrNet(rechargeTxid, toAddress, energy, bandWidth,0,amount,contractAddress);
            if(flag){
                TronAddress firstByAddress = tronAddressRepository.findFirstByAddress(toAddress);
                ApiInstance temp = tronService.getApiInstance(firstByAddress.getHexPrivateKey(),endpoint);
                temp.sendTrc20WithTxId(contractAddress,toAddress, collectAddress, originAmount);
            }
        } catch (IllegalException e) {
            log.error("calcTrxEnergy e={}", e);
        } catch (TransferException e) {
            log.error("sendFee e={}", e);
        }
    }

    public String sendFeeAndCollect(Balance balance, BigInteger minUsdt) {
        log.info("sendFeeAndCollect balance={}", JSONObject.toJSONString(balance));
        String toAddress = balance.getAddress();
        KeyPair keyPair = new KeyPair(feePrivateKey);
        String base58CheckAddress = keyPair.toBase58CheckAddress();
        // 提现地址 不处理
        if(toAddress.equals(collectAddress) || toAddress.equals(base58CheckAddress)){
            log.info("skip withdrawAddress={},collectAddress={},base58CheckAddress={}",toAddress,collectAddress,base58CheckAddress);
            return null;
        }
        String contractAddress = balance.getContractAddress();
        ApiInstance instance = tronService.getApiInstance(feePrivateKey, endpoint);
        log.info("collectInstance={}", endpoint);
        String feeAddress = instance.getAddress();
        BigInteger originAmount = instance.queryTrc20Balance(contractAddress, toAddress);
//        BigInteger originAmount = balance.getBalance();
        if (originAmount == null || originAmount.compareTo(minUsdt) < 0) {
            log.info("sendFeeAndCollect filter toAddress={},amount={},minUsdt={}", toAddress, originAmount, minUsdt);
            return null;
        }
        log.info("sendFeeAndCollect toAddress={},amount={},minUsdt={}", toAddress, originAmount, minUsdt);
        Long energy = instance.calcEnergyUsed(toAddress, collectAddress, contractAddress, originAmount);
        TronAddress tronAddress = tronAddressRepository.findFirstByAddress(toAddress);
        boolean active = false;
        if (null != tronAddress) {
            active = Optional.ofNullable(tronAddress.getActive()).orElse(false);
        }else{
            log.info("address={} not exist",toAddress);
            return null;
        }
        try {
            long trxEnergy = instance.calcTrxEnergy();
            log.info("sendFee collectAddress={},toAddress={}, amount={},trxEnergy={}", collectAddress, toAddress, originAmount, trxEnergy);
            long bandWidth = instance.calcTrc20BandWidth(toAddress, collectAddress, contractAddress, originAmount);
            if (!active) {
                instance.transferFee(feeAddress, toAddress, BigInteger.valueOf(1));
            }
            String format = String.format("%s-%s", toAddress, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            log.info("sendFeeAndCollect txid={}",format);
            boolean flag = tronFreezeService.fillEnergyOrNet(format, toAddress, energy, bandWidth,1,originAmount,contractAddress);
            log.info("sendFeeAndCollect flag={}", flag);
            if (flag) {
                ApiInstance temp = tronService.getApiInstance(tronAddress.getHexPrivateKey(),endpoint);
                String txId = temp.sendTrc20WithTxId(contractAddress,toAddress, collectAddress, originAmount);
                return txId;
            }
        } catch (TransferException e) {
            log.error("transferFee e={}", e);
        } catch (IllegalException e) {
            log.error("calcTrxEnergy e={}", e);
        }
        return null;
    }

//    public static void main(String[] args) {
//        KeyPair keyPair = new KeyPair("aa1bbb1b1082d1aeb16c43a192b11899c9c1a2cd5b986c37db43a331fe43b573");
//        System.out.println(keyPair.toBase58CheckAddress());
//    }
}
