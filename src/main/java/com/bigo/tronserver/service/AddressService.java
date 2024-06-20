package com.bigo.tronserver.service;

import com.bigo.tronserver.dao.TronAddressRepository;
import com.bigo.tronserver.entity.TronAddress;
import com.bigo.tronserver.model.OriginTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Service
@Slf4j
public class AddressService {

    @Resource
    CheckService checkService;

    @Resource
    TransactionService transactionService;

    @Resource
    TronService tronService;


    public TronAddress inAddress(String address){
//        log.info("inAddress={},TRON_ADDRESS_MAP={}",address,TronService.TRON_ADDRESS_MAP);
        TronAddress tronAddress = TronService.TRON_ADDRESS_MAP.get(address);
        return tronAddress;
    }

    public void compareAddress(OriginTransaction originTransaction,boolean testNet){
        String txId = originTransaction.getTxId();
        boolean withdraw = tronService.isWithdraw(txId);
        log.info("compareAddress txId={}",txId);
        if(withdraw){
            checkService.push(txId,originTransaction.getFromAddress());
            transactionService.addTransaction(originTransaction,txId);
            return;
        }
        String fromAddress = originTransaction.getFromAddress();
        TronAddress tronAddress = inAddress(fromAddress);
        log.info("compareAddress txId={},fromAddress={},tronAddress={}",txId,fromAddress,tronAddress);
        if(tronAddress!=null){
            checkService.push(txId,originTransaction.getFromAddress());
            transactionService.addFromTransaction(originTransaction,tronAddress,false,testNet);
        }
        String toAddress = originTransaction.getToAddress();
        tronAddress = inAddress(toAddress);
        log.info("compareAddress txId={},toAddress={},tronAddress={}",txId,toAddress,tronAddress);
        if(tronAddress!=null){
            checkService.push(txId,originTransaction.getToAddress());
            transactionService.addFromTransaction(originTransaction,tronAddress,true,testNet);
        }
    }
}
