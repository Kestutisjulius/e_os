package eu.minted.eos.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    @OneToOne(mappedBy = "deliveryAddress", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Order orderDelivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_address_id")
    @ToString.Exclude
    private Product productPickup;

    @OneToOne(mappedBy = "address", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private User userAddress;
}
