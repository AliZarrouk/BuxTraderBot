package buxtraderbot.services;

import java.util.List;

public interface ProductPositionDal {
    void registerPositionForProductId(String productId, String positionId);
    void sellPositonsForProductId(String productId);
    List<String> getProductPositions(String productId);
}
