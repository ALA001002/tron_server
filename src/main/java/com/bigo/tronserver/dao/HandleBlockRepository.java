package com.bigo.tronserver.dao;

import com.bigo.tronserver.entity.HandleBlock;

public interface HandleBlockRepository extends BaseRepository<HandleBlock>{

    HandleBlock findFirstByBlockNum(Integer blockNum);
}
