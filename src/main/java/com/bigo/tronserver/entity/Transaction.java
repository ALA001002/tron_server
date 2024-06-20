package com.bigo.tronserver.entity;


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.*;


/**
 * null
 *
 * <p>Date: Sat Sep 25 23:32:17 CST 2021</p>
 */

@Table(name ="tron_transaction")
@Entity
@Data
public class Transaction  implements Serializable {


    private static final long serialVersionUID =  935398697677022314L;

    /**
     * null
     */
    @Column(name = "id" )
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * null
     */
    @Column(name = "created_at" )
    private java.time.LocalDateTime createdAt;

    /**
     * null
     */
    @Column(name = "updated_at" )
    private java.time.LocalDateTime updatedAt;

    /**
     * null
     */
    @Column(name = "txid" )
    private String txid;

    /**
     * null
     */
    @Column(name = "from_address" )
    private String fromAddress;

    /**
     * null
     */
    @Column(name = "to_address" )
    private String toAddress;

    /**
     * //trc20 status 0 待转手续费 1转手续费中 2手续费成功 4待归集 5归集失败 6归集成功
     */
    @Column(name = "status" )
    private Integer status;

    /**
     * null
     */
    @Column(name = "amount" )
    private BigDecimal amount;

    @Column(name = "origin_amount" )
    private BigInteger originAmount;

    @Column(name="type")
    private Byte type;

    @Column(name="symbol")
    private String symbol;

    @Column(name = "fee_tx_id")
    private String feeTxId;

    @Column(name="block_num")
    private Long blockNum;


    @Column(name="confirm_block")
    private Long confirmBlock;

    @Column(name="confirm_time")
    private LocalDateTime confirmTime;

    @Column(name="contract_address")
    private String contractAddress;

    @Column(name="collect_tx_id")
    private String collectTxId;

    @Column(name="collect_time")
    private LocalDateTime collectTime;

    @Column(name="errors")
    private String errors;

    @Column(name = "collect_errors")
    private String collectErrors;

    @Column(name="parent_tx_id")
    private String parentTxId;

    @Column(name="auto_collect")
    private Boolean autoCollect;
    @Column(name="auto_collect_time")
    private LocalDateTime autoCollectTime;

    @Column(name="score")
    private Boolean score;

    @Column(name="balance_flag")
    private Boolean balanceFlag;

    public Long getBlockNum() {
        return Optional.of(blockNum).orElse(0L);
    }

}
