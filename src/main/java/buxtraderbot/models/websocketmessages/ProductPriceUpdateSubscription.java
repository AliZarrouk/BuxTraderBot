package buxtraderbot.models.websocketmessages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductPriceUpdateSubscription {
    private String productId;
    private Double currentPrice;
}
