package buxtraderbot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.support.PeriodicTrigger;

@Configuration
public class ChannelConfiguration {
    public final static String PRODUCT_SUBSCRIPTION_CHANNEL = "ProductSubscriptionChannel";
    public final static String PRICE_UPDATE_CHANNEL = "PriceUpdateChannel";
    public final static String PORTFOLIO_VALUE_CHANNEL = "PortfolioValueChannel";
    public final static String SUCCESSFUL_CONNECTION_CHANNEL = "SuccessfulConnectionChannel";

    @Bean(name = PRODUCT_SUBSCRIPTION_CHANNEL)
    public MessageChannel subscriptionProcessingChannel() {
        return new DirectChannel();
    }

    @Bean(name = SUCCESSFUL_CONNECTION_CHANNEL)
    public MessageChannel successfulConnectionChannel() {
        return new DirectChannel();
    }

    @Bean(name = PRICE_UPDATE_CHANNEL)
    public MessageChannel priceUpdateChannel() {
        return new QueueChannel();
    }

    @Bean(name = PORTFOLIO_VALUE_CHANNEL)
    public MessageChannel portfolioValueChannel() {
        return new DirectChannel();
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata defaultPoller() {

        PollerMetadata pollerMetadata = new PollerMetadata();
        pollerMetadata.setTrigger(new PeriodicTrigger(10));
        return pollerMetadata;
    }
}
