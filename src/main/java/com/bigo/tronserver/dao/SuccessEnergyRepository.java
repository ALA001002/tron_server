package com.bigo.tronserver.dao;

import com.bigo.tronserver.entity.SuccessEnergy;

public interface SuccessEnergyRepository extends BaseRepository<SuccessEnergy>{
    SuccessEnergy findFirstByTxid(String txid);

}
