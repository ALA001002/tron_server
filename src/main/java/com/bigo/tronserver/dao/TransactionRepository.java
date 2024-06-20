package com.bigo.tronserver.dao;

import com.bigo.tronserver.entity.Transaction;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;


/**
 * null
 *
 * <p>Date: Sat Sep 25 23:32:17 CST 2021</p>
 */
public interface TransactionRepository extends BaseRepository<Transaction> {
    List<Transaction> findAllByStatusAndType(Integer status,Byte type);

    @Modifying
    @Transactional
    @Query("update Transaction set status=6,collectTxId=:collectTxId,collectTime=:collectTime where status=4 " +
            " and toAddress=:toAddress and symbol=:symbol and createdAt<=:createdAt")
    void updateOldTransaction(@Param("toAddress")String toAddress,
                              @Param("collectTxId")String collectTxId,
                              @Param("symbol")String symbol,
                              @Param("collectTime")LocalDateTime collectTime,
                              @Param("createdAt")LocalDateTime createdAt);

    List<Transaction> findAllByTypeAndStatus(Byte type,Integer status);
    Transaction findFirstByTxid(String txId);
    Transaction findFirstByFeeTxId(String feeTxId);
    Transaction findFirstByCollectTxId(String collectTxId);
    Transaction findFirstByToAddressAndStatusIn(String toAddress,List<Byte> status);
}
