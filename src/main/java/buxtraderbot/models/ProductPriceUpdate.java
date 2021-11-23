package buxtraderbot.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductPriceUpdate {
    private String productId;
    private Double currentPrice;
}
