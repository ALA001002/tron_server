package com.bigo.tronserver.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushData {
    String txId;
    String method;
    Integer uid;
    BigDecimal balance;
    Byte type;
    String symbol;
    String address;
    Boolean test;
    String fromAddress;
}
