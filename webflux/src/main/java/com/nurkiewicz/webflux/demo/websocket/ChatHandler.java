package com.nurkiewicz.webflux.demo.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Sinks;

import java.time.Duration;

/**
 * TODO
 * <ol>
 *     <li>Use single sink to publish incoming messages</li>
 *     <li>Broadcast that sink to all listening subscribers</li>
 *     <li>New subscriber should receive last 5 messages before joining</li>
 *     <li>Add some logging: connecting/disconnecting, how many subscribers</li>
 * </ol>
 * Hint: Sink should hold {@link String}s, not {@link WebSocketMessage}s
 */
public class ChatHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatHandler.class);

    final Sinks.Many<String> sink = Sinks.many().replay().limit(5);

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(x -> log.info("[{}] Received: '{}'", session.getId(), x))
                .subscribe(sink::tryEmitNext);

        return session.send(
                sink
                        .asFlux()
                        .doOnNext(x -> log.info("[{}] Sending: '{}'", session.getId(), x))
                        .map(session::textMessage)
        );
    }

}