package tech.returnzero.microgateway.filter;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

	private static final String AUTHORIZATION_HEADER_IS_INVALID = "Authorization header is invalid";
	private static final String AUTHORIZATION_HEADER_IS_MISSING_IN_REQUEST = "Authorization header is missing in request";
	private static final String AUTHORIZATION = "Authorization";

	@Value("${auth.whitelistesuris}")
	private List<String> whitelistesuris;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${auth.securityurl}")
	private String securityurl;

	@Value("${auth.tokename}")
	private String jwttokenname;

	public AuthenticationFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {

		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			if (!whitelistesuris.contains(request.getURI().getPath())) {
				if (this.isAuthMissing(request)) {
					return this.onError(exchange, AUTHORIZATION_HEADER_IS_MISSING_IN_REQUEST,
							HttpStatus.UNAUTHORIZED);
				} else {
					if (!validateToken(this.getAuthHeader(request), exchange)) {
						return this.onError(exchange, AUTHORIZATION_HEADER_IS_INVALID, HttpStatus.UNAUTHORIZED);
					} else {
						return chain.filter(exchange);
					}
				}
			} else {
				return chain.filter(exchange);
			}
		};
	}

	private boolean validateToken(String token, ServerWebExchange exchange) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(AUTHORIZATION, token);
		HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(headers);
		ResponseEntity<String> reponseEntity = restTemplate.exchange(securityurl, HttpMethod.POST,
				entity, String.class);
		if (reponseEntity.getBody() == null) {
			return false;
		}
		if (reponseEntity.getStatusCode().is2xxSuccessful()) {
			this.populateRequestWithHeaders(exchange, reponseEntity.getBody());
			return true;
		}
		return false;
	}

	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);
		return response.setComplete();
	}

	private String getAuthHeader(ServerHttpRequest request) {
		return request.getHeaders().getOrEmpty(AUTHORIZATION).get(0);
	}

	private boolean isAuthMissing(ServerHttpRequest request) {
		return !request.getHeaders().containsKey(AUTHORIZATION);
	}

	private void populateRequestWithHeaders(ServerWebExchange exchange, String user) {
		exchange.getRequest().mutate().header(jwttokenname, user).build();
	}

	public static class Config {
		// No implementation required for now.
	}

}
