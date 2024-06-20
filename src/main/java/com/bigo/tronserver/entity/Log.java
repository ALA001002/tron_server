package com.bigo.tronserver.entity;


import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;




/**
 * null
 *
 * <p>Date: Sat Sep 25 23:30:34 CST 2021</p>
 */

@Table(name ="sync_log")
@Entity
@Data
public class Log  implements Serializable {


    private static final long serialVersionUID =  2149195528928878590L;

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
    @Column(name = "block_num" )
    private Long blockNum;

    /**
     * null
     */
    @Column(name = "transaction_size" )
    private Integer transactionSize;

    @Column(name="test")
    private Boolean test;


}
