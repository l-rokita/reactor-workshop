package com.nurkiewicz.webflux.demo.feed;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.time.Duration;

import com.rometools.opml.feed.opml.Outline;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class FeedAggregator {

    private static final Logger log = LoggerFactory.getLogger(FeedAggregator.class);

    private final OpmlReader opmlReader;
    private final FeedReader feedReader;
    private final ArticleRepository articleRepository; //todo(me) za duzo tych zaleznosci
    private final ArticlesStream articlesStream;

    public FeedAggregator(OpmlReader opmlReader, FeedReader feedReader, ArticleRepository repository, ArticlesStream articlesStream) {
        this.opmlReader = opmlReader;
        this.feedReader = feedReader;
        this.articleRepository = repository;
        this.articlesStream = articlesStream;
    }

    /**
     * TODO (4) Read all feeds and store them into database
     * TODO (5) Repeat periodically, do not store duplicates
     */
    @PostConstruct
    public void init() {
        Flux
                .interval(Duration.ofMinutes(1))
                .flatMap(interval -> opmlReader.allFeedsStream())
                .map(Outline::getXmlUrl)
                .flatMap(this::convertURL)
                .flatMap(feedReader::fetch)
                .flatMap(Article::from)
                .filterWhen(this::alreadySaved)
                .flatMap(articlesStream::save)
                .subscribe();
    }

    @NotNull
    private Mono<URL> convertURL(String url) {
        return Mono.fromCallable(() -> new URL(url))
                .doOnError(e -> log.error("Cannot initialize FeedAggregator. Malformed URL: {}", url, e))
                .onErrorResume(e -> Mono.empty());
    }

    private Publisher<Boolean> alreadySaved(Article article) {
        return articleRepository.existsById(article.getLink()).hasElement();
    }
}
