package com.bigo.tronserver.dao;

import com.bigo.tronserver.entity.Log;


/**
 * null
 *
 * <p>Date: Sat Sep 25 23:30:34 CST 2021</p>
 */
public interface LogRepository extends BaseRepository<Log> {
    Log findFirstByTestOrderByBlockNumDesc(Boolean test);
}
