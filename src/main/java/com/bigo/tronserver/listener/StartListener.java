package com.bigo.tronserver.listener;

import com.bigo.tronserver.service.CheckService;
import com.bigo.tronserver.service.SendFeeService;
import com.bigo.tronserver.service.TransactionService;
import com.bigo.tronserver.service.TronService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
public class StartListener implements ApplicationListener<ContextRefreshedEvent> {
    @Resource
    TronService tronService;

    @Resource
    TransactionService transactionService;

    @Resource
    CheckService checkService;

    @Resource
    SendFeeService sendFeeService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        tronService.syncMap();
        transactionService.syncHandle();
        tronService.start();
        checkService.start();
        sendFeeService.start();
        tronService.startTransfer();
    }
}
