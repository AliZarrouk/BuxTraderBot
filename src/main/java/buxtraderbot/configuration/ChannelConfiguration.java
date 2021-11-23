package buxtraderbot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
public class ChannelConfiguration {
    public final static String PRODUCT_SUBSCRIPTION_SCHANNEL = "ProductSubscriptionChannel";

    @Bean(name = PRODUCT_SUBSCRIPTION_SCHANNEL)
    public MessageChannel subscriptionProcessingChannel() {
        return new DirectChannel();
    }
}
