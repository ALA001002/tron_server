package com.bigo.tronserver.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@Builder
public class OriginTransaction {
    String txId;
    String contractAddress;
    String fromAddress;
    String toAddress;
    BigDecimal amount;
    Long timestamp;
    Boolean trc20;
    String symbol;
    BigInteger originAmount;
    Long blockNum;
    Boolean status;
}
