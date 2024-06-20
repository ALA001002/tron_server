package com.bigo.tronserver.entity;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * null
 *
 * <p>Date: Mon Oct 11 18:35:05 CST 2021</p>
 */

@Table(name ="handle_block")
@Entity
@Data
public class HandleBlock implements Serializable {


    private static final long serialVersionUID =  9174523195380659359L;

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
    private Long blockNum;


    @Column(name="create_time")
    private LocalDateTime createTime;

}
