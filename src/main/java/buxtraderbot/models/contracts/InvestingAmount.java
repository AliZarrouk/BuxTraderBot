package buxtraderbot.models.contracts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvestingAmount {
    private String currency;
    private int decimals;
    private String amount;
}
