package buxtraderbot.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSellingBuyingPrices {
    private Double buyPrice;
    private Double upperSellPrice;
    private Double lowerSellPrice;
}
