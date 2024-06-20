package com.bigo.tronserver.dao;

import com.bigo.tronserver.entity.History;


/**
 * null
 *
 * <p>Date: Mon Oct 11 18:35:05 CST 2021</p>
 */
public interface HistoryRepository extends BaseRepository<History> {
    History findFirstByTxId(String txId);
}
