package com.nurkiewicz.webflux.demo.feed;

import reactor.core.publisher.Flux;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Comparator;

@Component
public class ArticlesStream {

    private final ArticleRepository articleRepository;

    final Sinks.Many<Article> queue = Sinks.many().multicast().onBackpressureBuffer(100);

    public ArticlesStream(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    /**
     * TODO (7) Create an infinite stream of new articles
     * @return
     */
    Flux<Article> newArticles() {
        return Flux.empty();
    }

    Mono<Article> save(Article article) {
        queue.tryEmitNext(article);
        return articleRepository.save(article);
    }

    Flux<Article> getNewest(int limit) {
        return articleRepository
                .findAll()
                .sort(Comparator.comparing(Article::getPublishedDate, Comparator.reverseOrder()))
                .take(limit);
    }

    public Sinks.Many<Article> getQueue() {
        return queue;
    }
}
