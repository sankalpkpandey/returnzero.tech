package tech.returnzero.greyhoundengine.restclient;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

public class RestClient {

    private String url;

    private Integer timeout;

    private String contenttype;

    private HttpHeaders headers;

    private String accept;

    private Map<String, Object> body;

    private Class<?> monotype = Map.class;

    private RestClient() {
        // no impl
    }

    public RestClient url(String url) {
        this.url = url;
        return this;
    }

    public RestClient accept(String accept) {
        this.accept = accept;
        return this;
    }

    public RestClient headers(Map<String, String> headers) {
        this.headers = new HttpHeaders();
        this.headers.setAll(headers);
        return this;
    }

    public RestClient body(Map<String, Object> body) {
        this.body = body;
        return this;
    }

    public RestClient timeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    public RestClient contenttype(String contenttype) {
        this.contenttype = contenttype;
        return this;
    }

    public static final RestClient build() {
        return new RestClient();
    }

    public Object work() throws Exception {
        return this.post();
    }

    private Object post() throws Exception {

        final HttpClient httpclient = HttpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.MILLISECONDS));
                });

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpclient))
                .build().post().uri(new URI(this.url))
                .contentType(MediaType.valueOf(this.contenttype))
                .accept(MediaType.valueOf(this.accept))
                .body(Mono.just(body), Map.class).headers(httpHeaders -> {
                    httpHeaders.addAll(this.headers);
                })
                .retrieve()
                .bodyToMono(monotype).publishOn(Schedulers.boundedElastic()).block();
    }
}
