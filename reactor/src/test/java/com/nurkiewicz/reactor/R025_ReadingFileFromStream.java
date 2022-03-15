package com.nurkiewicz.reactor;

import com.nurkiewicz.reactor.samples.NotFound;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class R025_ReadingFileFromStream {

    private static final Logger log = LoggerFactory.getLogger(R025_ReadingFileFromStream.class);

    /**
     * TODO Read <code>/logback-test.xml</code> file using {@link BufferedReader#lines()} and {@link Flux#fromStream(Supplier)}
     * <p>Hint: use {@link #open(String)} helper method</p>
     */
    @Test
    public void readFileAsStreamOfLines() throws Exception {
        //when
        final Flux<String> lines = Flux.fromStream(() -> open("/logback-test.xml").lines());

        //then
        final Long count = lines
                .count()
                .block();
        assertThat(count).isEqualTo(12);
    }

    private BufferedReader open(String path) {
        final InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new NotFound(path);
        }
        return new BufferedReader(new InputStreamReader(stream));
    }

    /**
     * TODO Use {@link Flux#defer(Supplier)} in order to make eager stream lazy
     */
    @Test
    public void readingFileShouldBeLazy() throws Exception {
        //when
        final Flux<String> lines = Flux.defer(this::notFound);

        //then
        lines
                .as(StepVerifier::create)
                .verifyError(NotFound.class);
    }

    /**
     * Don't change this method!
     */
    private Flux<String> notFound() {
        return Flux.fromStream(open("404.txt").lines());
    }

}

