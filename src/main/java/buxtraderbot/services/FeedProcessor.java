package buxtraderbot.services;

import buxtraderbot.exceptions.MarketConnectorException;
import buxtraderbot.models.ProductPriceUpdate;
import buxtraderbot.models.websocketmessages.priceupdate.TradingQuote;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeedProcessor {
    private final Logger logger = LogManager.getLogger(FeedProcessor.class);

    private static final ObjectMapper om = new ObjectMapper();

    private final Trader trader;

    @Autowired
    public FeedProcessor(Trader trader) {
        this.trader = trader;
    }

    public void process(String payload) {
        if (payload.contains("\"t\": \"connect.connected\"")) {
            logger.info("Received connection OK");
            trader.processConnectionSuccessful();
            return;
        }

        if (payload.contains("\"t\": \"connect.failed\"")) {
            logger.info("Received connection Failed");
            // retry
            // send for retry
            return;
        }

        if (payload.contains("\"t\":\"portfolio.performance\"")) {
            logger.info("Received portfolio performance message");
            // update portfolio
            return;
        }

        if (payload.contains("\"t\": \"trading.quote\"")) {
            logger.info("Received trading quote");
            try {
                TradingQuote tradingQuote = om.readValue(payload, TradingQuote.class);
                ProductPriceUpdate productPriceUpdate = new ProductPriceUpdate();
                productPriceUpdate.setProductId(tradingQuote.getBody().getSecurityId());
                productPriceUpdate.setCurrentPrice(Double.parseDouble(tradingQuote.getBody().getCurrentPrice()));
                trader.processOrderUpdate(productPriceUpdate);
            } catch (JsonProcessingException e) {
                logger.error("Error parsing trading quote", e);
                throw new MarketConnectorException(e);
            }
            return;
        }

        logger.warn("Unknown message received {}, ignoring", payload);
    }
}
