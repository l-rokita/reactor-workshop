package com.nurkiewicz.reactor;

import com.nurkiewicz.reactor.pagehit.Country;
import com.nurkiewicz.reactor.pagehit.PageHit;
import com.nurkiewicz.reactor.pagehit.PageHits;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;

public class R074_GroupingStreams {

    private static final Logger log = LoggerFactory.getLogger(R074_GroupingStreams.class);

    /**
     * TODO Start with {@link PageHits#random()}, first group by country,
     * then count how many hits per second.
     */
    @Test
    public void groupByCountryEverySecond() throws Exception {
        Flux<Tuple2<Country, Long>> hitsPerSecond =
        //        .random()
        //        .window(Duration.ofSeconds(1))
        //        .flatMap(
        //                window -> window.groupBy(PageHit::getCountry).map(it -> it.map())
        //        );//TODO
        PageHits.random()
                .groupBy(PageHit::getCountry)
                .flatMap(
                        hitsPerCountry -> hitsPerCountry
                                .window(Duration.ofSeconds(1))
                                .flatMap(Flux::count)
                                .map(sum -> Tuples.of(hitsPerCountry.key(), sum))
                ).log();

        hitsPerSecond.blockLast();
    }

    /**
     * TODO Start with {@link PageHits#random()}, first group hits per second,
     * then count how many for each country.
     */
    @Test
    public void everySecondGroupByCountry() throws Exception {
        Flux<Tuple2<Country, Long>> hitsPerSecond = null; //TODO
    }

}
