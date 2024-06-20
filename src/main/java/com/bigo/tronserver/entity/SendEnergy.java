package com.bigo.tronserver.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name="send_energy")
public class SendEnergy {
//    "to_address": "TYzZ5hPMCz4twn4yyVd8EN9CTn9HuanTrx",
//    "price": 100,
//    "amount": 0.001,
//    "period": "1h",
//    "owner_address": "TZBNA795pFUQEDGXCr1nsy3paKXXXXXXXX",
//    "delegate_count": "10",
//    "delegate_tx_id": "eb8d6bd557c54a103aa2b670056202ac883301e0fefd56cc10c66d7d2e65e497",
//    "type": "ENERGY",
//    "status": "已发送",
//    "energy_expire_time": "2023-07-07 22:59:12"
    @Column(name = "id" )
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long rowId;

    @Column(name="txid")
    public String txid;

    @Column(name="energy_type")
    public Integer energyType;

    @Column(name="receive_address")
    public String receiveAddress;

    @Column(name="updated_at")
    public LocalDateTime updatedAt;

    @Column(name="to_address")
    public String to_address;

    @Column(name="price")
    public BigDecimal price;

    @Column(name="amount")
    public BigDecimal amount;

    @Column(name="period")
    public String period;

    @Column(name="owner_address")
    public String owner_address;

    @Column(name="delegate_count")
    public String delegate_count;

    @Column(name="delegate_tx_id")
    public String delegate_tx_id;

    @Column(name="type")
    public String type;

    @Column(name="status")
    public String status;

    @Column(name="energy_expire_time")
    public String energy_expire_time;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="code")
    private Integer code;

    @Column(name="net")
    private Long net;

    @Column(name="net_code")
    private Integer netCode;

    @Column(name="net_resp", columnDefinition = "text")
    private String netResp;

    @Column(name="energy")
    private Long energy;

    @Column(name="energy_code")
    private Integer energyCode;

    @Column(name="energy_resp", columnDefinition = "text")
    private String energyResp;

    @Column(name="origin_amount")
    BigInteger originAmount;

    @Column(name="contract_address")
    String contractAddress;

    @Column(name="delegate_id")
    String delegateId;

}
