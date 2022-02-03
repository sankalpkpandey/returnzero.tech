package tech.returnzero.microrest;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import tech.returnzero.microcontext.Context;

public class RestClient<T> {

    private RestTemplate restTemplate = Context.retrieve(RestTemplate.class);;
    private String url = null;
    private HttpMethod method = HttpMethod.POST;
    private Map<String, List<String>> headers = null;
    private Object request = null;
    private Class<T> responseType = null;

    public RestClient<T> url(String url) {
        this.url = url;
        return this;
    }

    public RestClient<T> method(HttpMethod method) {
        this.method = method;
        return this;
    }

    public RestClient<T> headers(Map<String, List<String>> headers) {
        this.headers = headers;
        return this;
    }

    public RestClient<T> responseType(Class<T> responseType) {
        this.responseType = responseType;
        return this;
    }

    public RestClient<T> request(Object request) {
        this.request = request;
        return this;
    }

    public T call() throws RestClientException {
        HttpEntity<Object> requestEntity = null;
        if (this.headers != null) {
            MultiValueMap<String, String> headermap = new LinkedMultiValueMap<>(this.headers);
            requestEntity = new HttpEntity<Object>(this.request, headermap);
        } else {
            requestEntity = new HttpEntity<Object>(this.request);
        }

        ResponseEntity<T> response = restTemplate.exchange(this.url, this.method, requestEntity,
                this.responseType);
        return response.getBody();

    }

}
