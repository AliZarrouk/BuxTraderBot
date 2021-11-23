package buxtraderbot.models.websocketmessages.priceupdate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TradingQuote {
    private String t;
    private Body body;
}
