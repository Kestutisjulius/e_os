package eu.minted.eos.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ProductInfo {
    private String productName;
    private Integer quantity;
}
