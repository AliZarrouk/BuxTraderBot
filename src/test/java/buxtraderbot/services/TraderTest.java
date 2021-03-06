package buxtraderbot.services;

import buxtraderbot.marketagent.PositionBuyerSeller;
import buxtraderbot.models.ProductPriceUpdate;
import buxtraderbot.models.ProductSellingBuyingPrices;
import buxtraderbot.models.websocketmessages.Subscription;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link Trader} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class TraderTest {

    private Trader underTest;

    @Mock
    private MessageChannel productSubscriptionChannel;

    @Mock
    private PositionBuyerSeller positionBuyerSeller;

    @Mock
    private ProductPositionDal productPositionDal;

    @Captor
    private ArgumentCaptor<Message<Subscription>> subscriptionMessageArgumentCaptor;

    @Before
    public void setUp() {
        underTest = new Trader(productSubscriptionChannel,
                positionBuyerSeller,
                productPositionDal);
        underTest.processPortfolioValueUpdate(MessageBuilder.withPayload(500d).build());
        var productsMap = new HashMap<String, ProductSellingBuyingPrices>();
        var productSellingBuyingPrice = new ProductSellingBuyingPrices();
        productSellingBuyingPrice.setBuyPrice(10.04);
        productSellingBuyingPrice.setLowerSellPrice(8.09);
        productSellingBuyingPrice.setUpperSellPrice(12.09);
        productsMap.put("p1", productSellingBuyingPrice);
        ReflectionTestUtils.setField(underTest, "productSellingBuyingPricesMap", productsMap);
    }

    @After
    public void tearDown() {
        Mockito.verifyNoMoreInteractions(positionBuyerSeller);
        Mockito.verifyNoMoreInteractions(productPositionDal);
        Mockito.verifyNoMoreInteractions(productSubscriptionChannel);
    }

    @Test
    public void processOrderUpdate_goodSellPrice_SellAll() {
        // arrange
        Mockito.when(productPositionDal.getProductPositions("p1"))
                .thenReturn(Arrays.asList("1", "2", "3", "4"));
        var productPriceUpdate = new ProductPriceUpdate();
        productPriceUpdate.setProductId("p1");
        productPriceUpdate.setCurrentPrice(15.83);
        var message = MessageBuilder.withPayload(productPriceUpdate).build();

        // act
        underTest.processOrderUpdate(message);

        // assert
        Mockito.verify(productPositionDal, Mockito.times(3)).getProductPositions("p1");
        Mockito.verify(positionBuyerSeller).closePosition("1");
        Mockito.verify(positionBuyerSeller).closePosition("2");
        Mockito.verify(positionBuyerSeller).closePosition("3");
        Mockito.verify(positionBuyerSeller).closePosition("4");
        Mockito.verify(productPositionDal).sellPositionsForProductId("p1");
    }

    @Test
    public void processOrderUpdate_badSellPrice_SellAll() {
        // arrange
        Mockito.when(productPositionDal.getProductPositions("p1"))
                .thenReturn(Arrays.asList("1", "2", "3", "4"));
        var productPriceUpdate = new ProductPriceUpdate();
        productPriceUpdate.setProductId("p1");
        productPriceUpdate.setCurrentPrice(5.83);
        var message = MessageBuilder.withPayload(productPriceUpdate).build();

        // act
        underTest.processOrderUpdate(message);

        // assert
        Mockito.verify(productPositionDal, Mockito.times(3)).getProductPositions("p1");
        Mockito.verify(positionBuyerSeller).closePosition("1");
        Mockito.verify(positionBuyerSeller).closePosition("2");
        Mockito.verify(positionBuyerSeller).closePosition("3");
        Mockito.verify(positionBuyerSeller).closePosition("4");
        Mockito.verify(productPositionDal).sellPositionsForProductId("p1");
    }

    @Test
    public void processOrderUpdate_goodBuyPrice_Buy() {
        // arrange
        Mockito.when(positionBuyerSeller.openPosition("p1", 150d))
                .thenReturn("1");
        var productPriceUpdate = new ProductPriceUpdate();
        productPriceUpdate.setProductId("p1");
        productPriceUpdate.setCurrentPrice(9.53);
        var message = MessageBuilder.withPayload(productPriceUpdate).build();

        // act
        underTest.processOrderUpdate(message);

        // assert
        Mockito.verify(productPositionDal, Mockito.times(2)).getProductPositions("p1");
        Mockito.verify(positionBuyerSeller).openPosition("p1", 150d);
        Mockito.verify(productPositionDal).registerPositionForProductId("p1", "1");
    }

    @Test
    public void processOrderUpdate_amazingBuyPrice_AllIn() {
        // arrange
        Mockito.when(positionBuyerSeller.openPosition("p1", 500d))
                .thenReturn("1");
        var productPriceUpdate = new ProductPriceUpdate();
        productPriceUpdate.setProductId("p1");
        productPriceUpdate.setCurrentPrice(2.83);
        var message = MessageBuilder.withPayload(productPriceUpdate).build();

        // act
        underTest.processOrderUpdate(message);

        // assert
        Mockito.verify(productPositionDal, Mockito.times(2)).getProductPositions("p1");
        Mockito.verify(positionBuyerSeller).openPosition("p1", 500.0d);
        Mockito.verify(productPositionDal).registerPositionForProductId("p1", "1");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void processConnectionSuccessful() {
        // arrange
        var classLoader = getClass().getClassLoader();
        var resource = classLoader.getResource("product-buy-sell-price-config");
        assert resource != null;
        ReflectionTestUtils.setField(underTest, "productBuySellFilePath",
                resource.getFile());
        ReflectionTestUtils.setField(underTest, "productBuySellFileSeparator",
                "-");


        // act
        underTest.processConnectionSuccessful(MessageBuilder.withPayload("").build());

        // assert
        Mockito.verify(productSubscriptionChannel).send(subscriptionMessageArgumentCaptor.capture());
        var subscription = subscriptionMessageArgumentCaptor.getValue().getPayload();

        assert subscription.getSubscribeTo().size() == 2;
        assert subscription.getSubscribeTo().contains("trading.product.p1");
        assert subscription.getSubscribeTo().contains("trading.product.p2");

        var productSellingBuyingPricesMap = (Map<String, ProductSellingBuyingPrices>) ReflectionTestUtils
                .getField(underTest, "productSellingBuyingPricesMap");
        assert productSellingBuyingPricesMap != null;
        var p1 = productSellingBuyingPricesMap.get("p1");
        assert p1 != null;
        assert p1.getUpperSellPrice() == 30;
        assert p1.getLowerSellPrice() == 15;
        assert p1.getBuyPrice() == 21;

        var p2 = productSellingBuyingPricesMap.get("p2");
        assert p2 != null;
        assert p2.getUpperSellPrice() == 11.3;
        assert p2.getLowerSellPrice() == 6.2;
        assert p2.getBuyPrice() == 8.98;
    }
}