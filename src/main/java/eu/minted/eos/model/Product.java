package eu.minted.eos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Product {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank(message = "product name is mandatory")
    @Column(length = 255, nullable = false)
    private String name;

    @NotNull
    @Min(value = 0, message = "Price must be a positive number")
    private BigDecimal price;

    @Min(value = 0, message = "Quantity must be non-negative")
    private int quantity;

    @Column(length = 30)
    private String unit;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @Column(length = 1024)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_address_id", referencedColumnName = "id")
    private Address pickupAddress;


}
