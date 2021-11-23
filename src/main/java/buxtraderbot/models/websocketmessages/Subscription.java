package buxtraderbot.models.websocketmessages;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Subscription {
    private List<String> subscribeTo;
    private List<String> unsubscribeFrom;
}
