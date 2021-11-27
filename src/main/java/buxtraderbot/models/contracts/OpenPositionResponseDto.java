package buxtraderbot.models.contracts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenPositionResponseDto {
    private String id;
    private String positionId;
    private Product product;
    private InvestingAmount investingAmount;
    private Price price;
    private int leverage;
    private String direction;
    private String type;
    private long dateCreated;
}
