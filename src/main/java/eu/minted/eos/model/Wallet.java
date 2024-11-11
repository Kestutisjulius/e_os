package eu.minted.eos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "wallets")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String walletNumber = WalletNumberGenerator.generateWalletNumber();

    @NotNull
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.TEN;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "wallet", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Transaction> transactions = new HashSet<>();

    public void addFunds(BigDecimal amount, String description) {
        if(amount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.add(amount);
            Transaction transaction = new Transaction(null, TransactionType.DEPOSIT, amount, LocalDateTime.now(), this, description);
            transactions.add(transaction);
        }else {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    public void removeFunds(BigDecimal amount, String description) {
        if(amount.compareTo(BigDecimal.ZERO) > 0 && this.balance.compareTo(amount) >= 0){
            this.balance = this.balance.subtract(amount);
            Transaction transaction = new Transaction(null, TransactionType.WITHDRAWAL, amount, LocalDateTime.now(), this, description);
        }else {
            throw new IllegalArgumentException("not enough balance");
        }
    }
}
