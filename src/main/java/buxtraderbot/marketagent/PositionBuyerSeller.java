package buxtraderbot.marketagent;

import buxtraderbot.models.contracts.ClosePositionResponseDto;
import buxtraderbot.models.contracts.InvestingAmount;
import buxtraderbot.models.contracts.OpenPositionRequestDto;
import buxtraderbot.models.contracts.OpenPositionResponseDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Objects;

@Service
public class PositionBuyerSeller {
    private final Logger logger = LogManager.getLogger(PositionBuyerSeller.class);

    @Value("${market.url.buy}")
    private String openPositionURL;

    @Value("${market.url.sell}")
    private String closePositionURL;

    @Value("${header.auth.token}")
    private String authToken;

    @Value("${header.accept.language}")
    private String acceptLanguage;

    private final RestTemplate restTemplate = new RestTemplate();

    public String openPosition(String productId, Double amount) {
        var dto = new OpenPositionRequestDto();
        dto.setProductId(productId);

        dto.setInvestingAmount(new InvestingAmount());
        dto.getInvestingAmount().setAmount(amount.toString());
        dto.getInvestingAmount().setCurrency("BUX");
        dto.getInvestingAmount().setDecimals(getDecimals(amount));

        dto.setLeverage(2);
        dto.setDirection("BUY");
        dto.setRiskWarningConfirmation("Alles goed");

        var entity = new HttpEntity<>(dto, getHeaders());
        try {
            var response = restTemplate.exchange(openPositionURL,
                    HttpMethod.POST,
                    entity,
                    OpenPositionResponseDto.class);
            return Objects.requireNonNull(response.getBody()).getPositionId();
        } catch (HttpClientErrorException e) {
            logger.error("Trade placement error", e);
            return null;
        }
    }

    public void closePosition(String positionId) {
        restTemplate.exchange(closePositionURL + positionId,
                HttpMethod.DELETE,
                new HttpEntity<>("", getHeaders()),
                ClosePositionResponseDto.class);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept-Language", acceptLanguage);
        return headers;
    }

    private int getDecimals(Double amount) {
        BigDecimal bigDecimal = new BigDecimal(String.valueOf(amount));
        int intValue = bigDecimal.intValue();
        return bigDecimal.subtract(
                new BigDecimal(intValue)).toPlainString().length() - 2;
    }
}
