package buxtraderbot.services;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InMemoryProductPositionDal implements ProductPositionDal {
    private final Map<String, List<String>> productPositionMap = new HashMap<>();

    @Override
    public void registerPositionForProductId(String productId, String positionId) {
        if (productPositionMap.containsKey(productId)) {
            productPositionMap.get(productId).add(positionId);
        } else {
            var positions = new ArrayList<String>();
            positions.add(positionId);
            productPositionMap.put(productId, positions);
        }
    }

    @Override
    public void sellPositionsForProductId(String productId) {
        productPositionMap.remove(productId);
    }

    @Override
    public List<String> getProductPositions(String productId) {
        return productPositionMap.get(productId);
    }
}
