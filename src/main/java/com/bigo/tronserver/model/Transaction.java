package com.bigo.tronserver.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Transaction {

    String fromAddress;
    String toAddress;
    BigDecimal amount;
    String coin;
}
