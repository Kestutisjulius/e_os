package eu.minted.eos.model;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class OrderWithProducts {
    private Order order;
    private List<ProductInfo> products;
}
