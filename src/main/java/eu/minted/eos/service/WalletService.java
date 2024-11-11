package eu.minted.eos.service;

import eu.minted.eos.model.Wallet;

import java.math.BigDecimal;

public interface WalletService {

    Wallet createWalletForUser(Long userId);
    Wallet getWalletByUserId(Long userId);
    Wallet deposit(Long userId, BigDecimal amount, String description);
    Wallet withdraw(Long userId, BigDecimal amount, String description);
    BigDecimal getBalance(Long userId);
}
