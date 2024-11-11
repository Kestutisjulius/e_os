package eu.minted.eos.service;

import eu.minted.eos.model.Transaction;

import java.util.List;

public interface TransactionService {
    Transaction saveTransaction(Transaction transaction);
    List<Transaction> getAllTransactionsByWalletId(Long walletId);
    List<Transaction> getTransactionsByType(String transactionType);
}
