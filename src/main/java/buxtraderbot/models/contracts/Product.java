package buxtraderbot.models.contracts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {
    private String securityId;
    private String symbol;
    private String displayName;
}
