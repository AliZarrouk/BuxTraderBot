package buxtraderbot.services;

import buxtraderbot.marketagent.PositionBuyerSeller;
import buxtraderbot.models.ProductPriceUpdate;
import buxtraderbot.models.ProductSellingBuyingPrices;
import buxtraderbot.models.websocketmessages.Subscription;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static buxtraderbot.configuration.ChannelConfiguration.*;

@Service
public class Trader {
    private final Logger logger = LogManager.getLogger(Trader.class);

    private final PositionBuyerSeller positionBuyerSeller;

    private final ProductPositionDal productPositionDal;

    private final Map<String, ProductSellingBuyingPrices> productSellingBuyingPricesMap;

    private final MessageChannel productSubscriptionChannel;

    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    private Double funds;

    @Value("${product-buy-sell-file}")
    private String productBuySellFilePath;

    @Value("${product-buy-sell-file-separator}")
    private String productBuySellFileSeparator;

    @Autowired
    public Trader(@Qualifier(PRODUCT_SUBSCRIPTION_CHANNEL) MessageChannel productSubscriptionChannel,
                  PositionBuyerSeller positionBuyerSeller,
                  ProductPositionDal productPositionDal) {
        this.positionBuyerSeller = positionBuyerSeller;
        this.productPositionDal = productPositionDal;
        this.productSubscriptionChannel = productSubscriptionChannel;
        productSellingBuyingPricesMap = new HashMap<>();
    }

    private Double getFunds() {
        Double result;
        lock.readLock().lock();
        result = funds;
        lock.readLock().unlock();
        return result;
    }

    private void updateFunds(Double funds) {
        lock.writeLock().lock();
        this.funds = funds;
        lock.writeLock().unlock();
    }

    @ServiceActivator(inputChannel = PRICE_UPDATE_CHANNEL)
    public void processOrderUpdate(Message<ProductPriceUpdate> productPriceUpdateMessage) {
        logger.info("Received price {} for product with id {}",
                productPriceUpdateMessage.getPayload().getCurrentPrice(),
                productPriceUpdateMessage.getPayload().getProductId());

        var productPriceUpdate = productPriceUpdateMessage.getPayload();
        if (!productSellingBuyingPricesMap.containsKey(productPriceUpdate.getProductId())) {
            logger.error("Product not tracked {}", productPriceUpdate.getProductId());
            return;
        }

        var productSellingBuyingPrices
                = productSellingBuyingPricesMap.get(productPriceUpdate.getProductId());

        if (productPositionDal.getProductPositions(productPriceUpdate.getProductId()) == null
            || productPositionDal.getProductPositions(productPriceUpdate.getProductId()).isEmpty()) {
            if (productSellingBuyingPrices.getBuyPrice() >= productPriceUpdate.getCurrentPrice()) {
                var investment = getInvestment(productSellingBuyingPrices.getBuyPrice(),
                        productPriceUpdate.getCurrentPrice());
                if (investment == 0d) {
                    logger.warn("Good opportunity but not enough mula");
                    return;
                }
                var positionId = positionBuyerSeller.openPosition(productPriceUpdate.getProductId(),
                        investment);
                if (positionId == null) {
                    return;
                }
                productPositionDal.registerPositionForProductId(productPriceUpdate.getProductId(), positionId);
                logger.info("Position {} opened for product with ID {}", positionId,
                        productPriceUpdate.getProductId());
            }
            return;
        }

        if (productPriceUpdate.getCurrentPrice() >= productSellingBuyingPrices.getUpperSellPrice()) {
            logger.info("Got {} which is >= than upper sell price for product {}. " +
                            "Closing all positions.",
                    productSellingBuyingPrices.getUpperSellPrice(),
                    productPriceUpdate.getProductId());
            productPositionDal.getProductPositions(productPriceUpdate.getProductId())
                    .forEach(positionBuyerSeller::closePosition);
            productPositionDal.sellPositionsForProductId(productPriceUpdate.getProductId());
            return;
        }

        if (productPriceUpdate.getCurrentPrice() <= productSellingBuyingPrices.getLowerSellPrice()) {
            logger.info("Got {} which is <= than lower sell price for product {}. " +
                            "Closing all positions.",
                    productSellingBuyingPrices.getUpperSellPrice(),
                    productPriceUpdate.getProductId());
            productPositionDal.getProductPositions(productPriceUpdate.getProductId())
                    .forEach(positionBuyerSeller::closePosition);
            productPositionDal.sellPositionsForProductId(productPriceUpdate.getProductId());
        }
    }

    @ServiceActivator(inputChannel = SUCCESSFUL_CONNECTION_CHANNEL)
    public void processConnectionSuccessful(Message<String> message) {
        parseProductBuySellFilePath();

        if (productSellingBuyingPricesMap.keySet().isEmpty()) {
            return;
        }

        var subscription = new Subscription();
        var products = productSellingBuyingPricesMap.keySet()
                .stream().map(x -> "trading.product." + x)
                .collect(Collectors.toList());
        subscription.setSubscribeTo(products);
        productSubscriptionChannel.send(MessageBuilder.withPayload(subscription).build());
    }

    @ServiceActivator(inputChannel = PORTFOLIO_VALUE_CHANNEL)
    public void processPortfolioValueUpdate(Message<Double> doubleMessage) {
        updateFunds(doubleMessage.getPayload());
    }

    private Double getInvestment(Double buyPrice, Double currentPrice) {
        var funds = getFunds();
        if (funds == 0d || funds < 10d) {
            return 0d;
        }

        if (currentPrice <= (buyPrice/2)) {
            // a bargain ! all innnn!
            Double result = getFunds();
            if (result >= 500d) {
                result = 500d;
            }
            updateFunds(getFunds() - 500d);
            return result;
        }

        Double result =  0.3 * getFunds();
        updateFunds(getFunds() - result);
        return result;
    }

    private void parseProductBuySellFilePath() {
        try (BufferedReader br = new BufferedReader(new FileReader(productBuySellFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                var splitted = line.split(productBuySellFileSeparator);
                if (splitted.length < 4) {
                    continue;
                }
                var productSellingBuyingPrices = new ProductSellingBuyingPrices();
                productSellingBuyingPrices.setBuyPrice(Double.parseDouble(splitted[1]));
                productSellingBuyingPrices.setUpperSellPrice(Double.parseDouble(splitted[2]));
                productSellingBuyingPrices.setLowerSellPrice(Double.parseDouble(splitted[3]));
                productSellingBuyingPricesMap.put(splitted[0], productSellingBuyingPrices);
            }
        } catch (IOException e) {
            logger.error("Error parsing config file", e);
            throw new RuntimeException(e);
        }
    }
}
