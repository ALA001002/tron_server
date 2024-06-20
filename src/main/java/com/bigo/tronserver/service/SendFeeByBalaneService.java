package com.bigo.tronserver.service;

import com.bigo.tronserver.config.TrxConfig;
import com.bigo.tronserver.dao.BalanceRepository;
import com.bigo.tronserver.dao.TransactionRepository;
import com.bigo.tronserver.entity.Balance;
import com.bigo.tronserver.entity.Transaction;
import com.bigo.tronserver.entity.TronAddress;
import com.bigo.tronserver.model.ApiInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class SendFeeByBalaneService extends Thread{

    @Resource
    BalanceRepository balanceRepository;

    @Resource
    TransactionRepository transactionRepository;

    @Resource
    TransferService transferService;

    @Resource
    TrxConfig trxConfig;

    @Resource
    CheckService checkService;

    @Override
    public void run() {
        super.run();
        int minTrx = trxConfig.getMinTrx();
        int minUsdt = trxConfig.getMinUsdt();
        log.info("SendFeeServiceExecute1 send fee start minTrx={}",minTrx);
        try {
            log.info("SendFeeServiceExecute1==instance start");
            ApiInstance instance = trxConfig.getFeeInstance();
            log.info("SendFeeServiceExecute1==instance");
//            log.info("SendFeeServiceExecute1 instance={}", JSON.toJSONString(instance));
            while (true) {
                try {
                    log.info("SendFeeServiceExecute1 = start fee");
                    long trxBalance = instance.queryBalance() / BigInteger.TEN.pow(6).longValue();
                    log.info("SendFeeServiceExecute1 send fee start minTrx={},balance={}", minTrx, trxBalance);
                    if (trxBalance >= minTrx) {
                        BigInteger multiply = BigInteger.valueOf(minUsdt).multiply(BigInteger.TEN.pow(6));
                        LocalDateTime end = LocalDateTime.now().minusHours(1);
                        List<Balance> balances = balanceRepository.findAllByBalanceGreaterThan(BigInteger.ZERO);
                        log.info("SendFeeServiceExecute transactions autoCollect={}", balances.size());
                        if (!CollectionUtils.isEmpty(balances)) {
                            for (Balance balance : balances) {
                                String address = balance.getAddress();
                                log.info("SendFeeServiceExecute sendFee service address={}", address);
                                transferService.sendFeeAndCollect(balance,multiply);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.info("error={}",e);
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    log.info("SendFeeService={}", e);
                    log.info("error={}",e);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            log.info("error={}",e);
        }
    }

}
