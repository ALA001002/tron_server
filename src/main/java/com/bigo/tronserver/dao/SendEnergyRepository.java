package com.bigo.tronserver.dao;

import com.bigo.tronserver.entity.SendEnergy;

public interface SendEnergyRepository extends BaseRepository<SendEnergy>{
    SendEnergy findFirstByTxid(String txid);
    SendEnergy findFirstByDelegateId(String txid);

}
