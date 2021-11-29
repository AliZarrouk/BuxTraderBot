package buxtraderbot.services;

import java.util.List;

public interface ProductPositionDal {
    void registerPositionForProductId(String productId, String positionId);
    void sellPositionsForProductId(String productId);
    List<String> getProductPositions(String productId);
}
