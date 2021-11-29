package buxtraderbot.marketagent;

import buxtraderbot.models.websocketmessages.Subscription;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * Unit test for {@link BuxMarketSocketListener} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class BuxMarketSocketListenerTest {
    private BuxMarketSocketListener underTest;

    @Mock
    private Session session;

    @Mock
    private RemoteEndpoint remote;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Test
    public void send() throws IOException {
        underTest = new BuxMarketSocketListener(null);
        ReflectionTestUtils.setField(underTest, "session", session);
        Mockito.when(session.getRemote()).thenReturn(remote);
        var subscription = new Subscription();
        subscription.setSubscribeTo(Arrays.asList("p1", "p2", "p3"));
        subscription.setUnsubscribeFrom(Arrays.asList("p4", "p5", "p6"));

        underTest.send(MessageBuilder.withPayload(subscription).build());

        Mockito.verify(remote).sendString(stringArgumentCaptor.capture());
        assert stringArgumentCaptor.getValue() != null;
        assert stringArgumentCaptor.getValue().equals("{\"subscribeTo\":[\"p1\",\"p2\",\"p3\"]," +
                "\"unsubscribeFrom\":[\"p4\",\"p5\",\"p6\"]}");
    }
}