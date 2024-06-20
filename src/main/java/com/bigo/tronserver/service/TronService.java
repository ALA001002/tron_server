package com.bigo.tronserver.service;

import com.alibaba.fastjson.JSON;
import com.bigo.tronserver.controller.request.TransferData;
import com.bigo.tronserver.dao.*;
import com.bigo.tronserver.entity.*;
import com.bigo.tronserver.event.PushData;
import com.bigo.tronserver.exception.TransferException;
import com.bigo.tronserver.factory.ApiFactory;
import com.bigo.tronserver.model.ApiInstance;
import com.bigo.tronserver.model.OriginTransaction;
import com.bigo.tronserver.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Chain;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TronService extends Thread{

    public final static Map<String,TronAddress> TRON_ADDRESS_MAP = new HashMap<>();
    public final static Map<String,Boolean> WITHDRAW_MAP = new HashMap<>();

    static ExecutorService executorService;// = Executors.newFixedThreadPool(10);
    @Resource
    TronAddressRepository tronAddressRepository;

    @Value("${start.blockNum:30000}")
    long start;

    @Value("${api.privatekey}")
    String privatekey;

    @Value("#{'${api.key:}'.split(',')}")
    List<String> apiKey;

    @Value("${tron.endpoint:''}")
    String endpoint;
    @Value("${tron.solidityEndPoint:''}")
    String solidityEndPoint;

    @Value("${testNet:false}")
    Boolean testNet;

    @Resource
    LogRepository logRepository;

    @Resource
    TransactionService transactionService;

    @Resource
    CoinService coinService;

    @Resource
    PushService pushService;

    @Resource
    WithdrawRepository withdrawRepository;

    @Value("${wallet.transferPrivateKey:}")
    String transferPrivateKey;

    @Resource
    TransactionRepository transactionRepository;

    @Resource
    HistoryRepository historyRepository;

    @Resource
    TronFreezeService tronFreezeService;

    @Value("${thread.num:10}")
    int threadNum;
    private volatile static int index = 0;

    public synchronized String getApiKey(){
        if(index>=apiKey.size()){
            index = 0;
        }
        String key = apiKey.get(index);
        index++;
        log.info("getApiKey={}",key);
        return key;
    }
    public ApiInstance getApiInstance(){
        return ApiFactory.getInstance(privatekey,testNet?null:endpoint);
    }

    public ApiInstance getApiInstance(String hexPrivateKey){
        return ApiFactory.getInstance(hexPrivateKey,testNet?null:endpoint);
    }
    public ApiInstance getApiInstance(String hexPrivateKey,String endpoint){
        return ApiFactory.getInstance(hexPrivateKey,endpoint);
    }

    public Result address(Integer uid){
        log.info("create address={}",uid);
        KeyPair keyPair = KeyPair.generate();
        TronAddress tronAddress = tronAddressRepository.findFirstByUid(uid);
        String base58CheckAddress;
        if(tronAddress==null){
            tronAddress=new TronAddress();
            base58CheckAddress = keyPair.toBase58CheckAddress();
            String privateKey = keyPair.toPrivateKey();
            log.info("base58CheckAddress={},base58CheckAddress={},privateKey={}",base58CheckAddress,base58CheckAddress,privateKey);
            tronAddress.setAddress(base58CheckAddress);
            tronAddress.setHexPrivateKey(privateKey);
            tronAddress.setCreateAt(LocalDateTime.now());
            tronAddress.setUid(uid);
            tronAddressRepository.save(tronAddress);
            pushService.pushAddress(uid,base58CheckAddress);
            TRON_ADDRESS_MAP.put(tronAddress.getAddress(),tronAddress);
        }else{
            base58CheckAddress = tronAddress.getAddress();
        }
//        ApiFactory.getInstance();
        return Result.success(base58CheckAddress);
    }

    public ApiInstance.Callback getCallback(ApiInstance instance){
        return new ApiInstance.Callback() {
            @Override
            public void onSyncSuccess(long blockNum, int size,LocalDateTime now) {
                transactionService.createLog(blockNum,size,testNet,now);
            }

            @Override
            public void onTrc20Transaction(OriginTransaction originTransaction) {
                log.info("getCallback onTrc20Transaction={}",originTransaction);
                transactionService.compare(originTransaction,testNet);
            }

            @Override
            public TronCoin onCoin(String contractAddress) {
                return coinService.queryCoin(contractAddress,testNet,instance);
            }

            @Override
            public void onTronTransaction(OriginTransaction originTransaction) {
                transactionService.compare(originTransaction,testNet);
            }

            @Override
            public void onFail(OriginTransaction originTransaction) {
                transactionService.compare(originTransaction,testNet,false);
            }

            @Override
            public void onDelegate(String txid) {
                tronFreezeService.addSuccessEnergy(txid);
            }
        };
    }

    public void run(){
        log.info("start sync={}",LocalDateTime.now());
        executorService = Executors.newFixedThreadPool(threadNum);
        Log log = logRepository.findFirstByTestOrderByBlockNumDesc(testNet);
        long blockNum = start;
        if(log!=null && log.getBlockNum()>start){
            blockNum = log.getBlockNum();
        }
        ApiInstance instance = getApiInstance(privatekey);//ApiFactory.getInstance(privatekey, testNet?null:endpoint);
        instance.setCallback(getCallback(instance));
        Chain.Block newBlock = null;
        long number = 0;
        try {
            newBlock = instance.getNewBlock();
            number = newBlock.getBlockHeader().getRawData().getNumber();
        } catch (IllegalException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                if(number==blockNum) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else {
                    ApiInstance temp = getApiInstance(privatekey);//ApiFactory.getInstance(privatekey, testNet?null:endpoint);
                    temp.setCallback(getCallback(temp));
                    executorService.submit(new SyncTask(newBlock,temp,transactionService));
                    blockNum++;
                    while (blockNum<number) {
//                        ApiInstance temp2 = getApiInstance(privatekey);//ApiFactory.getInstance(privatekey, testNet?null:endpoint);
                        temp.setCallback(getCallback(temp));
                        executorService.submit(new SyncTask(blockNum,temp,transactionService));
                        blockNum++;
                    }
                }
                newBlock = instance.getNewBlock();
                number = newBlock.getBlockHeader().getRawData().getNumber();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(JSON.toJSONString("\u200FTHJtz3xVPeaRLtK8TCtAnthcB8JhCmT2J8"));
        System.out.println(JSON.toJSONString("\u200FTHJtz3xVPeaRLtK8TCtAnthcB8JhCmT2J8".replace("\\u200F","")));
//        KeyPair keyPair = new KeyPair("4a973d4e1ddf698a4a533654e574d409040e1f27a83d9f73e714a719795301c3");
//        System.out.println(keyPair.toPrivateKey());
//        System.out.println(keyPair.toBase58CheckAddress());
    }

    public Result balance(String symbol, Integer uid) {
        TronAddress tronAddress = tronAddressRepository.findFirstByUid(uid);
        if(tronAddress!=null){
            String hexPrivateKey = tronAddress.getHexPrivateKey();
            boolean test = Optional.ofNullable(tronAddress.getActive()).orElse(false);
            ApiInstance instance = getApiInstance(hexPrivateKey);//ApiFactory.getInstance(hexPrivateKey, endpoint);
            TronCoin tronCoin = coinService.findFirstBySymbolAndTest(symbol,test);
            if(tronCoin!=null) {
                BigInteger balance = instance.queryTrc20Balance(tronCoin.getContractAddress());
                int power = tronCoin.getDecimals().intValue();
                if(power>7){
                    power = 7;
                }
                BigDecimal price = BigDecimal.valueOf(balance.longValue());
                BigDecimal bigDecimal = BigDecimal.valueOf(tronCoin.getDecimals().intValue());
                return Result.success(price.divide(bigDecimal,power, RoundingMode.FLOOR));
            }
        }
        return Result.success(BigDecimal.ZERO);
    }

    public String notify(PushData data) {
        log.info("notify={}",data);
        return "SUCCESS";
    }

    public void createWithdrawHistory(String txId,String address,String error){
        History history = new History();
        history.setAddress(address);
        history.setTxId(txId);
        history.setErrors(error);
        history.setCreateTime(LocalDateTime.now());
        WITHDRAW_MAP.put(txId,true);
        historyRepository.save(history);
    }

    public boolean isWithdraw(String txId){
        return WITHDRAW_MAP.get(txId)!=null;
//        History firstByTxId = historyRepository.findFirstByTxId(txId);
//        return firstByTxId!=null;
    }


    public synchronized Result transfer(TransferData req) throws TransferException {
        try {
            log.info("transfer={}", JSON.toJSONString(req));
            if (StringUtils.isEmpty(transferPrivateKey)) {
                return Result.success();
            }
            Integer withdrawId = req.getWithdrawId();
            if(withdrawId==null){
                return Result.fail("提现id不能为空");
            }
            Optional<TWithdraw> optional = withdrawRepository.findById(withdrawId);
            if (!optional.isPresent()) {
                return Result.fail("提现不存在");
            }
            TWithdraw tWithdraw = optional.get();
            Byte checkStatus = tWithdraw.getCheckStatus();
            if (checkStatus == 2) {
                return Result.fail("审核驳回");
            }
//            if (status.intValue() == 1 && (realStatus==0 || realStatus==1)) {
//                return Result.fail("提现已完成");
//            }
            String symbol = req.getSymbol();
            BigDecimal price = req.getPrice();
            if(StringUtils.isEmpty(symbol)){
                return Result.fail("交易对不能为空");
            }
            if(price==null){
                return Result.fail("价格不能为空");
            }
            if("USDT_TRC20".equals(symbol)){
                symbol = "USDT";
            }
            addTransfer(req);
            return Result.success();
        }catch (Exception e) {
            String msg = e.getMessage();
            return Result.fail(msg);
        }
    }

    private final static Queue<TransferData> TRANSFER_DATA_QUEUE = new LinkedBlockingQueue<TransferData>();
    public void addTransfer(TransferData transferData){
        TRANSFER_DATA_QUEUE.add(transferData);
    }

    public void startTransfer(){
        new Thread(() -> {
            while (true) {
                TransferData item;
                log.info("startTransfer={}",TRANSFER_DATA_QUEUE.size());
                while ((item = TRANSFER_DATA_QUEUE.poll()) != null) {
                    startTransfer(item);
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (Exception e) {
                        log.info("startTransfer error={}", e);
                    }
                }
                try {
                    Thread.sleep(5*1000);
                } catch (InterruptedException e) {
                    log.error("startTransfer error={}",e);
                }
            }
        }).start();
    }

    private void startTransfer(TransferData req){
        try {
            log.info("transfer={}", JSON.toJSONString(req));
            Integer withdrawId = req.getWithdrawId();
            if(withdrawId==null){
                log.info("addTransfer is null");
                return;
            }
            Optional<TWithdraw> optional = withdrawRepository.findById(withdrawId);
            if (!optional.isPresent()) {
                log.info("TWithdraw is null");
            }
            TWithdraw tWithdraw = optional.get();
            String symbol = req.getSymbol();
            if("USDT_TRC20".equals(symbol)){
                symbol = "USDT";
            }   Byte status = tWithdraw.getStatus();
            Byte checkStatus = tWithdraw.getCheckStatus();
            byte realStatus = Optional.ofNullable(tWithdraw.getRealStatus()).orElse((byte) -1);
            if (checkStatus == 2) {
                log.info("审核驳回");
                return;
            }
            if (realStatus==0 || realStatus==1) {
                log.info("提现已完成");
                return;
            }
            BigDecimal price = req.getPrice();
            TronCoin tronCoin = coinService.findFirstBySymbolAndTest(symbol, testNet);
            ApiInstance instance = getApiInstance(transferPrivateKey);//ApiFactory.getInstance(transferPrivateKey, endpoint);
            BigDecimal amount = price.multiply(BigDecimal.TEN.pow(tronCoin.getDecimals().intValue()));
            String address = req.getAddress();
            log.info("transfer start={},symbol={},address={}", amount, symbol, address);
            String errors = null;
            String txId = null;
            try {
                Chain.Transaction transaction = instance.sendTrc20(tronCoin.getContractAddress(), instance.getAddress(), req.getAddress(), BigInteger.valueOf(amount.longValue()));
                byte[] bytes = ApiWrapper.calculateTransactionHash(transaction);
                txId  = ApiWrapper.toHex(bytes);
                errors = JSON.toJSONString(transaction.getRetList());
                if (errors != null && errors.length() > 2000) {
                    errors = errors.substring(0, 2000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            createWithdrawHistory(txId, address, errors);
            tWithdraw.setTxId(txId);
            tWithdraw.setRealStatus((byte) 0);//提现中
            withdrawRepository.save(tWithdraw);
            log.info("transfer={} end", JSON.toJSONString(req));
        }catch (Exception | TransferException e){
            log.info("addTransfer error={}",e);
        }
    }

    public Result transferTo(TransferData req) throws TransferException {
        String symbol = req.getSymbol();
        BigDecimal price = req.getPrice();
        String address = req.getAddress();
        TronAddress tronAddress = tronAddressRepository.findFirstByAddress(address);
        String hexPrivateKey = tronAddress.getHexPrivateKey();
        TronCoin tronCoin = coinService.findFirstBySymbolAndTest(symbol,testNet);
        ApiInstance instance = getApiInstance(hexPrivateKey);//ApiFactory.getInstance(transferPrivateKey, endpoint);
        BigDecimal amount = price.multiply(BigDecimal.TEN.pow(tronCoin.getDecimals().intValue()));
        instance.sendTrc20(tronCoin.getContractAddress(),instance.getAddress(),req.getAddress(),BigInteger.valueOf(amount.longValue()));
        return Result.success();
    }

    public Result collect(TransferData req) {
        Integer withdrawId = req.getWithdrawId();
        log.info("collect withdrawId={}",withdrawId);
        String txId = req.getTxId();
        if (!StringUtils.isEmpty(txId)){
            Transaction transaction = transactionRepository.findFirstByTxid(txId);
            Integer status = transaction.getStatus();
            if(status!=null && status.intValue()==6){
                return Result.fail("已经归集成功");
            }
            String toAddress = transaction.getToAddress();
            TronAddress firstByUid = tronAddressRepository.findFirstByAddress(toAddress);
//            transferService.sendFeeEnergy(transaction, transaction.getContractAddress(), toAddress,BigInteger.valueOf(10));
        }else {
            Optional<TWithdraw> byId = withdrawRepository.findById(withdrawId);
            if (!byId.isPresent()) {
                TWithdraw tWithdraw = byId.get();
                Integer uid = tWithdraw.getUid();
                TronAddress firstByUid = tronAddressRepository.findFirstByUid(uid);
                String address = firstByUid.getAddress();
                log.info("collect withdrawId={},address={},uid={}", withdrawId, address, uid);
                Transaction firstByToAddressAndStatusIn = transactionRepository.findFirstByToAddressAndStatusIn(address, Arrays.asList((byte) 0, (byte) 1, (byte) 3, (byte) 5));
//                transferService.sendFeeEnergy(firstByToAddressAndStatusIn, firstByToAddressAndStatusIn.getContractAddress(), address,BigInteger.TEN);
            }
        }
        return Result.success();
    }

    public Result syncBlock(TransferData req) {
        Long blockNum = req.getBlockNum();
        log.info("blockNum=={}", req.getBlockNum());
        if(blockNum==null){
            return Result.success("未找到该区块");
        }
        ApiInstance temp = getApiInstance(privatekey);//ApiFactory.getInstance(privatekey, testNet?null:endpoint);
        temp.setCallback(getCallback(temp));
        SyncTask syncTask = new SyncTask(blockNum, temp, transactionService, blockNum1 -> transactionService.addHandleBlock(blockNum1));
        syncTask.run();
        return Result.success();
    }

    public void syncMap(){
        log.info("syncMap={}",123123123);
        List<TronAddress> all = tronAddressRepository.findAll();
        log.info("syncMap={}",all.size());
        Map<String, TronAddress> collect = all.stream().collect(Collectors.toMap(TronAddress::getAddress, t -> t, (t1, t2) -> t1));
        TRON_ADDRESS_MAP.putAll(collect);
        List<History> histories = historyRepository.findAll();
        log.info("syncMap={}",histories.size());
        Map<String, Boolean> historyMap = histories.stream().collect(Collectors.toMap(History::getTxId, t -> true, (t1, t2) -> t1));
        WITHDRAW_MAP.putAll(historyMap);
    }

    public Result notify(TransferData req) {
        String txId = req.getTxId();
        Transaction firstByTxid = transactionRepository.findFirstByTxid(txId);
        String toAddress = firstByTxid.getToAddress();
        TronAddress firstByAddress = tronAddressRepository.findFirstByAddress(toAddress);
        Integer uid = firstByAddress.getUid();
        pushService.pushBalanceChangeByTransaction(firstByTxid,uid);
        return Result.success();
    }



}
