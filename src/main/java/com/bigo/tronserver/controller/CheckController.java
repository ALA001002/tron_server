package com.bigo.tronserver.controller;

import com.bigo.tronserver.model.Result;
import com.bigo.tronserver.service.CheckBalanceService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tron.trident.core.exceptions.IllegalException;

import javax.annotation.Resource;

@RequestMapping(value="/check/")
@RestController
public class CheckController {

    @Resource
    CheckBalanceService checkBalanceService;

    @RequestMapping(value="start")
    public Result check(){
        checkBalanceService.checkBalance();
        return Result.success();
    }

    @RequestMapping(value="balance")
    public Result balance(){
        return Result.success(checkBalanceService.queryBalances());
    }


    @RequestMapping(value="sendFeeAndCollect/{address}")
    public Result sendFeeAndCollect(@PathVariable("address") String address){
        checkBalanceService.sendFeeAndCollect(address);
        return Result.success();
    }

    @RequestMapping(value="startCollect")
    public Result startCollect(){
        checkBalanceService.startCollect();
        return Result.success();
    }


    @RequestMapping(value="startCollectMin/{min}")
    public Result startCollect(@PathVariable("min")Integer min){
        checkBalanceService.startCollect(min);
        return Result.success();
    }

    @RequestMapping(value="checkStatus/{txId}")
    public Result startCollect(@PathVariable("txId")String txId) throws IllegalException {
        return Result.success(checkBalanceService.checkStatus(txId));
    }

    @RequestMapping(value="checkAccount/{address}")
    public Result checkAccount(@PathVariable("address")String address) throws IllegalException {
        return checkBalanceService.checkAccount((address));
    }

}
