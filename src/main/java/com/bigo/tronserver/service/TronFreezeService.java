package com.bigo.tronserver.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.bigo.tronserver.dao.SendEnergyRepository;
import com.bigo.tronserver.dao.SuccessEnergyRepository;
import com.bigo.tronserver.dao.TransactionRepository;
import com.bigo.tronserver.dao.TronAddressRepository;
import com.bigo.tronserver.entity.SendEnergy;
import com.bigo.tronserver.entity.SuccessEnergy;
import com.bigo.tronserver.entity.Transaction;
import com.bigo.tronserver.entity.TronAddress;
import com.bigo.tronserver.exception.TransferException;
import com.bigo.tronserver.factory.ApiFactory;
import com.bigo.tronserver.model.ApiInstance;
import com.bigo.tronserver.model.FrozenData;
import com.bigo.tronserver.model.TokenResult;
import com.google.protobuf.InvalidProtocolBufferException;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Convert;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.tron.trident.core.Constant.TRONGRID_MAIN_NET;
import static org.tron.trident.core.Constant.TRONGRID_MAIN_NET_SOLIDITY;

@Service
@Slf4j
public class TronFreezeService {


    @Value("${energy.token}")
    String token;

    @Resource(name="energyApiInstance")
    ApiInstance apiInstance;

    @Value("${wallet.collectAddress}")
    String collectAddress;

    @Value("${tron.endpoint}")
    String endpoint;

    @Resource
    SendEnergyRepository sendEnergyRepository;

    @Resource
    TransactionRepository transactionRepository;

    @Resource
    TronAddressRepository tronAddressRepository;

    @Resource
    SuccessEnergyRepository successEnergyRepository;

    @Resource
    TronService tronService;
    public void addSuccessEnergy(String delegateTxId){
        SuccessEnergy successEnergy = new SuccessEnergy();
        successEnergy.setTxid(delegateTxId);
        successEnergy.setUpdatedAt(LocalDateTime.now());
        successEnergyRepository.save(successEnergy);
        SendEnergy sendEnergy = sendEnergyRepository.findFirstByDelegateId(delegateTxId);
        if(sendEnergy!=null){
            startCollect(delegateTxId);
        }
    }

    private final static Queue<String> QUEUE_LIST = new ConcurrentLinkedQueue<>();


    public void updateSuccessStatus(String delegateTxId){
        SuccessEnergy successEnergy = successEnergyRepository.findFirstByTxid(delegateTxId);
        if(null!=successEnergy){
            startCollect(delegateTxId);
        }
    }

    ExecutorService executorService = Executors.newFixedThreadPool(5);
    public void startCollect(String delegateTxId){
        executorService.submit(() -> {
            try {
                log.info("startCollect delegateTxId={}",delegateTxId);
                Thread.sleep(60*1000);
                updateStatus(delegateTxId);
            } catch (InterruptedException e) {
                log.error("startCollect e={}",e);
            }
        });
//        new Thread(() -> {
//            while(true){
//                String delegateTxId;
//                log.info("QUEUE_LIST size={}",QUEUE_LIST.size());
//                if((delegateTxId=QUEUE_LIST.poll())!=null){
//                    log.info("updateStatus delegateTxId={}",delegateTxId);
//                    updateStatus(delegateTxId);
//                }
//                try {
//                    Thread.sleep(60*1000);
//                } catch (InterruptedException e) {
//                    log.error("startQueue e={}",e);
//                }
//            }
//        }).start();
    }

    public void sendTrc20(SendEnergy sendEnergy){
        String toAddress = sendEnergy.getReceiveAddress();
        TronAddress firstByAddress = tronAddressRepository.findFirstByAddress(toAddress);
        ApiInstance temp = tronService.getApiInstance(firstByAddress.getHexPrivateKey(),endpoint);
        BigInteger originAmount = sendEnergy.getOriginAmount();
        String contractAddress = sendEnergy.getContractAddress();
        int energyType = Optional.ofNullable(sendEnergy.getEnergyType()).orElse(0);
        if(energyType==0){
            String txid = sendEnergy.getTxid();
            String delegateTxId = sendEnergy.getDelegate_tx_id();
            Transaction transaction = transactionRepository.findFirstByTxid(txid);
            if (transaction != null) {
                log.info("transaction delegateTxId={},txid={}", delegateTxId, txid);
                transaction.setUpdatedAt(LocalDateTime.now());
                transaction.setStatus(6);
                transactionRepository.save(transaction);
            }
        }
        try {
            String txId = temp.sendTrc20WithTxId(contractAddress,toAddress, collectAddress, originAmount);
            log.info("sendTrc20WithTxId txid={}",txId);
        } catch (TransferException e) {
            log.info("sendTrc20WithTxId e={}",e);
        }
    }

    public synchronized void updateStatus(String delegateTxId){
        SendEnergy sendEnergy = sendEnergyRepository.findFirstByDelegateId(delegateTxId);
        if(sendEnergy!=null){
            log.info("updateStatus delegateTxId={}",delegateTxId);
            String txid = sendEnergy.getTxid();
//            int energyType = Optional.ofNullable(sendEnergy.getEnergyType()).orElse(0);
            sendTrc20(sendEnergy);
//            if(energyType==0) {
//                sendTrc20(sendEnergy);
//            }else{
//                String toAddress = sendEnergy.getReceiveAddress();
//                TronAddress firstByAddress = tronAddressRepository.findFirstByAddress(toAddress);
//                ApiInstance temp = tronService.getApiInstance(firstByAddress.getHexPrivateKey(),endpoint);
//                BigInteger originAmount = sendEnergy.getOriginAmount();
//                String contractAddress = sendEnergy.getContractAddress();
//                try {
//                    String txId = temp.sendTrc20WithTxId(contractAddress,toAddress, collectAddress, originAmount);
//                    log.info("sendTrc20WithTxId txid={}",txId);
//                } catch (TransferException e) {
//                    log.info("sendTrc20WithTxId e={}",e);
//                }
//            }
        }
    }

    public Boolean fillEnergyOrNet(String txid,String receiveAddress,long energy,long brand,int type){
        return fillEnergyOrNet(txid,receiveAddress,energy,brand,type,null,null);
    }

    public boolean fillEnergyOrNet(String txid,String receiveAddress,long energy,long brand,int type,BigInteger amount,String contractAddreess) {
        SendEnergy firstByTxid = sendEnergyRepository.findFirstByTxid(txid);
        if(null!=firstByTxid){
            return true;
        }
        SendEnergy sendEnergy = new SendEnergy();
        boolean flag = true;
        try{
            Response.AccountResourceMessage accountResourceMessage = apiInstance.queryAccountResource(receiveAddress);
            long freeNetUsed = accountResourceMessage.getFreeNetUsed();
            long freeNetLimit = accountResourceMessage.getFreeNetLimit();
            long netLimit = freeNetLimit-freeNetUsed;
            long energyLimit = accountResourceMessage.getEnergyLimit();
            long netCount = 0l;
            log.info("freeNetLimit={},freeNetUsed={},netLimit={},energyLimit={}",freeNetLimit,freeNetUsed,netLimit,energyLimit);
            if(netLimit<brand){
                netCount = brand;
                netCount = (long)(netCount * 1.1);
                sendEnergy.setNet(netCount);
                flag = false;
            }
            long energyCount = 0l;
            if(energyLimit<energy){
                energyCount = energy - energyLimit;
                energyCount = (long)(energyCount * 1.1);
                sendEnergy.setEnergy(energyCount);
                flag = false;
            }
            if(energyCount>0 || netCount>0) {
                TokenResult<FrozenData> frozen = frozen(energyCount, netCount, receiveAddress, "1h");
                FrozenData data = frozen.getData();
                if(data != null) {
                    BeanUtils.copyProperties(data, sendEnergy);
                }
                sendEnergy.setEnergyCode(frozen.getCode());
                sendEnergy.setEnergyResp(frozen.getOrigin());
            }
        }catch (Exception e){
            log.error("fillEnergyOrNet e={}",e);
            sendEnergy.setStatus("-1");
        }
        sendEnergy.setUpdatedAt(LocalDateTime.now());
        sendEnergy.setOriginAmount(amount);
        sendEnergy.setContractAddress(contractAddreess);
        sendEnergy.setTxid(txid);
        sendEnergy.setEnergyType(type);
        sendEnergy.setReceiveAddress(receiveAddress);
        sendEnergy.setDelegateId(sendEnergy.getDelegate_tx_id());
        sendEnergyRepository.save(sendEnergy);
        String delegateTxId = sendEnergy.getDelegate_tx_id();
        if(!flag) {
            updateSuccessStatus(delegateTxId);
        }
        return flag;
    }

    public TokenResult<FrozenData> frozen(Long energy,Long netCount,String receiveAddress,String period) {
        if(StringUtils.isEmpty(period)){
            period = "1h";
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("token",token);
        String type="energy";
        if(netCount>0){
            type="both";
            paramMap.put("net_count",netCount);
            if(energy==0){
                energy=30000l;
            }
            if(energy<30000){
                energy=30000l;
            }
        }
        paramMap.put("type",type);
        paramMap.put("count",energy);
        paramMap.put("address",receiveAddress);
        paramMap.put("period",period);
        log.info("frozen paramMap={}",paramMap);
        HttpResponse<String> response = Unirest.post("https://weidubot.cc/api/trc_api/frozen_energy_sync")
                .contentType("multipart/form-data")
                .header("content-type","multipart/form-data")
                .header("user-agent","weidubot_xiang")
                .queryString(paramMap).asString();
        String body = response.getBody();
        TokenResult<FrozenData> frozenDataTokenResult = JSON.parseObject(body, new TypeReference<TokenResult<FrozenData>>() {
        });
        log.info("frozenDataTokenResult={}",frozenDataTokenResult);
        frozenDataTokenResult.setOrigin(body);
        return frozenDataTokenResult;
    }


    public static void test() throws IllegalException {
//        TronFreezeService tronFreezeService = new TronFreezeService();
//        tronFreezeService.apiInstance = ApiFactory.getInstance("17d563a8b6576a94a9c5701d03f3162a45ac38662a6871ac39f964d93096f42f",TRONGRID_MAIN_NET,TRONGRID_MAIN_NET_SOLIDITY);
//        tronFreezeService.collectAddress = "TGrZSpwa3qmFzPioSeC5K7vrgfLpqZR7wz";
//        tronFreezeService.fillEnergyOrNet("TVMq5XhVQthyW87ERxT6jVM7M29wyA2LTX",10000000,10000000);
//        Transaction transaction = new Transaction();
//        transaction.setContractAddress("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t");
//        transaction.setOriginAmount(BigInteger.valueOf(9000000));
//        transaction.setAmount(BigDecimal.valueOf(9));
//        transaction.setToAddress("THnQASwRXBH6WDUc95r184Yz3aXGcpbqoj");
//        tronFreezeService.apiInstance.undelegateResourceOfEnergy("TVMq5XhVQthyW87ERxT6jVM7M29wyA2LTX",BigDecimal.valueOf(3730.8));
//        tronFreezeService.apiInstance.unfreezeBalanceV2OfEnergy(BigDecimal.valueOf(3730.8),1);
//        tronFreezeService.apiInstance.unfreezeBalanceV2OfEnergy(BigDecimal.valueOf(1094.7),0);
//        tronFreezeService.sendFeeEnergy(transaction, transaction.getContractAddress(), "TVMq5XhVQthyW87ERxT6jVM7M29wyA2LTX", BigInteger.valueOf(1000000));
//        Map<String,String> headers = new HashMap<>();
//        headers.put("content-type", "application/json");
////        {\"owner_address\":\"TZ4UXDV5ZhNW7fb2AMSbgfAEZ7hWsnYS2g\",\"frozen_balance\":10000000,\"resource\":\"ENERGY\",\"visible\":true}"
//        Map<String,Object> params = new HashMap<>();
//        params.put("owner_address","TVMq5XhVQthyW87ERxT6jVM7M29wyA2LTX");
//        params.put("frozen_balance",10000000);
//        params.put("resource","ENERGY");
//        params.put("visible",true);
//        HttpResponse<String> response = Unirest.post("https://api.shasta.trongrid.io/wallet/freezebalancev2").headers(headers).body(params).asString();
//        System.out.println(response.getBody());
//        ApiInstance instance = ApiFactory.getInstance("cc4e8818655ad1581dd7198b74cc3baedfe5cd142dc6872a590d9b48558c29bd",null);
//        ApiWrapper apiWrapper = instance.getApiWrapper();
//        String hexPrivateKey = "cc4e8818655ad1581dd7198b74cc3baedfe5cd142dc6872a590d9b48558c29bd";
////        ApiWrapper apiWrapper = new ApiWrapper(TRONGRID_SHASTA,TRONGRID_SHASTA_SOLIDITY,"cc4e8818655ad1581dd7198b74cc3baedfe5cd142dc6872a590d9b48558c29bd");
//        BigDecimal sun = Convert.toSun("1", Convert.Unit.TRX);
//        long l = sun.longValue();
//        System.out.println(l);
//        String ownerAddress = "TVMq5XhVQthyW87ERxT6jVM7M29wyA2LTX";
//        String receiveAddress = "TVd6yFBZcDVPvLjud5ZXA9fWGmxrK5QHZN";
////        ApiInstance instance = ApiFactory.getInstance(hexPrivateKey, null);
////        instance.unfreezeBalanceV2OfEnergy(BigDecimal.valueOf(1000),1);
////        instance.undelegateResource(receiveAddress,500l,1);
//        ApiInstance instance = ApiFactory.getInstance("1ca24d6b4771c845d26ec197725302620aca615b00cd827379ace34f2c3960f4", null);
//        instance.unfreezeBalanceV2OfEnergy(BigDecimal.valueOf(1000),1);
//        instance.undelegateResource(receiveAddress,500l,1);
//        Long aLong = instance.calcEnergyUsed("TVd6yFBZcDVPvLjud5ZXA9fWGmxrK5QHZN", "TVdFN8moWMTPtRBzzuyXfQNA2gxTEbGnDa", "TPxRs3Bajzb1ekuG88tf41CDD13tAHmu5N", BigInteger.ONE);
//        System.out.println(aLong);
//        try {
//            instance.sendTrc20("TPxRs3Bajzb1ekuG88tf41CDD13tAHmu5N","TVd6yFBZcDVPvLjud5ZXA9fWGmxrK5QHZN","TVdFN8moWMTPtRBzzuyXfQNA2gxTEbGnDa",BigInteger.ONE);
//        } catch (TransferException e) {
//            throw new RuntimeException(e);
//        }
//        instance.queryAccountResource(receiveAddress);
//        String hash = instance.freezeBalanceV2(1000, 1);
//        instance.delegateResource(receiveAddress, 1000, 1);
//        Long aLong = instance.calcEnergyUsed("TVMq5XhVQthyW87ERxT6jVM7M29wyA2LTX", "TVd6yFBZcDVPvLjud5ZXA9fWGmxrK5QHZN", "TBZERcbYLZvb7wJYFAzYA2dkTuyLYKh1Qq", BigInteger.valueOf(10000000));
//        System.out.println(aLong);
//        instance.sendEnergy(receiveAddress, BigDecimal.ONE);
//        Response.TransactionExtention transactionExtention = apiWrapper.freezeBalanceV2(ownerAddress, l, 1);
//        try {
//            Chain.Transaction transaction = apiWrapper.signTransaction(transactionExtention.getTransaction());
//            String s = apiWrapper.broadcastTransaction(transaction);
//            System.out.println(s);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        Response.TransactionExtention transactionExtention1 = apiWrapper.delegateResource(ownerAddress, l, 1, "TVd6yFBZcDVPvLjud5ZXA9fWGmxrK5QHZN", false);
//
////        Response.TransactionExtention transactionExtention = apiWrapper.freezeBalance("TVMq5XhVQthyW87ERxT6jVM7M29wyA2LTX", 1000000, 3,1, "TVd6yFBZcDVPvLjud5ZXA9fWGmxrK5QHZN");
//        try {
//            Chain.Transaction transaction = apiWrapper.signTransaction(transactionExtention1.getTransaction());
//            String s = apiWrapper.broadcastTransaction(transaction);
//            System.out.println(s);
//        }catch (Exception e){
//            e.printStackTrace();
//        }


//        OkHttpClient client = new OkHttpClient();
//
//        MediaType mediaType = MediaType.parse("application/json");
//        RequestBody body = RequestBody.create(mediaType, "{\"owner_address\":\"TZ4UXDV5ZhNW7fb2AMSbgfAEZ7hWsnYS2g\",\"frozen_balance\":10000000,\"resource\":\"ENERGY\",\"visible\":true}");
//        Request request = new Request.Builder()
//                .url("https://api.shasta.trongrid.io/wallet/freezebalancev2")
//                .post(body)
//                .addHeader("accept", "application/json")
//                .addHeader("content-type", "application/json")
//                .build();
//
//        Response response = client.newCall(request).execute();
    }

    private static void aa() throws IllegalException {
        ApiWrapper apiWrapper = new ApiWrapper("3.225.171.164:50051", "3.225.171.164:50052","d0eb0c4ba56ebafca09992a5fdf35f11e46fb26550b81278d8bb887c675870dd");
        Response.AccountResourceMessage accountResource = apiWrapper.getAccountResource("TTuLE5EFdfJb7AsTQ8pViH7WjFRn1S6FVY");
        Response.AccountNetMessage accountNet = apiWrapper.getAccountNet("TTuLE5EFdfJb7AsTQ8pViH7WjFRn1S6FVY");
        System.out.println(accountResource);
        System.out.println(accountNet);
//        Response.TransactionInfo transactionInfo = apiWrapper.getTransactionInfoById("bc62db249d20dab10118f0cc1f487d00d3dcbf172628dde4c8d30733759d959c");
//        System.out.println(transactionInfo);
//        transactionInfo.get
    }

    public static void main(String[] args) throws IllegalException {
//        new TronFreezeService().active("account");
//        new TronFreezeService().frozen("123123",BigDecimal.valueOf(33000),"THnQASwRXBH6WDUc95r184Yz3aXGcpbqoj","energy");
//        test();
//        System.out.println(apiInstance1.getAddress());
//        aa();
//        System.out.println((long)(1000l*1.1));
        ApiInstance apiInstance = ApiFactory.getInstance("d0eb0c4ba56ebafca09992a5fdf35f11e46fb26550b81278d8bb887c675870dd", "101.44.38.214:50051", "101.44.38.214:50061");
//        Long aLong = apiInstance.calcEnergyUsed("TQ8QsMWrNChyx3YnzvD1BWRBrGwkEu1Mhb", "TGp9jdZLfPH8MFzrq77foiGDy9MUuaYwpd", "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t", BigInteger.valueOf(3000000));
//        System.out.println("along="+aLong);
        Chain.Block newBlock1 = apiInstance.getNewBlock();
        System.out.println(newBlock1.getBlockHeader().getRawData().getNumber());
//        Response.AccountResourceMessage accountResource = apiInstance.getAccountResource();
//        Chain.Block newBlock = apiInstance.getNewBlock();
//        List<Chain.Transaction> transactionsList = newBlock.getTransactionsList();
////        System.out.println(accountResource);
////        System.out.println(newBlock);
////        System.out.println(transactionsList.size());
//        try {
//            apiInstance.parse(newBlock);
//        } catch (InvalidProtocolBufferException e) {
//            log.error("parse={}",newBlock);
//        }

    }
}
