package buxtraderbot.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryProductPositionDal implements ProductPositionDal {
    private Map<String, List<String>> productPositionMap = new HashMap<>();

    @Override
    public void registerPositionForProductId(String productId, String positionId) {
        if (productPositionMap.containsKey(productId)) {
            productPositionMap.get(productId).add(positionId);
        } else {
            ArrayList<String> positions = new ArrayList<>();
            positions.add(positionId);
            productPositionMap.put(productId, positions);
        }
    }

    @Override
    public void sellPositonsForProductId(String productId) {

    }
}
