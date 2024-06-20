package com.bigo.tronserver.config;

import com.bigo.tronserver.factory.ApiFactory;
import com.bigo.tronserver.model.ApiInstance;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class TrxConfig {
    @Value("${api.privatekey}")
    String privateKey;

    @Value("${tron.endpoint:''}")
    String endpoint;

    @Value("${tron.solidityEndPoint:''}")
    String solidityEndPoint;

    @Value("${collect.contractAddress}")
    String collectContractAddress;

    @Value("${wallet.feePrivateKey}")
    String feePrivateKey;

    @Value("${wallet.energyPrivateKey}")
    String energyPrivateKey;

    @Value("${wallet.minTrx:5}")
    int minTrx;

    @Value("${wallet.minUsdt:1}")
    int minUsdt;

    public ApiInstance getInstance(){
        return ApiFactory.getInstance(privateKey,endpoint,solidityEndPoint);
    }

    @Bean
    public ApiInstance energyApiInstance(){
        ApiInstance instance = ApiFactory.getInstance(energyPrivateKey, endpoint,solidityEndPoint);
        return instance;
    }

    public ApiInstance getFeeInstance(){
        return ApiFactory.getInstance(feePrivateKey,endpoint);
    }
}
