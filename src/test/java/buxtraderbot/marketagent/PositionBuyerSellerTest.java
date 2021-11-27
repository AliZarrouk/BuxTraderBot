package buxtraderbot.marketagent;

import buxtraderbot.models.contracts.ClosePositionResponseDto;
import buxtraderbot.models.contracts.OpenPositionRequestDto;
import buxtraderbot.models.contracts.OpenPositionResponseDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;

/**
 * Unit test for {@link PositionBuyerSeller} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class PositionBuyerSellerTest {
    private PositionBuyerSeller underTest;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    ResponseEntity<OpenPositionResponseDto> responseEntity;

    @Captor
    private ArgumentCaptor<HttpEntity<OpenPositionRequestDto>> httpEntityOpenRequestArgumentCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity> httpEntityCloseRequestArgumentCaptor;

    @Before
    public void setUp() {
        underTest = new PositionBuyerSeller();
        ReflectionTestUtils.setField(underTest, "openPositionURL", "open");
        ReflectionTestUtils.setField(underTest, "closePositionURL", "close/");
        ReflectionTestUtils.setField(underTest, "authToken", "token");
        ReflectionTestUtils.setField(underTest, "acceptLanguage", "lang");
        ReflectionTestUtils.setField(underTest, "restTemplate", restTemplate);
    }

    @After
    public void tearDown() {
        Mockito.verifyNoMoreInteractions(restTemplate);
    }

    @Test
    public void openPosition() {
        // arrange
        Mockito.when(restTemplate.exchange(eq("open"),
                eq(HttpMethod.POST),
                any(),
                eq(OpenPositionResponseDto.class)
                )).thenReturn(responseEntity);
        var responseDto = new OpenPositionResponseDto();
        responseDto.setPositionId("1");
        Mockito.when(responseEntity.getBody()).thenReturn(responseDto);

        // act
        String positionId = underTest.openPosition("p1", 234.233);

        // assert
        Assertions.assertEquals("1", positionId);
        Mockito.verify(restTemplate).exchange(eq("open"),
                eq(HttpMethod.POST),
                httpEntityOpenRequestArgumentCaptor.capture(),
                eq(OpenPositionResponseDto.class)
        );
        HttpEntity<OpenPositionRequestDto> entity = httpEntityOpenRequestArgumentCaptor.getValue();
        verifyHeaders(entity.getHeaders());
        var body = entity.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals("p1", body.getProductId());
        Assertions.assertEquals("BUY", body.getDirection());
        Assertions.assertNull(body.getSource());
        Assertions.assertEquals("Alles goed", body.getRiskWarningConfirmation());
        Assertions.assertEquals(2, body.getLeverage());
        Assertions.assertNotNull(body.getInvestingAmount());
        Assertions.assertEquals("234.233", body.getInvestingAmount().getAmount());
        Assertions.assertEquals("BUX", body.getInvestingAmount().getCurrency());
        Assertions.assertEquals(3, body.getInvestingAmount().getDecimals());
    }

    @Test
    public void closePosition() {
        // act
        underTest.closePosition("p1");

        // assert
        Mockito.verify(restTemplate).exchange(eq("close/p1"),
                eq(HttpMethod.DELETE),
                httpEntityCloseRequestArgumentCaptor.capture(),
                eq(ClosePositionResponseDto.class)
        );
        var entity = httpEntityCloseRequestArgumentCaptor.getValue();
        verifyHeaders(entity.getHeaders());
    }

    private void verifyHeaders(HttpHeaders headers) {
        Assertions.assertEquals(4, headers.size());
        Assertions.assertEquals(1, headers.getAccept().size());
        Assertions.assertEquals(MediaType.APPLICATION_JSON, headers.getAccept().get(0));
        Assertions.assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        Assertions.assertNotNull(headers.get("Accept-Language"));
        Assertions.assertEquals(1, headers.get("Accept-Language").size());
        Assertions.assertEquals("lang", headers.get("Accept-Language").get(0));
        Assertions.assertEquals(1, headers.get("Authorization").size());
        Assertions.assertEquals("Bearer token", headers.get("Authorization").get(0));
    }
}
