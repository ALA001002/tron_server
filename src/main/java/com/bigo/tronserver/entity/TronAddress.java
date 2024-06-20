package com.bigo.tronserver.entity;


import java.io.Serializable;
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
 * <p>Date: Wed Sep 22 23:15:31 CST 2021</p>
 */

@Table(name ="tron_address")
@Entity
@Data
public class TronAddress implements Serializable {


    private static final long serialVersionUID =  1430979604572040903L;

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
    @Column(name = "create_at" )
    private java.time.LocalDateTime createAt;

    /**
     * 私钥
     */
    @Column(name = "hex_private_key" )
    private String hexPrivateKey;

    /**
     * 地址
     */
    @Column(name = "address" )
    private String address;
    /**
     * 用户id
     */
    @Column(name = "uid" )
    private Integer uid;

    @Column(name="active")
    private Boolean active;

    @Column(name="active_time")
    private LocalDateTime activeTime;


}
