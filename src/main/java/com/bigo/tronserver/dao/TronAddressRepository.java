package com.bigo.tronserver.dao;


import com.bigo.tronserver.entity.TronAddress;

public interface TronAddressRepository extends BaseRepository<TronAddress>{

    TronAddress findFirstByUid(Integer uid);

    TronAddress findFirstByAddress(String address);
}
