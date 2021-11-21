package buxtraderbot.services;

public interface ProductPositionDal {
    void registerPositionForProductId(String productId, String positionId);
    void sellPositonsForProductId(String productId);
}
