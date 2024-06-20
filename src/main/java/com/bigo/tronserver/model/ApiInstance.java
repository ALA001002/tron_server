package com.bigo.tronserver.model;

import com.bigo.tronserver.entity.TronCoin;
import com.bigo.tronserver.exception.TransferException;
import com.bigo.tronserver.factory.ApiFactory;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.transaction.TransactionBuilder;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Common;
import org.tron.trident.proto.Contract;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Base58Check;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Data
@Slf4j
public class ApiInstance {
    public interface Callback {
        void onSyncSuccess(long blockNum, int size,LocalDateTime now);

        void onTrc20Transaction(OriginTransaction originTransaction);

        TronCoin onCoin(String contractAddress);

        void onTronTransaction(OriginTransaction originTransaction);

        void onFail(OriginTransaction originTransaction);

        void onDelegate(String txid);
    }

    String address;
    String privateKey;
    String hexPrivateKey;
    String hexAddress;
    ApiWrapper apiWrapper;
    Callback callback;
    long blockNum = 18594677;


    public Response.Account getAccount(String address) {
        return apiWrapper.getAccount(address);
    }

    public ByteString full(ByteString other) {
        ByteString byteString = ByteString.copyFrom(new byte[]{65});
        if (other.size() == 32) {
            log.info("prefix={}", ApiWrapper.toHex(other));
            other = other.substring(12);
            log.info("after={}", ApiWrapper.toHex(other));
        }
        ByteString result = byteString.concat(other);
        log.info("result={}", result);
        return result;
    }

    public void parseTrc20Contract(String txId, Long timestamp,Long blockNum) throws IllegalException {
        Response.TransactionInfo transactionInfo = apiWrapper.getTransactionInfoById(txId);
        String contractAddress;
        String fromAddress;
        String toAddress;
        BigInteger amount = BigInteger.ZERO;
        List<Response.TransactionInfo.Log> logList = transactionInfo.getLogList();
        Chain.Transaction.Result.contractResult result = transactionInfo.getReceipt().getResult();
        if (result != Chain.Transaction.Result.contractResult.SUCCESS) {
            log.error("result fail txId={} result={}", txId, result);
            if (callback != null) {
                OriginTransaction originTransaction = OriginTransaction.builder()
                        .trc20(true)
                        .status(false)
                        .txId(txId)
                        .originAmount(amount)
                        .blockNum(blockNum)
                        .timestamp(timestamp).build();
                callback.onFail(originTransaction);//txId, contractAddress, fromAddress, toAddress, balance,timestamp);
            }
            return;
        }
        for (Response.TransactionInfo.Log item : logList) {
            List<ByteString> topicsList = item.getTopicsList();
            if(topicsList.size()<=0){
                continue;
            }
            ByteString bytes = topicsList.get(0);
            String method = ApiWrapper.toHex(bytes);
            if (!"ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef".equals(method)) {
                log.error("error txId={}", txId);
                continue;
            }
            if (topicsList.size() == 3) {
                contractAddress = Base58Check.bytesToBase58(full(item.getAddress()).toByteArray());
                fromAddress = Base58Check.bytesToBase58(full(topicsList.get(1)).toByteArray());
                toAddress = Base58Check.bytesToBase58(full(topicsList.get(2)).toByteArray());
                ByteString data = item.getData();
                log.info("amount={}", amount);
                try {
                    amount = new BigInteger(ApiWrapper.toHex(data), 16);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                TronCoin tronCoin = null;
//                if(callback!=null){
//                    tronCoin = callback.onCoin(contractAddress);
//                }
//                if(tronCoin==null) {
//                    continue;
//                }
                int power = 6;
                String symbol = null;
                if("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t".equals(contractAddress)){
                    symbol = "USDT";
                }
                log.info("power={}",power);
                BigDecimal balance = BigDecimal.valueOf(amount.longValue()).divide(BigDecimal.TEN.pow(power));
                if (callback != null) {
                    OriginTransaction originTransaction = OriginTransaction.builder()
                            .contractAddress(contractAddress)
                            .trc20(true)
                            .status(true)
                            .fromAddress(fromAddress)
                            .toAddress(toAddress)
                            .txId(txId)
                            .originAmount(amount)
                            .amount(balance)
                            .blockNum(blockNum)
                            .symbol(symbol)
                            .timestamp(timestamp).build();
                    callback.onTrc20Transaction(originTransaction);//txId, contractAddress, fromAddress, toAddress, balance,timestamp);
                }
                log.info("parseTrc20Contract txId={},contract_address={},fromAddress={},toAddress={},amount={},timestamp={}", txId, contractAddress, fromAddress, toAddress, balance, timestamp);
            }
        }
        return;
    }

    public Contract.TriggerSmartContract unpack(Chain.Transaction.Contract contract) throws InvalidProtocolBufferException {
        Contract.TriggerSmartContract triggerSmartContract = contract.getParameter().unpack(Contract.TriggerSmartContract.class);
        return triggerSmartContract;
    }

    public Contract.TransferContract unpackTransfer(Chain.Transaction.Contract contract) throws InvalidProtocolBufferException {
        Contract.TransferContract transferContract = contract.getParameter().unpack(Contract.TransferContract.class);
        return transferContract;
    }

    public void delegateResourceContract(String txid) {
        if(callback!=null) {
            callback.onDelegate(txid);
        }
    }


    public void parseContract(Chain.Transaction.Contract contract, String txId, long timestamp,long blockNum) throws InvalidProtocolBufferException {
        Contract.TransferContract transferContract = unpackTransfer(contract);
        long amount = transferContract.getAmount();
        String ownerAddress = Base58Check.bytesToBase58(transferContract.getOwnerAddress().toByteArray());
        String toAddress = Base58Check.bytesToBase58(transferContract.getToAddress().toByteArray());
        BigDecimal balance = BigDecimal.valueOf(amount);
        balance = balance.divide(BigDecimal.TEN.pow(6), 6);
        if (callback != null) {
            OriginTransaction originTransaction = OriginTransaction.builder()
                    .trc20(false).symbol("TRX")
                    .fromAddress(ownerAddress)
                    .toAddress(toAddress)
                    .blockNum(blockNum)
                    .txId(txId)
                    .originAmount(BigInteger.valueOf(amount))
                    .amount(balance).timestamp(timestamp).build();
            callback.onTronTransaction(originTransaction);
        }
        log.info("transfer txId={},owerAddress={},toAddress={},amount={},timestamp={}", txId, ownerAddress, toAddress, balance, timestamp);
    }

    public long sync(long blockNum) throws IllegalException, InvalidProtocolBufferException {
//        Chain.Block block = null;//apiWrapper.getBlockByNum(blockNum);

        Response.BlockExtention blockByNum = apiWrapper.getBlockByNum(blockNum);
        Chain.Block block = apiWrapper.getBlockById(ApiWrapper.toHex(blockByNum.getBlockid()));
        return parse(block);
    }

    public Byte queryStatus(String txId) {
        Response.TransactionInfo transactionInfo = null;
        try {
            transactionInfo = apiWrapper.getTransactionInfoById(txId);
            Chain.Transaction.Result.contractResult result = transactionInfo.getReceipt().getResult();
            byte status = (byte) (result != Chain.Transaction.Result.contractResult.SUCCESS?1:2);
            return status;
        } catch (IllegalException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public long parse(Chain.Block block) throws InvalidProtocolBufferException, IllegalException {
        long number = block.getBlockHeader().getRawData().getNumber();
        log.info("parse block={}", number);
        List<Chain.Transaction> transactionsList = block.getTransactionsList();
        for (Chain.Transaction transaction : transactionsList) {
            log.info("transaction txId={} size={}",number,transaction.getSerializedSize());
            Chain.Transaction.Result result = transaction.getRetList().get(0);
            long timestamp = transaction.getRawData().getTimestamp();
            Chain.Transaction.Result.code ret = result.getRet();
            if (ret == Chain.Transaction.Result.code.SUCESS) {
                List<Chain.Transaction.Contract> contractList = transaction.getRawData().getContractList();
                for (Chain.Transaction.Contract contract : contractList) {
                    byte[] bytes = ApiWrapper.calculateTransactionHash(transaction);
                    String txId = ApiWrapper.toHex(bytes);
                    if(contract.getType() == Chain.Transaction.Contract.ContractType.DelegateResourceContract){
                        log.info("DelegateResourceContract={}",txId);
                        delegateResourceContract(txId);
                    }
                    if (contract.getType() == Chain.Transaction.Contract.ContractType.TransferContract) {
                        log.info("TransferContract={}", txId);
                        parseContract(contract, txId, timestamp,number);
                    }
                    if (contract.getType() == Chain.Transaction.Contract.ContractType.TransferAssetContract) {
                        log.info("TransferAssetContract={}", txId);
                    }
                    if (contract.getType() == Chain.Transaction.Contract.ContractType.TriggerSmartContract) {
                        parseTrc20Contract(txId, timestamp,number);
                    }
                }
            }
        }
        int size = transactionsList.size();
        if(callback!=null) {
            callback.onSyncSuccess(number, size, LocalDateTime.now());
        }
        return number;
    }

    public BigInteger queryDecimals(String contractAddress) {
        Trc20Contract token = getContract(contractAddress);
        log.info("contractAddress={}",contractAddress);
        BigInteger decimals = BigInteger.ZERO;
        try {
            decimals = token.decimals();
        }catch (Exception e){
            log.error("decimals={}",e);
        }
        log.info("queryDecimals={}", decimals);
        return decimals;
    }

    public String querySymbol(String contractAddress) {
        Trc20Contract token = getContract(contractAddress);
        String name = null;
        try{
            name=token.symbol();
        }catch (Exception e){
            log.error("symbol={},contractAddress={}",e,contractAddress);
        }
        log.info("querySymbol={}", name);
        return name;
    }

    public Trc20Contract getContract(String contractAddress) {
        return getContract(contractAddress,null);
    }
    public Trc20Contract getContract(String contractAddress,String address) {
        org.tron.trident.core.contract.Contract contract = apiWrapper.getContract(contractAddress);
        Trc20Contract token = new Trc20Contract(contract, address==null?this.address:address, apiWrapper);
        return token;
    }

    public String queryName(String contractAddress) {
        Trc20Contract token = getContract(contractAddress);
        String name = null;
        try{
            name=token.symbol();
        }catch (Exception e){
            log.error("symbol={},contractAddress={}",e,contractAddress);
        }
        log.info("queryName={}", name);
        return name;
    }

    public Common.SmartContract.ABI queryAbi(String contractAddress) {
        Trc20Contract token = getContract(contractAddress);
        Common.SmartContract.ABI abi = token.getAbi();
        log.info("queryName={}", abi);
        return abi;
    }

    public Chain.Transaction sendTrx(String fromAddress, String toAddress, BigInteger amount) throws IllegalException {
        Chain.Transaction trxAndSignTransaction = createTrxAndSignTransaction(apiWrapper, fromAddress, toAddress, amount);
        log.info("sendTrx fromAddress={},toAddress={},amount={}",fromAddress,toAddress,amount);
        String result = apiWrapper.broadcastTransaction(trxAndSignTransaction);
        return trxAndSignTransaction;
    }
    public String sendTrx(String toAddress, BigInteger amount) throws IllegalException {
        Chain.Transaction trxAndSignTransaction = createTrxAndSignTransaction(apiWrapper, address, toAddress, amount);
        log.info("sendTrx fromAddress={},toAddress={},amount={}",address,toAddress,amount);
        String result = apiWrapper.broadcastTransaction(trxAndSignTransaction);
        return result;
    }

    public String sendTrxWithTxid(String fromAddress, String toAddress, BigInteger amount) throws IllegalException {
        Chain.Transaction trxAndSignTransaction = createTrxAndSignTransaction(apiWrapper, fromAddress, toAddress, amount);
        log.info("sendTrx fromAddress={},toAddress={},amount={}",fromAddress,toAddress,amount);
        String result = apiWrapper.broadcastTransaction(trxAndSignTransaction);
        return result;
    }
    public Chain.Transaction createTrxAndSignTransaction(String fromAddress,String toAddress,BigInteger amount) throws IllegalException {
        return createTrxAndSignTransaction(apiWrapper,fromAddress,toAddress,amount);
    }

    public Chain.Transaction createTrxAndSignTransaction(ApiWrapper apiWrapper,String fromAddress,String toAddress,BigInteger amount) throws IllegalException {
        Response.TransactionExtention transfer = apiWrapper.transfer(fromAddress, toAddress, amount.longValue());
        Chain.Transaction transaction = apiWrapper.signTransaction(transfer);
        return transaction;
    }

    public Chain.Transaction createTrc20AndSignTransaction(String contractAddress,String fromAddress,String toAddress,BigInteger amount) throws IllegalException {
        return createTrc20AndSignTransaction(apiWrapper,contractAddress,fromAddress,toAddress,amount);
    }
    public Chain.Transaction createTrc20AndSignTransaction(ApiWrapper apiWrapper,String contractAddress,String fromAddress,String toAddress,BigInteger amount) throws IllegalException {
        Function transfer = new Function("transfer", Arrays.asList( new Address(toAddress), new Uint256(amount)), Arrays.asList(new TypeReference<Bool>() {
        }));
        TransactionBuilder builder = apiWrapper.triggerCall(fromAddress, contractAddress, transfer);
        builder.setFeeLimit(BigInteger.TEN.pow(9).longValue());
//        builder.setMemo("memo");
        Chain.Transaction signedTxn = apiWrapper.signTransaction(builder.build());
        return signedTxn;
    }

    public long queryBalance(String fromAddress){
        long accountBalance = apiWrapper.getAccountBalance(fromAddress);
        return accountBalance;
    }

    public long queryBalance(){
        log.info("queryBalance address={}",address);
        long accountBalance = apiWrapper.getAccountBalance(address);
        log.info("queryBalance address={},accountBalance={}",address,accountBalance);
        return accountBalance;
    }

    public BigInteger queryTrc20Balance(String contractAddress){
        Trc20Contract contract = getContract(contractAddress);
        return contract.balanceOf(address);
    }

    public BigInteger queryTrc20Balance(String contractAddress,String address){
        Trc20Contract contract = getContract(contractAddress,address);
        BigInteger bigInteger = contract.balanceOf(address);
        log.info("queryTrc20Balance contractAddress={},address={},amount={}",contractAddress,address,bigInteger);
        return bigInteger;
    }

    public void broadcast(Chain.Transaction transaction) throws IllegalException {
        int size = transaction.getSerializedSize() + 64;
        String result = apiWrapper.broadcastTransaction(transaction);
    }

//    public int calcBandWidth(String fromAddress, String toAddress,BigInteger amount) throws IllegalException {
//        Chain.Transaction transaction = createTrxAndSignTransaction(fromAddress,toAddress,amount);
//        int serializedSize = transaction.toBuilder().build().getSerializedSize()+64;
//        log.info("calcBandWidth size={},fromAddress={},toAddress={},amount={}",serializedSize,fromAddress,toAddress,amount);
//        return serializedSize;
//    }

    public long calcTrc20BandWidth(String fromAddress, String toAddress,String contractAddress,BigInteger amount) throws IllegalException {
        Chain.Transaction transaction = createTrc20AndSignTransaction(contractAddress,fromAddress,toAddress,amount);
        long serializedSize = transaction.toBuilder().clearRet().build().getSerializedSize()+64;
        log.info("calcBandWidth size={},contractAddress={},fromAddress={},toAddress={},amount={}",serializedSize,contractAddress,fromAddress,toAddress,amount);
        return serializedSize;
    }

    public Chain.Transaction sendTrc20(String contractAddress,String fromAddress, String toAddress,BigInteger amount) throws TransferException {
        Chain.Transaction transaction = null;
        try {
            transaction = createTrc20AndSignTransaction(contractAddress,fromAddress,toAddress,amount);
        } catch (IllegalException e) {
            throw new TransferException(e.getMessage());
        }
        byte[] bytes = ApiWrapper.calculateTransactionHash(transaction);
        String txId = ApiWrapper.toHex(bytes);
        log.info("calcBandWidth contractAddress={},fromAddress={},toAddress={},amount={},txId={}",contractAddress,fromAddress,toAddress,amount,txId);
        Response.TransactionReturn transactionReturn = apiWrapper.blockingStub.broadcastTransaction(transaction);
        log.info("sendTrc20={}",transactionReturn);
        boolean result = transactionReturn.getResult();
        if(!result){
            throw new TransferException(transactionReturn,transaction);
        }
        return transaction;
    }
    public Chain.Transaction sendTrc20(String contractAddress, String toAddress,BigInteger amount) throws TransferException {
        Chain.Transaction transaction = null;
        try {
            transaction = createTrc20AndSignTransaction(contractAddress,address,toAddress,amount);
        } catch (IllegalException e) {
            throw new TransferException(e.getMessage());
        }
        byte[] bytes = ApiWrapper.calculateTransactionHash(transaction);
        String txId = ApiWrapper.toHex(bytes);
        log.info("calcBandWidth contractAddress={},fromAddress={},toAddress={},amount={},txId={}",contractAddress,address,toAddress,amount,txId);
        Response.TransactionReturn transactionReturn = apiWrapper.blockingStub.broadcastTransaction(transaction);
        log.info("sendTrc20={}",transactionReturn);
        boolean result = transactionReturn.getResult();
        if(!result){
            throw new TransferException(transactionReturn,transaction);
        }
        return transaction;
    }
    public String sendTrc20WithTxId(String contractAddress,String fromAddress, String toAddress,BigInteger amount) throws TransferException {
        Chain.Transaction transaction = null;
        try {
            transaction = createTrc20AndSignTransaction(contractAddress,fromAddress,toAddress,amount);
        } catch (IllegalException e) {
            throw new TransferException(e.getMessage());
        }
        byte[] bytes = ApiWrapper.calculateTransactionHash(transaction);
        String txId = ApiWrapper.toHex(bytes);
        log.info("calcBandWidth contractAddress={},fromAddress={},toAddress={},amount={},txId={}",contractAddress,fromAddress,toAddress,amount,txId);
        Response.TransactionReturn transactionReturn = apiWrapper.blockingStub.broadcastTransaction(transaction);
        log.info("sendTrc20={}",transactionReturn);
        boolean result = transactionReturn.getResult();
        if(!result){
            throw new TransferException(transactionReturn,transaction);
        }
        return txId;
    }
    public Long calcEnergyUsed(String fromAddress, String toAddress, String contractAddress, BigInteger amount) {
        Function transfer = new Function("transfer", Arrays.asList(new Address(toAddress), new Uint256(amount)), Arrays.asList(new TypeReference<Bool>() {
        }));
//        Response.TransactionExtention txnExt = token.callWithoutBroadcast(fromAddress,token,transfer);
        Response.TransactionExtention txnExt = apiWrapper.constantCall(fromAddress, contractAddress, transfer);
        long energyUsed = txnExt.getEnergyUsed();
        log.info("fromAddress={},contractAddress={},toAddress={},amount={},energyUsed={}",fromAddress,contractAddress,toAddress,amount,energyUsed);
        return energyUsed;
    }

    public Chain.Block getNewBlock() throws IllegalException {
        Chain.Block nowBlock = null;
        while (nowBlock==null){
            try {
                nowBlock = apiWrapper.getNowBlock();
            }catch (Exception e){
                log.error("getNewBlock error={}",e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return nowBlock;
    }

    public long calcTrxEnergy() throws IllegalException {
        Response.ChainParameters chainParameters = apiWrapper.getChainParameters();
        Optional<Response.ChainParameters.ChainParameter> first = chainParameters.getChainParameterList().stream().filter(t -> "getEnergyFee".equals(t.getKey())).findFirst();
        long value = first.get().getValue();
        return value;
    }

    public String transferFee(String fromAddress,String toAddress,BigInteger fee) throws TransferException {
        log.info("transferFee={}",fee);
        long balance = queryBalance(toAddress);
        if(balance>=fee.longValue()){
            log.info("transferFee fee={},balance={} enough",fee,balance);
            return null;
        }
        Chain.Transaction transaction = null;
        try {
            transaction = createTrxAndSignTransaction(fromAddress, toAddress, fee);
        } catch (IllegalException e) {
            throw new TransferException(e.getMessage());
        }
        String result = apiWrapper.broadcastTransaction(transaction);
        log.info("transfer={}",result);
        return result;
    }

    public int queryBlocks(String txId) throws IllegalException, InvalidProtocolBufferException {
        Chain.Transaction transaction = apiWrapper.getTransactionById(txId);
        Response.TransactionApprovedList transactionApprovedList = apiWrapper.getTransactionApprovedList(transaction);
        return transactionApprovedList.getApprovedListCount();
    }


    public String freezeBalanceV2(BigDecimal trx,int resourceCode) throws IllegalException {
        Response.TransactionExtention transactionExtention = apiWrapper.freezeBalanceV2(address, Convert.toSun(trx.toString(), Convert.Unit.TRX).longValue(), resourceCode);
        Chain.Transaction transaction = apiWrapper.signTransaction(transactionExtention.getTransaction());
        String result = apiWrapper.broadcastTransaction(transaction);
        return result;
    }

    public String freezeBalanceV2OfEnergy(BigDecimal trx,int resourceCode) throws IllegalException {
        return freezeBalanceV2(trx,resourceCode);
    }

    public String delegateResource(String receciveAddress,BigDecimal trx,int resourceCode) throws IllegalException {
        Response.TransactionExtention transactionExtention1 = apiWrapper.delegateResource(address,  Convert.toSun(trx.toString(), Convert.Unit.TRX).longValue(), resourceCode, receciveAddress, false);
        Chain.Transaction transaction = apiWrapper.signTransaction(transactionExtention1.getTransaction());
        String result = apiWrapper.broadcastTransaction(transaction);
        return result;
    }
    public String delegateResourceOfEnergy(String receciveAddress,BigDecimal trx) throws IllegalException {
        return delegateResource(receciveAddress,trx,1);
    }

    public String unfreezeBalanceV2(BigDecimal trx,int resourceCode) throws IllegalException {
        Response.TransactionExtention transactionExtention = apiWrapper.unfreezeBalanceV2(address, Convert.toSun(trx.toString(), Convert.Unit.TRX).longValue(), resourceCode);
        Chain.Transaction transaction = apiWrapper.signTransaction(transactionExtention.getTransaction());
        String result = apiWrapper.broadcastTransaction(transaction);
        return result;
    }

    public String unfreezeBalanceV2OfEnergy(BigDecimal trx,int resourceCode) throws IllegalException {
        return unfreezeBalanceV2(trx,resourceCode);
    }

    public String undelegateResource(String receciveAddress,BigDecimal trx,int resourceCode) throws IllegalException {
        Response.TransactionExtention transactionExtention1 = apiWrapper.undelegateResource(address,  Convert.toSun(trx.toString(), Convert.Unit.TRX).longValue(), resourceCode, receciveAddress);
        Chain.Transaction transaction = apiWrapper.signTransaction(transactionExtention1.getTransaction());
        String result = apiWrapper.broadcastTransaction(transaction);
        return result;
    }
    public String undelegateResourceOfEnergy(String receciveAddress,BigDecimal trx) throws IllegalException {
        return undelegateResource(receciveAddress,trx,1);
    }

    public Long getAccountResource(String ownerAddress){
        Response.AccountResourceMessage accountResource = apiWrapper.getAccountResource(ownerAddress);
        return accountResource.getTotalEnergyWeight();
    }

    public void sendEnergy(String receiveAddress,BigDecimal trx) throws IllegalException {
//        freezeBalanceV2(trx,1);
//        delegateResourceOfEnergy(receiveAddress,trx);
    }
    public void sendBand(String receiveAddress,BigDecimal trx) throws IllegalException {
//        freezeBalanceV2(trx,0);
        delegateResource(receiveAddress,trx,0);
    }

    public Response.AccountResourceMessage getAccountResource(){
        Response.AccountResourceMessage accountResource = apiWrapper.getAccountResource(address);
        return accountResource;
    }


    public BigDecimal queryNeedTrxOfSum(Response.AccountResourceMessage accountResourceMessage,BigDecimal data,int resourceCode){

        BigDecimal result = BigDecimal.ZERO;
        if(resourceCode==0){
            long totalNetWeight = accountResourceMessage.getTotalNetWeight();
            long totalNetLimit = accountResourceMessage.getTotalNetLimit();
            result = data.multiply(BigDecimal.valueOf(1.05)).multiply(BigDecimal.valueOf(totalNetWeight)).divide(BigDecimal.valueOf(totalNetLimit),BigDecimal.ROUND_UP);
        }else {
            long totalEnergyWeight = accountResourceMessage.getTotalEnergyWeight();
            long totalEnergyLimit = accountResourceMessage.getTotalEnergyLimit();
            result = data.multiply(BigDecimal.valueOf(1.05)).multiply(BigDecimal.valueOf(totalEnergyWeight)).divide(BigDecimal.valueOf(totalEnergyLimit),BigDecimal.ROUND_UP);

        }
        return result;
    }

    public Response.AccountResourceMessage queryAccountResource(String address) {
        Response.AccountResourceMessage accountResource = apiWrapper.getAccountResource(address);
        log.info("accountResource={}",accountResource);
        return accountResource;
    }

    public Response.AccountResourceMessage queryAccountResource() {
        return queryAccountResource(address);
    }

//    public BigDecimal queryNeedTrxOfSum(Long energy) {
//        BigDecimal dEnergy = BigDecimal.valueOf(energy);
//        Long accountResource = getAccountResource();
//        BigDecimal trx = dEnergy.divide(BigDecimal.valueOf(50_000_000_000L)).multiply(BigDecimal.valueOf(accountResource));
//        return trx;
//    }

    public static void main(String[] args) throws IllegalException {
            ApiWrapper apiWrapper = ApiWrapper.ofMainnet("d0eb0c4ba56ebafca09992a5fdf35f11e46fb26550b81278d8bb887c675870dd","d6a69ed9-ca03-4a81-aed8-ced5cac3a5cc");
//        Chain.Block block = apiWrapper.getBlockById("54456380");
        Response.BlockExtention blockByNum = apiWrapper.getBlockByNum(54456380);
//        new ApiInstance().sync()
        Chain.Block blockById = apiWrapper.getBlockById("00000000033ef03cde06387942f6c80b2f1ef85dc4d66c2033c9900509306bd1");
        ApiInstance instance = ApiFactory.getInstance("d0eb0c4ba56ebafca09992a5fdf35f11e46fb26550b81278d8bb887c675870dd", "grpc.trongrid.io:50051", "grpc.trongrid.io:50052");
        try {
            instance.sync(54459651);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
//        System.out.println(new BigInteger("000000000000000000000000000000000000000000000000000000000af79e00", 16));
//        ApiWrapper apiWrapper = ApiWrapper.ofMainnet("d0eb0c4ba56ebafca09992a5fdf35f11e46fb26550b81278d8bb887c675870dd", "d6a69ed9-ca03-4a81-aed8-ced5cac3a5cc");
//        org.tron.trident.core.contract.Contract contract = apiWrapper.getContract("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t");
//        Trc20Contract token = new Trc20Contract(contract, "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t", apiWrapper);
//        System.out.println(token.name());
//        Response.TransactionExtention transaction = apiWrapper.transfer("TLtrDb1udekjDumnrf3EVeke3Q6pHkZxjm", "TP8LKAf3R3FHDAcrQXuwBEWmaGrrUdRvzb", 1_000_000);
//        Chain.Transaction signedTxn = apiWrapper.signTransaction(transaction);
//        signedTxn.getSerializedSize();
//
//        String origin = "Transfer(address,address,uint256)";
//        byte[] s = Hash.sha3(origin.getBytes(StandardCharsets.UTF_8),0,origin.length());
//        System.out.println(ApiWrapper.toHex(s));
//        System.out.println(Base58Check.bytesToBase58(ApiWrapper.parseHex("41a614f803b6fd780986a42c78ec9c7f77e6ded13c").toByteArray()));
//        System.out.println(Base58Check.bytesToBase58(ApiWrapper.parseHex("418a4a39b0e62a091608e9631ffd19427d2d338dbd").toByteArray()));
//        System.out.println(Base58Check.bytesToBase58(ApiWrapper.parseHex("419bca1ddbf78e89f3b1fb52a2fd74f64101c55013").toByteArray()));
//        System.out.println("E552F6487585C2B58BC2C9BB4492BC1F17132CD0".length());
//        System.out.println(new BigInteger("0000000000000000000000000000000000000000000000000000000000116ab0",16));
    }
//    public static void main(String[] args) {
////        String method = "Transfer(address,address,uint256)";
////        event_hash = keccak256(method.encode()).hex();
//        String address = Base58Check.bytesToBase58(ApiWrapper.parseHex("41683efa719428044e06edf1864b5efdb4df39223f").toByteArray());
//        System.out.println(address);
//    }


}
