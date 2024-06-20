package com.bigo.tronserver.service;

import com.bigo.tronserver.dao.CoinRepository;
import com.bigo.tronserver.entity.TronCoin;
import com.bigo.tronserver.model.ApiInstance;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class CoinService {

    @Resource
    CoinRepository coinRepository;

    private final static Map<String,TronCoin> CACHE = new HashMap<>();
    public TronCoin findFirstBySymbolAndTest(String symbol,boolean test){
        return coinRepository.findFirstBySymbolAndTest(symbol,test);
    }
    public synchronized TronCoin queryCoin(String contractAddress, boolean test, ApiInstance apiInstance){
        TronCoin tronCoin = CACHE.get(contractAddress);
        if(tronCoin==null) {
            tronCoin = coinRepository.findFirstByContractAddressAndTest(contractAddress, test);
        }
        if(tronCoin==null){
            tronCoin = new TronCoin();
            tronCoin.setCreatedTime(LocalDateTime.now());
            tronCoin.setTest(test);
            tronCoin.setContractAddress(contractAddress);
            String symbol = apiInstance.querySymbol(contractAddress);
            if(StringUtils.isEmpty(symbol)){
                return null;
            }
            tronCoin.setSymbol(symbol);
            String name = apiInstance.queryName(contractAddress);
            tronCoin.setName(name);
            BigInteger decimals = apiInstance.queryDecimals(contractAddress);
            tronCoin.setDecimals(decimals);
            coinRepository.save(tronCoin);
        }
        CACHE.put(contractAddress,tronCoin);
        return tronCoin;
    }
}
