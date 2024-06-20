package com.bigo.tronserver.controller.request;

import com.bigo.tronserver.ReplaceUtils;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferData {
    BigDecimal price;
    String symbol;
    String address;
    Integer withdrawId;
    Long blockNum;
    String txId;

    public String getAddress() {
        return ReplaceUtils.replaceAll(address);
    }
}
