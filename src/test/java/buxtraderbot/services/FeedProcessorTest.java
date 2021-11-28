package buxtraderbot.services;

import buxtraderbot.models.ProductPriceUpdate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;

/**
 * Unit test for {@link FeedProcessor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class FeedProcessorTest {
    private FeedProcessor underTest;

    @Mock
    private MessageChannel priceUpdateChannel;

    @Mock
    private MessageChannel portfolioValueChannel;

    @Mock
    private MessageChannel successfulConnectionChannel;

    @Captor
    private ArgumentCaptor<Message<Double>> doubleMessageArgumentCaptor;

    @Captor
    private ArgumentCaptor<Message<ProductPriceUpdate>> productPriceUpdateMessageArgumentCaptor;

    @Before
    public void setUp() {
        underTest = new FeedProcessor(priceUpdateChannel,
                portfolioValueChannel,
                successfulConnectionChannel);
    }

    @After
    public void tearDown() {
        Mockito.verifyNoMoreInteractions(priceUpdateChannel);
        Mockito.verifyNoMoreInteractions(portfolioValueChannel);
        Mockito.verifyNoMoreInteractions(successfulConnectionChannel);
    }

    @Test
    public void process_connected() {
        underTest.process("{\n" +
                "    \"t\": \"connect.connected\",\n" +
                "    \"body\": {\n" +
                "}\n" +
                "} }");

        Mockito.verify(successfulConnectionChannel).send(any());
    }

    @Test
    public void process_connectionFailed() {
        underTest.process("{\n" +
                "    \"t\": \"connect.failed\",\n" +
                "    \"body\": {\n" +
                "        \"developerMessage\": \"Missing JWT Access Token in request\",\n" +
                "        \"errorCode\": \"RTF_002\"\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void process_portfolioPeformance() {
        underTest.process("{\"t\":\"portfolio.performance\"," +
                "\"id\":\"6148a3f5-4a42-11ec-a439-25b99b813335\",\"v\":1," +
                "\"body\":{\"accountValue\":{\"currency\":\"BUX\",\"decimals\":5,\"amount\":\"16564.87000\"}," +
                "\"performance\":\"0.0266\",\"suggestFunding\":false}}");

        Mockito.verify(portfolioValueChannel).send(doubleMessageArgumentCaptor.capture());
        Message<Double> message = doubleMessageArgumentCaptor.getValue();
        assert message.getPayload() == 16564.87000;
    }

    @Test
    public void process_tradingQuote() {
        underTest.process("{\n" +
                "   \"t\": \"trading.quote\",\n" +
                "   \"body\": {\n" +
                "      \"securityId\": \"p1\",\n" +
                "      \"currentPrice\": \"10692.3\"\n" +
                "   }\n" +
                "}");

        Mockito.verify(priceUpdateChannel).send(productPriceUpdateMessageArgumentCaptor.capture());
        var payload = productPriceUpdateMessageArgumentCaptor.getValue().getPayload();
        assert payload.getCurrentPrice() == 10692.3;
        assert Objects.equals(payload.getProductId(), "p1");
    }

    @Test
    public void process_unknown() {
        underTest.process("???");
    }
}