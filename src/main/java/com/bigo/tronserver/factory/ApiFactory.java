package com.bigo.tronserver.factory;

import com.bigo.tronserver.model.ApiInstance;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
public class ApiFactory {
    private final static Map<String,ApiInstance> API_INSTANCE_MAP = new HashMap<>();
    public static ApiInstance getInstance(String hexPrivateKey,String endpoint){
        return getInstance(hexPrivateKey,endpoint,endpoint);
    }
    public static ApiInstance getInstance(String hexPrivateKey,String endpoint,String solidEndpoint){
        String key = String.format("%s:%s",hexPrivateKey,endpoint);
        ApiInstance apiInstance = API_INSTANCE_MAP.get(key);
        if(apiInstance==null){
            apiInstance = new ApiInstance();
            API_INSTANCE_MAP.put(key,apiInstance);
        }else{
            return apiInstance;
        }
        KeyPair keyPair = new KeyPair(hexPrivateKey);
        String hexAddress = keyPair.toBase58CheckAddress();
        apiInstance.setAddress(hexAddress);
        apiInstance.setHexAddress(keyPair.toHexAddress());
        apiInstance.setHexPrivateKey(hexPrivateKey);
        ApiWrapper apiWrapper;
        if (!StringUtils.isEmpty(endpoint)) {
            apiWrapper = new ApiWrapper(endpoint,solidEndpoint,hexPrivateKey);
//            apiWrapper = ApiWrapper.ofMainnet(hexPrivateKey, apiKey);
        } else {
            apiWrapper = ApiWrapper.ofShasta(hexPrivateKey);
        }
        apiInstance.setApiWrapper(apiWrapper);
        log.info("create instance hexPrivateKey={},address={},apiKey={},net={}", hexPrivateKey, hexAddress, endpoint, endpoint == null);
        return apiInstance;
    }

    public static void main(String[] args) throws IllegalException, UnsupportedEncodingException, InvalidProtocolBufferException {
//        KeyPair keyPair = KeyPair.generate();
//        String s = keyPair.toBase58CheckAddress();
//        System.out.println(keyPair.toPrivateKey());
        KeyPair keyPair = new KeyPair("306cdf10085dae2d30bebe20defd3bcb6fe72db01a8ecc72683e2938f2d3a3f7");
        System.out.println(keyPair.toBase58CheckAddress());
//        ApiInstance instance = ApiFactory.getInstance("17d563a8b6576a94a9c5701d03f3162a45ac38662a6871ac39f964d93096f42f", "101.44.35.240:50051");//"119.12.165.104:50051"
//        System.out.println();
//        ApiInstance instance2= ApiFactory.getInstance("1d414c1e039c4644a139fbee1997cf681b26fe2ebf61dace017175c4c8260c2a", "3.225.171.164:50051");//"119.12.165.104:50051"
//        System.out.println(instance.getNewBlock().getBlockHeader().getRawData().getNumber());
//        int i = instance.queryStatus("b679682131355b6af808d6d6d4c6fd5971f4a9a1530070d378f6ccc3c279ac6f");
//        System.out.println(i);
//        System.out.println(instance.getNewBlock().getBlockHeader().getRawData().getNumber());
//        System.out.println(instance2.getNewBlock().getBlockHeader().getRawData().getNumber());
//        Chain.Transaction transaction = instance.getApiWrapper().getTransactionById("b679682131355b6af808d6d6d4c6fd5971f4a9a1530070d378f6ccc3c279ac6f");
//        ApiWrapper apiWrapper = new ApiWrapper("119.12.165.104:50051","119.12.165.104:50051","1d414c1e039c4644a139fbee1997cf681b26fe2ebf61dace017175c4c8260c2a");
//        System.out.println(apiWrapper.getTransactionById("b679682131355b6af808d6d6d4c6fd5971f4a9a1530070d378f6ccc3c279ac6f"));
//        System.out.println(apiWrapper.getTransactionById("708a4fb35050f24f8400ab2941a01e70236b060fa9c097c8afef1d93e36ec3b7"));
//        System.out.println(instance.getNewBlock());

//        BigInteger balance = instance.queryTrc20Balance("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t", "TEHaQZGueaYay7etwatihumGf7ice3a7X6");
//        System.out.println(balance);
//        System.out.println(instance.calcTrxEnergy());
        //        System.out.println(instance.queryTrc20Balance("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",s));
//        Long aLong = instance.calcEnergyUsed("TQ8QsMWrNChyx3YnzvD1BWRBrGwkEu1Mhb", "TGp9jdZLfPH8MFzrq77foiGDy9MUuaYwpd", "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t", BigInteger.valueOf(3000000));
//        long calcTrxEnergy = instance.calcTrxEnergy();
//        System.out.println(calcTrxEnergy);
//        System.out.println(aLong);
//        System.out.println(aLong*calcTrxEnergy+25000);
//        System.out.println(keyPair.toBase58CheckAddress());
//        try {
//            instance.sendTrc20("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",keyPair.toBase58CheckAddress(),"TVJgPkk86stgtnZsZSVU5uNGQ3USR3qrJ2",BigInteger.valueOf(1000000));
//            instance.sendTrc20("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",keyPair.toBase58CheckAddress(),"TVJgPkk86stgtnZsZSVU5uNGQ3USR3qrJ2",BigInteger.valueOf(1000000));
//        } catch (TransferException e) {
//            e.printStackTrace();
//        }
//        long energyUsed = instance.calcEnergyUsed("TEHaQZGueaYay7etwatihumGf7ice3a7X6", "TYwKXyMb8Kg7TTMRURJ8vufzpi3mqQq1Ud", "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t", BigInteger.valueOf(100000));
//        long trxEnergy = instance.calcTrxEnergy();
//        System.out.println(energyUsed);
//        System.out.println(new Date());
//        Date last = new Date();
//        BigInteger bigInteger = instance.queryTrc20Balance("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t", "TYwKXyMb8Kg7TTMRURJ8vufzpi3mqQq1Ud");
//        System.out.println(bigInteger);
//        System.out.println(new Date().getTime()-last.getTime());
//        BigInteger amount = BigInteger.valueOf(10000);
//        Object o = instance.calcTrxEnergy();
//        System.out.println(o.getClass());
//        System.out.println(o);
//        Long aLong = instance.calcTrc20BandWidth("TVJgPkk86stgtnZsZSVU5uNGQ3USR3qrJ2", "TPCTwizUznWWv6mEb7EeZia1YWNPdyS8D7", "TUYNofocSmr8kPY8FnLGuJFzXUGHrpa1RV", amount);
//        System.out.println(aLong);
//        KeyPair keyPair = KeyPair.generate();
//        try {
//        BigInteger tVpwGcjADRwTzGD5S3mWSxFwKpJ9Nzegd2 = instance.queryTrc20Balance("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t","TVpwGcjADRwTzGD5S3mWSxFwKpJ9Nzegd2");
//        System.out.println(tVpwGcjADRwTzGD5S3mWSxFwKpJ9Nzegd2);
//            instance.sendTrc20("TUYNofocSmr8kPY8FnLGuJFzXUGHrpa1RV","TVJgPkk86stgtnZsZSVU5uNGQ3USR3qrJ2","TYwKXyMb8Kg7TTMRURJ8vufzpi3mqQq1Ud",BigInteger.valueOf(1010000));
//        } catch (TransferException e) {
//            e.printStackTrace();
//        }
//        int count = instance.queryBlocks("dbe14930b17952434b182e57aa373511bb9f7330ab9983f08feb7e883787b9fc");
//        System.out.println(count);
//        instance.calcTransaction("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t","TJ8KJwBwwKG6FSNjsLZuRW1zd6sL83tyhg","TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",BigInteger.valueOf(1000));
    }
}
