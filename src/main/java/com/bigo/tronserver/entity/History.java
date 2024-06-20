package com.bigo.tronserver.entity;


import java.io.Serializable;
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
 * <p>Date: Mon Oct 11 18:35:05 CST 2021</p>
 */

@Table(name ="withdraw_history")
@Entity
@Data
public class History  implements Serializable {


    private static final long serialVersionUID =  9174523195380659358L;

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
    @Column(name = "address" )
    private String address;

    /**
     * null
     */
    @Column(name = "create_time" )
    private java.time.LocalDateTime createTime;

    /**
     * null
     */
    @Column(name = "tx_id" )
    private String txId;

    @Column(name="errors",length = 2000)
    private String errors;


}
