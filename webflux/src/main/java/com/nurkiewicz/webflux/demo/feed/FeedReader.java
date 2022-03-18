package com.nurkiewicz.webflux.demo.feed;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import com.google.common.io.CharStreams;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class FeedReader {

    private static final Logger log = LoggerFactory.getLogger(FeedReader.class);

    private final WebClient webClient;

    public FeedReader(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * TODO (3) Return <code>Flux&lt;SyndEntry&gt;</code>
     * Start by replacing {@link #get(URL)} with {@link #getAsync(URL)}.
     */
    public Flux<SyndEntry> fetch(URL url) {
        return getAsync(url).flatMapMany(this::convertEntry);
    }

    private Flux<SyndEntry> convertEntry(String feedBody) {
        return Mono.fromCallable(()-> doConvertAsync(feedBody))
                .subscribeOn(Schedulers.newBoundedElastic(10, 100, "Entry-Converter-Scheduler"))
                .flatMapIterable(Function.identity());
    }

    private List<SyndEntry> doConvertAsync(String feedBody) throws ParserConfigurationException, SAXException, IOException, FeedException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream is = new ByteArrayInputStream(applyAtomNamespaceFix(feedBody).getBytes(UTF_8));
        Document doc = builder.parse(is);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(doc);
        return feed.getEntries();
    }

    private String applyAtomNamespaceFix(String feedBody) {
        return feedBody.replace("https://www.w3.org/2005/Atom", "http://www.w3.org/2005/Atom");
    }

    private String get(URL url) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn.getResponseCode() == HttpStatus.MOVED_PERMANENTLY.value()) {
            return get(new URL(conn.getHeaderField("Location")));
        }
        try (final InputStreamReader reader = new InputStreamReader(conn.getInputStream(), UTF_8)) {
            return CharStreams.toString(reader);
        }
    }

    /**
     * TODO (2) Load data asynchronously using {@link org.springframework.web.reactive.function.client.WebClient}
     * wszczyknac webclient
     *
     * @see <a href="https://stackoverflow.com/questions/47655789/how-to-make-reactive-webclient-follow-3xx-redirects">How to make reactive webclient follow 3XX-redirects?</a>
     */
    Mono<String> getAsync(URL url) {
        return Mono.fromCallable(url::toURI)
                .flatMap(this::exchangeFor)
                .doOnError(e -> log.error("Error on reading url: {}", url, e))
                .onErrorResume(e -> Mono.empty());
    }

    @NotNull
    private Mono<String> exchangeFor(URI uri) {
        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.empty());
    }

}
