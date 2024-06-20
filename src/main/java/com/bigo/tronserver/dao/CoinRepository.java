package com.bigo.tronserver.dao;

import com.bigo.tronserver.entity.TronCoin;


/**
 * null
 *
 * <p>Date: Sat Sep 25 19:55:45 CST 2021</p>
 */
public interface CoinRepository extends BaseRepository<TronCoin> {
    TronCoin findFirstByContractAddressAndTest(String contractAddress,boolean test);

    TronCoin findFirstBySymbolAndTest(String symbol,boolean test);
}
