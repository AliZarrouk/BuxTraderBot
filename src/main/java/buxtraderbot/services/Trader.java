package buxtraderbot.services;

import buxtraderbot.marketagent.PositionBuyerSeller;
import buxtraderbot.models.ProductPriceUpdate;
import buxtraderbot.models.ProductSellingBuyingPrices;
import buxtraderbot.models.websocketmessages.Subscription;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static buxtraderbot.configuration.ChannelConfiguration.PRODUCT_SUBSCRIPTION_SCHANNEL;

@Service
public class Trader {
    private final Logger logger = LogManager.getLogger(Trader.class);

    private final PositionBuyerSeller positionBuyerSeller;

    private final ProductPositionDal productPositionDal;

    private Map<String, ProductSellingBuyingPrices> productSellingBuyingPricesMap;

    private final MessageChannel productSubscriptionChannell;

    @Autowired
    public Trader(@Qualifier(PRODUCT_SUBSCRIPTION_SCHANNEL) MessageChannel productSubscriptionChannel,
                  PositionBuyerSeller positionBuyerSeller,
                  ProductPositionDal productPositionDal) {
        this.positionBuyerSeller = positionBuyerSeller;
        this.productPositionDal = productPositionDal;
        this.productSubscriptionChannell = productSubscriptionChannel;
    }

    @PostConstruct
    public void postConstruct() {
        // prepare positions with prices to buy, upperSell and lowerSell
        productSellingBuyingPricesMap = new HashMap<>();
    }

    public void processOrderUpdate(ProductPriceUpdate productPriceUpdate) {
        if (!productSellingBuyingPricesMap.containsKey(productPriceUpdate.getProductId())) {
            logger.error("Prouct not tracked {}", productPriceUpdate.getProductId());
            return;
        }

        var productSellingBuyingPrices
                = productSellingBuyingPricesMap.get(productPriceUpdate.getProductId());

        if (productSellingBuyingPrices.getBuyPrice() <= productPriceUpdate.getCurrentPrice()) {
            var positionId = positionBuyerSeller.openPosition(productPriceUpdate.getProductId());
            productPositionDal.registerPositionForProductId(productPriceUpdate.getProductId(), positionId);
            logger.info("Position {} opened for product with ID {}", positionId,
                    productPriceUpdate.getProductId());
            return;
        }

        if (productSellingBuyingPrices.getUpperSellPrice() <= productPriceUpdate.getCurrentPrice()) {
            logger.info("Got {} which is >= than upper sell price for product {}. " +
                            "Closing all positions.",
                    productPriceUpdate.getCurrentPrice(),
                    productSellingBuyingPrices.getUpperSellPrice());
        }

        if (productSellingBuyingPrices.getLowerSellPrice() >= productPriceUpdate.getCurrentPrice()) {
            logger.info("Got {} which is <= than lower sell price for product {}. " +
                            "Closing all positions.",
                    productPriceUpdate.getCurrentPrice(),
                    productSellingBuyingPrices.getUpperSellPrice());
        }

        productPositionDal.getProductPositions(productPriceUpdate.getProductId())
                .forEach(positionBuyerSeller::clostPosition);
        productPositionDal.sellPositonsForProductId(productPriceUpdate.getProductId());
    }

    public void processConnectionSuccessful() {
        var subscription = new Subscription();
        subscription.setSubscribeTo(new ArrayList<>(productSellingBuyingPricesMap.keySet()));
        productSubscriptionChannell.send(MessageBuilder.withPayload(subscription).build());
    }
}
