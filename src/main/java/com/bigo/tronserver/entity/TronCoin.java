package com.bigo.tronserver.entity;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;

@Table(name ="tron_coin")
@Entity
@Data
public class TronCoin  implements Serializable {

    private static final long serialVersionUID =  8610730223633318395L;

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
    @Column(name = "created_time" )
    private java.time.LocalDateTime createdTime;

    /**
     * null
     */
    @Column(name = "updated_time" )
    private java.time.LocalDateTime updatedTime;

    /**
     * null
     */
    @Column(name = "del_flag" )
    private String delFlag;

    /**
     * null
     */
    @Column(name = "symbol" )
    private String symbol;

    /**
     * null
     */
    @Column(name = "name" )
    private String name;

    /**
     * null
     */
    @Column(name = "contract_address" )
    private String contractAddress;

    @Column(name = "test")
    private Boolean test;

    @Column(name = "decimals")
    private BigInteger decimals;



}
