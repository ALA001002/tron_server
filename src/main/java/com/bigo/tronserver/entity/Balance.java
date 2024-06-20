package com.bigo.tronserver.entity;


import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
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
 * <p>Date: Sat Oct 16 19:24:34 CST 2021</p>
 */

@Table(name ="tron_balance")
@Entity
@Data
public class Balance  implements Serializable {


    private static final long serialVersionUID =  5077890197875689076L;

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
    @Column(name = "create_time" )
    private java.time.LocalDateTime createTime;
    @Column(name = "update_time" )
    private java.time.LocalDateTime updateTime;

    /**
     * null
     */
    @Column(name = "contract_address" )
    private String contractAddress;

    @Column(name = "balance" )
    private BigInteger balance;

    @Column(name = "trx_balance" )
    private BigInteger trxBalance;

    @Column(name="address")
    private String address;

    @Column(name="last_tx_id")
    private String lastTxId;

    @Column(name="tx_id")
    private String txId;

    @Column(name="fee_tx_id")
    private String feeTxId;

    @Column(name="status")
    private Integer status;

    @Column(name="errors")
    private String errors;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;


}
