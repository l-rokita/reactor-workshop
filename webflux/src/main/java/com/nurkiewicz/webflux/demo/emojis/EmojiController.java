package com.nurkiewicz.webflux.demo.emojis;

import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@RestController
public class EmojiController {

    private final URI emojiTrackerUrl;
    private final WebClient webClient;

    public EmojiController(@Value("${emoji-tracker.url}") URI emojiTrackerUrl, WebClient webClient) {
        this.emojiTrackerUrl = emojiTrackerUrl;
        this.webClient = webClient;
    }

    @GetMapping(value = "/emojis/raw", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<ServerSentEvent> raw() {
        return webClient
                .get()
                .uri(emojiTrackerUrl)
                .retrieve()
                .bodyToFlux(ServerSentEvent.class);
    }

    @GetMapping(value = "/emojis/rps", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<Long> rps() {
        return webClient
                .get()
                .uri(emojiTrackerUrl)
                .retrieve()
                .bodyToFlux(ServerSentEvent.class)
                .window(Duration.ofSeconds(1))
                .flatMap(Flux::count);
    }

    @GetMapping(value = "/emojis/eps", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<Integer> eps() {
        return webClient
                .get()
                .uri(emojiTrackerUrl)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Integer>>() {})
                .flatMapIterable(Map::values)
                .window(Duration.ofSeconds(1))
                .flatMap(count->count.reduce(Integer::sum));
    }

    @GetMapping(value = "/emojis/aggregated", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<Map<String, Integer>> aggregated() {
        return Flux.empty();
    }

    /**
     * @see #topValues(Map, int)
     */
    @GetMapping(value = "/emojis/top", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<Map<String, Integer>> top(@RequestParam(defaultValue = "10", required = false) int limit) {
//        return aggregated()...
        return Flux.empty();
    }

    private <T> Map<T, Integer> topValues(Map<T, Integer> agg, int n) {
        return new HashMap<>(agg
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(reverseOrder()))
                .limit(n)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @GetMapping(value = "/emojis/topStr", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<String> topStr(@RequestParam(defaultValue = "10", required = false) int limit) {
        return Flux.empty();
    }

    String keysAsOneString(Map<String, Integer> m) {
        return m
                .keySet()
                .stream()
                .map(EmojiController::codeToEmoji)
                .collect(Collectors.joining());
    }

    static String codeToEmoji(String hex) {
        final String[] codes = hex.split("-");
        if (codes.length == 2) {
            return hexToEmoji(codes[0]) + hexToEmoji(codes[1]);
        } else {
            return hexToEmoji(hex);
        }
    }

    private static String hexToEmoji(String hex) {
        return new String(Character.toChars(Integer.parseInt(hex, 16)));
    }

}
