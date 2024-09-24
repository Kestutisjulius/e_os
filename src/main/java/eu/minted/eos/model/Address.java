package eu.minted.eos.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
