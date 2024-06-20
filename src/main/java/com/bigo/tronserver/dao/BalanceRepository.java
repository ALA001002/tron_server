package com.bigo.tronserver.dao;


import com.bigo.tronserver.entity.Balance;
import com.bigo.tronserver.entity.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * null
 *
 * <p>Date: Sat Oct 16 19:24:34 CST 2021</p>
 */
public interface BalanceRepository extends BaseRepository<Balance> {

    Balance findFirstByContractAddressAndAddress(String contractAddress,String address);
    Balance findFirstByAddress(String address);

    @Query("select A from Transaction A where  A.symbol='USDT' and coalesce(A.autoCollect,false)=false  and A.status in (3,5) and A.type=1 and A.createdAt<:endTime")
    List<Transaction> querySendFeeError(@Param("endTime")LocalDateTime endTime);

    List<Balance> findAllByBalanceGreaterThan(BigInteger balance);
}
