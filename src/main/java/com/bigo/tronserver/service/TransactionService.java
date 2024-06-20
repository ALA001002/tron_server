package com.bigo.tronserver.service;

import com.bigo.tronserver.dao.*;
import com.bigo.tronserver.entity.*;
import com.bigo.tronserver.exception.TransferException;
import com.bigo.tronserver.factory.ApiFactory;
import com.bigo.tronserver.model.ApiInstance;
import com.bigo.tronserver.model.OriginTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionService {

    @Resource
    TransactionRepository transactionRepository;

    @Resource
    TronAddressRepository tronAddressRepository;

    @Resource
    CheckBalanceService checkBalanceService;

    @Resource
    LogRepository logRepository;

    @Resource
    AddressService addressService;

    @Resource
    TransferService transferService;

    @Resource
    TronService tronService;

    @Value("${wallet.minUsdt:0}")
    BigDecimal minUsdt;

    @Value("${wallet.collectAddress}")
    String collectAddress;

    @Resource
    PushService pushService;

    @Resource
    HandleBlockRepository handleBlockRepository;

    @Value("${collect.contractAddress}")
    String collectContractAddress;

    @Resource(name = "energyApiInstance")
    ApiInstance energyApiInstance;

    @Resource
    WithdrawRepository withdrawRepository;

    private final static Map<Long,Boolean> HANDLE_MAP = new HashMap<>();

    public void createLog(long blockNum, int size,boolean testNet,LocalDateTime createTime){
        Log log = new Log();
        LocalDateTime now = LocalDateTime.now();
        log.setTransactionSize(size);
        log.setTest(testNet);
        log.setBlockNum(blockNum);
        log.setCreatedAt(createTime);
        log.setUpdatedAt(now);
        logRepository.save(log);
    }
    public void compare(OriginTransaction originTransaction,boolean testNet) {
        compare(originTransaction,testNet,true);
    }
    public void compare(OriginTransaction originTransaction,boolean testNet,boolean status) {
        try {
            log.info("OriginTransaction compare={}",originTransaction);
            if(status) {
                addressService.compareAddress(originTransaction, testNet);
            }
            updateWithdraw(originTransaction,status);
        }catch (Exception e){
            log.info("compare transaction={},={}",originTransaction,e);
        }
    }

    public void send(){

    }

    @Scheduled(fixedDelay = 10*1000)
    public void queryConfirm(){
//        List<Transaction> transactions = transactionRepository.findAllByStatusAndType(1,(byte)1);
//        log.info("queryConfirm={}",transactions.size());
//        if(!CollectionUtils.isEmpty(transactions)){
//            LocalDateTime now = LocalDateTime.now();
//            ApiInstance instance = tronService.getApiInstance();
//            try {
//                Chain.Block newBlock = instance.getNewBlock();
//                long number = newBlock.getBlockHeader().getRawData().getNumber();
//                for (Transaction transaction:transactions){
//                    long blockNum = transaction.getBlockNum();
//                    if(blockNum+19<=number){
//                        transaction.setStatus(2);
//                        transaction.setConfirmTime(now);
//                        transactionRepository.save(transaction);
//                    }
//                    transaction.setConfirmBlock(number-blockNum);
//                    transaction.setUpdatedAt(now);
//                    transactionRepository.save(transaction);
//                }
//            } catch (IllegalException e) {
//                e.printStackTrace();
//            }
//        }
        List<Transaction> waitTransactions = transactionRepository.findAllByTypeAndStatus((byte)1,2);
        LocalDateTime current = LocalDateTime.now();
//        if(!CollectionUtils.isEmpty(waitTransactions)){
//            for (Transaction transaction:waitTransactions) {
//                String toAddress = transaction.getToAddress();
//                BigInteger originAmount = transaction.getOriginAmount();
//                TronAddress tronAddress = tronAddressRepository.findFirstByAddress(toAddress);
//                int status = 4;
//                String errors = null;
//                if(tronAddress==null){
//                    status=5;
//                    errors=toAddress+" private key not exist";
//                }else {
//                    ApiInstance tronInstance = tronService.getApiInstance(tronAddress.getHexPrivateKey());//ApiFactory.getInstance(tronAddress.getHexPrivateKey(), tronService.getApiKey());
//                    String contractAddress = transaction.getContractAddress();
//                    BigInteger amount = BigInteger.ZERO;
//                    try {
//                        amount = tronInstance.queryTrc20Balance(contractAddress, toAddress);
//                    }catch (Exception e){
//                        log.error("IndexOutOfBoundsException={},contractAddress={},toAddress={}",contractAddress,toAddress,e);
//                    }
//                    if(amount==null){
//                        amount = originAmount;
//                    }
//                    Chain.Transaction result;
//                    try {
//                        if (!StringUtils.isEmpty(contractAddress) && collectContractAddress.equals(contractAddress)) {
//                            try {
//                                result = tronInstance.sendTrc20(contractAddress, toAddress, collectAddress, amount);
//                                status=4;
//                            } catch (TransferException e) {
//                                errors = e.getMessage();
//                                result = e.getTransaction();
//                                status = 5;
//                                log.info("sendTrc20={}", e);
//                            }
//                        } else {
//                            result = tronInstance.sendTrx(toAddress, collectAddress, originAmount);
//                        }
//                        byte[] bytes = ApiWrapper.calculateTransactionHash(result);
//                        String txId = ApiWrapper.toHex(bytes);
//                        transaction.setCollectTxId(txId);
//                        transaction.setCollectTime(current);
//                        transaction.setStatus(6);
//                    } catch (IllegalException e) {
//                        log.error("queryConfirm={}", e);
//                        transaction.setErrors(e.toString());
//                    }
//                }
//                transaction.setCollectTime(current);
//                transaction.setCollectErrors(errors);
//                transaction.setStatus(status);
//                transactionRepository.save(transaction);
//            }
//        }
        checkWithdrawStatus();
    }

    public void checkWithdrawStatus(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.withHour(0).withMinute(0).minusMinutes(10);
        LocalDateTime endTime = now.minusMinutes(10);
        List<TWithdraw> withdraws = withdrawRepository.findAllByRealStatusAndVerifyTimeLessThanEqual((byte) 0, endTime);
        log.info("checkWithdrawStatus={}",withdraws.size());
        for (TWithdraw withdraw : withdraws) {
            String txId = withdraw.getTxId();
            byte status = checkBalanceService.checkStatusValue(txId);
            if(status!=0){
                withdraw.setRealStatus(status);
                withdraw.setConfirmTime(LocalDateTime.now());
                withdrawRepository.save(withdraw);
            }
        }
    }

    public void updateOldAddress(String fromAddress,String txId,LocalDateTime createdAt){
        BigInteger balance = checkBalanceService.queryUSDTBalance(fromAddress);
        BigInteger minBalance = BigInteger.ONE.multiply(BigInteger.TEN.pow(6));
        log.info("updateOldAddress={},txid={},balance={},minBalance={}",fromAddress,txId,balance,minBalance);
        if(balance.compareTo(minBalance)>=0){
            return;
        }
        log.info("updateOldAddress={},txid={}",fromAddress,txId);
        transactionRepository.updateOldTransaction(fromAddress,txId,"USDT",LocalDateTime.now(),createdAt);
    }

    //status trc=0
    //trc20 status 0 待转手续费 1转手续费中 2手续费成功 4待归集 5归集失败 6归集成功
    public void addFromTransaction(OriginTransaction originTransaction, TronAddress tronAddress, boolean flag,boolean testNet) {
        String contractAddress = originTransaction.getContractAddress();
        String symbol = originTransaction.getSymbol();
        Transaction transaction = new Transaction();
        LocalDateTime now = LocalDateTime.now();
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);
        boolean trc20 = Optional.ofNullable(originTransaction.getTrc20()).orElse(false);
        int status;
        if(trc20){
            status = 2;
        }else{
            status = 0;
        }
        transaction.setStatus(status);
        String fromAddress = originTransaction.getFromAddress();
        transaction.setFromAddress(fromAddress);
        transaction.setToAddress(originTransaction.getToAddress());
        byte type = flag?(byte)1:0;
        transaction.setType(type);
        transaction.setOriginAmount(originTransaction.getOriginAmount());
        transaction.setBlockNum(originTransaction.getBlockNum());
        transaction.setAmount(originTransaction.getAmount());
        String txId = originTransaction.getTxId();
        transaction.setTxid(txId);
        transaction.setSymbol(originTransaction.getSymbol());
        transaction.setContractAddress(originTransaction.getContractAddress());
        Transaction feeTransaction = transactionRepository.findFirstByFeeTxId(txId);
        if(feeTransaction!=null){
            transaction.setParentTxId(feeTransaction.getTxid());
            transaction.setStatus(6);
        }
        Transaction parentTransaction = transactionRepository.findFirstByCollectTxId(txId);
        if(parentTransaction!=null){
            transaction.setParentTxId(parentTransaction.getTxid());
            transaction.setStatus(6);
        }
        LocalDateTime createdAt = transaction.getCreatedAt();
        int updateStatus = Optional.ofNullable(transaction.getStatus()).orElse(0);
        if(updateStatus==6){
            updateOldAddress(fromAddress,txId,createdAt);
        }
        transactionRepository.save(transaction);
        if("TRX".equals(symbol)){
            String toAddress = transaction.getToAddress();
            log.info("active toAddress={}",toAddress);
            TronAddress temp = tronAddressRepository.findFirstByAddress(toAddress);
            if(temp.getActive()==null || !temp.getActive()) {
                temp.setActive(true);
                temp.setActiveTime(now);
                tronAddressRepository.save(temp);
            }
        }
        if(flag && !"TRX".equals(symbol)){
            String toAddress = originTransaction.getToAddress();
            TronAddress firstByAddress = tronAddressRepository.findFirstByAddress(toAddress);
            if(firstByAddress!=null) {
                Integer uid = firstByAddress.getUid();
                BigDecimal amount = originTransaction.getAmount();
                if(amount.compareTo(BigDecimal.ONE)>=0) {
                    log.info("pushBalanceChange uid={},amount={}",uid,amount);
                    pushService.pushBalanceChange(txId, uid, amount, type, symbol, testNet, originTransaction.getFromAddress(), originTransaction.getToAddress());
                }else{
                    log.info("pushBalanceChange uid={},amount={},filter",uid,amount);
                }
            }
            log.info("contractAddress={},symbol={}",contractAddress,symbol);
            BigDecimal amount = Optional.ofNullable(originTransaction.getAmount()).orElse(BigDecimal.ZERO);
            BigDecimal minUsdtValue = Optional.ofNullable(minUsdt).orElse(BigDecimal.ZERO);
            log.info("amount={},minUsdtValue={}",amount,minUsdtValue);
            if(collectContractAddress.equals(contractAddress) && amount.compareTo(minUsdtValue)>=0) {//必须指定归集合约
                transferService.sendFee(transaction, originTransaction.getContractAddress(), toAddress, tronAddress);
            }
        }
    }

    public void addTransaction(OriginTransaction originTransaction, String txId) {
        String symbol = originTransaction.getSymbol();
        Transaction transaction = new Transaction();
        LocalDateTime now = LocalDateTime.now();
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);
        transaction.setStatus(6);
        transaction.setFromAddress(originTransaction.getFromAddress());
        transaction.setToAddress(originTransaction.getToAddress());
        transaction.setType((byte)3);
        transaction.setOriginAmount(originTransaction.getOriginAmount());
        transaction.setBlockNum(originTransaction.getBlockNum());
        transaction.setAmount(originTransaction.getAmount());
        transaction.setTxid(txId);
        transaction.setSymbol(symbol);
        transaction.setContractAddress(originTransaction.getContractAddress());
        transactionRepository.save(transaction);
    }

    public void syncHandle(){
        log.info("syncHandle");
        List<HandleBlock> all = handleBlockRepository.findAll();
        Map<Long, Boolean> collect = all.stream().collect(Collectors.toMap(HandleBlock::getBlockNum, t -> true, (t1, t2) -> t1));
        HANDLE_MAP.putAll(collect);
    }

    public boolean hasSyncBlock(Long blockNum){
        return HANDLE_MAP.get(blockNum)!=null;
    }

    public void addHandleBlock(Long blockNum) {
        log.info("addHandleBlock={}",blockNum);
        HANDLE_MAP.put(blockNum,true);
        HandleBlock handleBlock = new HandleBlock();
        handleBlock.setBlockNum(blockNum);
        handleBlock.setCreateTime(LocalDateTime.now());
        handleBlockRepository.save(handleBlock);
    }

    /**
     * -1 未上链  1提现成功  2提现失败  0 提现中
     * @param originTransaction
     * @param status
     */
    public void updateWithdraw(OriginTransaction originTransaction,boolean status){
        String txId = originTransaction.getTxId();
        TWithdraw firstByTxId = withdrawRepository.findFirstByTxId(txId);
        if(firstByTxId==null){
            return;
        }
        firstByTxId.setRealStatus((byte) (status?1:2));
        firstByTxId.setConfirmTime(LocalDateTime.now());
        withdrawRepository.save(firstByTxId);
    }

}
