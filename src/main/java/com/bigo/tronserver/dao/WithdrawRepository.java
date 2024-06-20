package com.bigo.tronserver.dao;

import com.bigo.tronserver.entity.TWithdraw;

import java.time.LocalDateTime;
import java.util.List;


/**
 * null
 *
 * <p>Date: Sat Sep 25 19:55:45 CST 2021</p>
 */
public interface WithdrawRepository extends BaseRepository<TWithdraw> {

    TWithdraw findFirstByToAddress(String toAddress);

    TWithdraw findFirstByTxId(String txId);

    List<TWithdraw> findAllByRealStatusAndVerifyTimeLessThanEqual(Byte realStatus,LocalDateTime endTme);
}
