package buxtraderbot.services;

import buxtraderbot.exceptions.MarketConnectorException;
import buxtraderbot.models.ProductPriceUpdate;
import buxtraderbot.models.websocketmessages.priceupdate.TradingQuote;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import static buxtraderbot.configuration.ChannelConfiguration.*;

@Service
public class FeedProcessor {
    private final Logger logger = LogManager.getLogger(FeedProcessor.class);

    private static final ObjectMapper om = new ObjectMapper();

    private final MessageChannel priceUpdateChannel;
    private final MessageChannel portfolioValueChannel;
    private final MessageChannel successfulConnectionChannel;

    @Autowired
    public FeedProcessor(@Qualifier(PRICE_UPDATE_CHANNEL) MessageChannel priceUpdateChannel,
                         @Qualifier(PORTFOLIO_VALUE_CHANNEL) MessageChannel portfolioValueChannel,
                         @Qualifier(SUCCESSFUL_CONNECTION_CHANNEL) MessageChannel successfulConnectionChannel) {
        this.portfolioValueChannel = portfolioValueChannel;
        this.priceUpdateChannel = priceUpdateChannel;
        this.successfulConnectionChannel = successfulConnectionChannel;
    }

    public void process(String payload) {
        if (payload.contains("connect.connected")) {
            logger.info("Connection established");
            successfulConnectionChannel.send(MessageBuilder.withPayload("").build());
            return;
        }

        if (payload.contains("connect.failed")) {
            logger.info("Received connection Failed");
            // retry ?
            return;
        }

        if (payload.contains("portfolio.performance")) {
            logger.info("Received portfolio performance message");
            portfolioValueChannel.send(MessageBuilder.withPayload(getFunds(payload)).build());
            return;
        }

        if (payload.contains("trading.quote")) {
            logger.info("Received trading quote");
            try {
                var tradingQuote = om.readValue(payload, TradingQuote.class);
                var productPriceUpdate = new ProductPriceUpdate();
                productPriceUpdate.setProductId(tradingQuote.getBody().getSecurityId());
                productPriceUpdate.setCurrentPrice(Double.parseDouble(tradingQuote.getBody().getCurrentPrice()));
                priceUpdateChannel.send(MessageBuilder.withPayload(productPriceUpdate).build());
            } catch (JsonProcessingException e) {
                logger.error("Error parsing trading quote", e);
                throw new MarketConnectorException(e);
            }
            return;
        }

        logger.warn("Unknown message received {}, ignoring", payload);
    }

    private Double getFunds(String payload) {
        String[] splitted = payload.split("amount");
        int end = splitted[1].indexOf('}') - 1;
        String substring = splitted[1].substring(3, end);
        Double d = Double.parseDouble(substring);
        return d;
    }
}
