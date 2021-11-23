package buxtraderbot.models.websocketmessages.priceupdate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Body {
    private String securityId;
    private String currentPrice;
}
