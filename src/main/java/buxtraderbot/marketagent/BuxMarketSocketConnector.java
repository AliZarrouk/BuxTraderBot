package buxtraderbot.marketagent;

import buxtraderbot.exceptions.MarketConnectorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.PostConstruct;
import java.net.URI;

@Configuration
public class BuxMarketSocketConnector {
    private final Logger logger = LogManager.getLogger(BuxMarketSocketConnector.class);

    @Value("${market.url.feed}")
    private String marketFeedUrl;

    @Value("${header.auth.token}")
    private String authToken;

    @Value("${header.accept.language}")
    private String acceptLanguage;

    private final BuxMarketSocketListener listener;

    @Autowired
    public BuxMarketSocketConnector(BuxMarketSocketListener listener) {
        this.listener = listener;
    }

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        connect();
    }

    private void connect() {
        try {
            WebSocketClient webSocketClient = new WebSocketClient();
            webSocketClient.start();
            URI marketFeedUri = new URI(marketFeedUrl);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
            request.setHeader(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage);
            webSocketClient.connect(listener, marketFeedUri, request);
        } catch (Exception e) {
            logger.error("Connection error", e);
            throw new MarketConnectorException(e);
        }
    }
}
