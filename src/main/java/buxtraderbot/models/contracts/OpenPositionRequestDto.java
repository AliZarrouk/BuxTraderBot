package buxtraderbot.models.contracts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenPositionRequestDto {
    private String productId;
    private InvestingAmount investingAmount;
    private int leverage;
    private String direction;
    private Source source;
    private String riskWarningConfirmation;
}
