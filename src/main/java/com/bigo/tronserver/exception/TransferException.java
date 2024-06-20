package com.bigo.tronserver.exception;

import lombok.Data;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;

@Data
public class TransferException extends Throwable {
    Response.TransactionReturn transactionReturn;
    Chain.Transaction transaction;
    public TransferException(Response.TransactionReturn transactionReturn, Chain.Transaction transaction) {
        super(transactionReturn.toString());
        this.transactionReturn = transactionReturn;
        this.transaction = transaction;
    }

    public TransferException(String message) {
        super(message);
    }
}
