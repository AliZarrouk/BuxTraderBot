package buxtraderbot.marketagent;

import buxtraderbot.exceptions.MarketConnectorException;
import buxtraderbot.models.websocketmessages.Subscription;
import buxtraderbot.services.FeedProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

import static buxtraderbot.configuration.ChannelConfiguration.PRODUCT_SUBSCRIPTION_CHANNEL;

@Service
public class BuxMarketSocketListener implements WebSocketListener {
    private final Logger logger = LogManager.getLogger(BuxMarketSocketListener.class);

    private static final ObjectMapper om = new ObjectMapper();

    private Session session;

    private final FeedProcessor feedProcessor;

    @Autowired
    public BuxMarketSocketListener(FeedProcessor feedProcessor) {
        this.feedProcessor = feedProcessor;
    }

    @Override
    public void onWebSocketClose(int i, String s) {
        logger.info("Web socket closed");
    }

    @Override
    public void onWebSocketConnect(Session session) {
        logger.info("Connection established");
        this.session = session;
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        logger.info("Connection error");
    }

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        logger.info("onWebSocketBinary");
    }

    @Override
    public void onWebSocketText(String s) {
        logger.info("onWebSocketText");
        // get message type
        // send trader if price update
        feedProcessor.process(s);
    }

    @ServiceActivator(inputChannel = PRODUCT_SUBSCRIPTION_CHANNEL)
    public void send(Message<Subscription> payload) {
        try {
            session.getRemote().sendString(om.writeValueAsString(payload.getPayload()));
        } catch (IOException e) {
            logger.error("Web socket error", e);
            throw new MarketConnectorException(e);
        }
    }
}
