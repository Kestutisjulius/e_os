package eu.minted.eos.service;

import eu.minted.eos.model.TransactionType;
import eu.minted.eos.model.User;
import eu.minted.eos.model.Wallet;
import eu.minted.eos.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserServiceImpl userService;

    @Override
    public Wallet createWalletForUser(Long userId) {
        if (walletRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("User already has a wallet");
        }
        Wallet wallet = new Wallet();
        Optional<User> optionalUser = userService.getUserById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            wallet.setUser(user);
        }
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user id: " + userId));
    }

    @Override
    @Transactional
    public Wallet deposit(Long userId, BigDecimal amount, String description) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.addFunds(amount, description); // Pridedama į istoriją
        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public Wallet withdraw(Long userId, BigDecimal amount, String description) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.removeFunds(amount, description); // Pridedama į istoriją
        return walletRepository.save(wallet);
    }

    @Override
    public BigDecimal getBalance(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        return wallet.getBalance();
    }
}
