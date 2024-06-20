package com.bigo.tronserver.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FrozenData {
    String to_address;
    BigDecimal price;
    BigDecimal amount;
    String period;
    String owner_address;
    String delegate_count;
    String delegate_tx_id;
    String type;
    String status;
    String energy_expire_time;
}
