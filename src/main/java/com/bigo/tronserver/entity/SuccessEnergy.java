package com.bigo.tronserver.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name="success_energy")
public class SuccessEnergy {
    @Column(name = "id" )
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long rowId;

    @Column(name="txid")
    public String txid;

    @Column(name="updated_at")
    public LocalDateTime updatedAt;
}
