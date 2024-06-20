package com.bigo.tronserver.entity;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * null
 *
 * <p>Date: Tue Sep 28 22:16:53 CST 2021</p>
 */

@Table(name ="push_log")
@Entity
@Data
public class PushLog implements Serializable {


    private static final long serialVersionUID =  6029571752160474464L;

    /**
     * null
     */
    @Column(name = "row_id" )
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rowId;

    /**
     * null
     */
    @Column(name = "push_data",length = 1024)
    private String pushData;

    /**
     * null
     */
    @Column(name = "success" )
    private Byte success;

    @Column(name="create_time")
    private LocalDateTime createTime;
    @Column(name="result")
    private String result;

    @Column(name="uid")
    private Integer uid;

    @Column(name="address")
    private String address;


}
