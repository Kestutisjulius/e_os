package eu.minted.eos.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "carts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Šis krepšelis bus susietas su konkrečiu vartotoju
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Krepšelio turinys (prekių sąrašas ir kiekiai)
    @ElementCollection
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
    @MapKeyJoinColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<Product, Integer> items = new HashMap<>();

    // Krepšelio sukūrimo data
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    // Metodas, skirtas pridėti prekę į krepšelį
    public void addProduct(Product product, int quantity) {
        this.items.put(product, this.items.getOrDefault(product, 0) + quantity);
    }

    // Metodas, skirtas pašalinti prekę iš krepšelio
    public void removeProduct(Product product) {
        this.items.remove(product);
    }

    // Skaičiuoti bendrą prekių kainą
    public BigDecimal getTotalPrice() {
        return items.entrySet().stream()
                .map(entry -> entry.getKey().getPrice().multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Gauti visų prekių kiekį
    public int getTotalQuantity() {
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }
}
