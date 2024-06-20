package com.bigo.tronserver.controller;

import com.bigo.tronserver.controller.request.TransferData;
import com.bigo.tronserver.event.PushData;
import com.bigo.tronserver.exception.TransferException;
import com.bigo.tronserver.model.Result;
import com.bigo.tronserver.service.TronService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tron.trident.core.exceptions.IllegalException;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping(value="/api/")
public class ThirdApiController {

    @Resource
    TronService tronService;

    @RequestMapping(value="address/{uid}")
    public Result address(@PathVariable("uid")Integer uid){
        return tronService.address(uid);
    }

    @RequestMapping(value="balance/{symbol}/{uid}")
    public Result address(@PathVariable("symbol")String symbol,@PathVariable("uid")Integer uid){
        return tronService.balance(symbol,uid);
    }

    @RequestMapping(value="notify")
    public String notify(@RequestBody PushData data){
        return tronService.notify(data);
    }

    @RequestMapping(value="transfer")
    public Result transfer(@RequestBody TransferData req) throws TransferException {
        return tronService.transfer(req);
    }

    @RequestMapping(value="transferTo")
    public Result transferTo(TransferData req) throws TransferException {
        return tronService.transferTo(req);
    }

    @RequestMapping(value="collect")
    public Result collect(@RequestBody TransferData req) throws TransferException {
        return tronService.collect(req);
    }

    @RequestMapping(value="collectByEnergy")
    public Result collectByEnergy(@RequestBody TransferData req) throws TransferException {
        return tronService.collect(req);
    }

    @RequestMapping(value="notifyPush")
    public Result notify(@RequestBody TransferData req) throws TransferException {
        return tronService.notify(req);
    }

    @RequestMapping(value="syncBlock")
    public Result syncBlock(@RequestBody TransferData req) throws TransferException {
        return tronService.syncBlock(req);
    }





}
