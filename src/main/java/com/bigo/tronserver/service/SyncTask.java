package com.bigo.tronserver.service;

import com.bigo.tronserver.factory.ApiFactory;
import com.bigo.tronserver.model.ApiInstance;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain;

@Slf4j
public class SyncTask implements Runnable {
    Chain.Block block = null;
    ApiInstance instance;
    long blockNum;
    TransactionService transactionService;
    SuccessCallback successCallback;
    interface  SuccessCallback{
        void execute(Long blockNum);
    }
    public SyncTask(Chain.Block block, ApiInstance instance,TransactionService transactionService){
        this(block,instance,transactionService,null);
    }
    public SyncTask(Chain.Block block, ApiInstance instance,TransactionService transactionService,SuccessCallback successCallback) {
        this.block = block;
        this.instance = instance;
        this.transactionService = transactionService;
        this.successCallback =successCallback;
    }

    public SyncTask(long blockNum, ApiInstance instance,TransactionService transactionService,SuccessCallback successCallback) {
        this.blockNum = blockNum;
        this.instance = instance;
        this.transactionService = transactionService;
        this.successCallback =successCallback;
    }

    public SyncTask(long blockNum, ApiInstance instance,TransactionService transactionService){
        this(blockNum,instance,transactionService,null);
    }
    @Override
    public void run() {
        try {
            if(transactionService!=null) {
                boolean flag = transactionService.hasSyncBlock(blockNum);
                if (flag) {
                    return;
                }
            }
            log.info("start sync={}",blockNum);
            if(block!=null) {
                instance.parse(block);
            }else{
                instance.sync(blockNum);
            }
            if(successCallback!=null){
                successCallback.execute(blockNum);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("parse error={},block={},blockNum={}",e,block,blockNum);
        }
    }

    public static void main(String[] args) {
        ApiInstance apiInstance = ApiFactory.getInstance("1d414c1e039c4644a139fbee1997cf681b26fe2ebf61dace017175c4c8260c2a","182.160.3.156:50051");
        try {
            apiInstance.sync(37882160L);
        } catch (IllegalException e) {
            e.printStackTrace();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
