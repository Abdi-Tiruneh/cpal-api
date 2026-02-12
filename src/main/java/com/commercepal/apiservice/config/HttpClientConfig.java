package com.commercepal.apiservice.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * High-performance HTTP client configuration. Optimized for concurrent external API calls with
 * connection pooling.
 */
@Slf4j
@Configuration
public class HttpClientConfig {

  /**
   * Async HTTP Client with optimized connection pooling. Used for non-blocking external provider
   * calls.
   */
  @Bean
  public AsyncHttpClient asyncHttpClient() {
    DefaultAsyncHttpClientConfig config = Dsl.config()
        .setConnectTimeout(3000)                    // 3s connection timeout
        .setRequestTimeout(10000)                   // 10s request timeout
        .setReadTimeout(8000)                       // 8s read timeout
        .setMaxConnections(500)                     // Max total connections
        .setMaxConnectionsPerHost(100)              // Max per host
        .setConnectionTtl(60000)                    // Connection TTL 60s
        .setPooledConnectionIdleTimeout(30000)      // Idle timeout 30s
        .setKeepAlive(true)                         // Enable keep-alive
        .setCompressionEnforced(true)               // Enable compression
        .setFollowRedirect(true)                    // Follow redirects
        .setMaxRedirects(3)                         // Max redirect hops
        .setUseInsecureTrustManager(false)          // Secure SSL
        .setIoThreadsCount(Runtime.getRuntime().availableProcessors() * 2)
        .build();

    AsyncHttpClient client = Dsl.asyncHttpClient(config);

    log.info("Async HTTP Client initialized: maxConn=500, maxPerHost=100, timeout=10s");
    return client;
  }

  /**
   * Reactive WebClient with connection pooling. Alternative to AsyncHttpClient for reactive
   * streams.
   */
  @Bean
  public WebClient webClient() {
    // Configure connection provider with pooling
    ConnectionProvider connectionProvider = ConnectionProvider.builder("product-provider")
        .maxConnections(500)
        .maxIdleTime(Duration.ofSeconds(30))
        .maxLifeTime(Duration.ofMinutes(5))
        .pendingAcquireTimeout(Duration.ofSeconds(2))
        .evictInBackground(Duration.ofSeconds(60))
        .build();

    // Configure HTTP client with timeouts and compression
    HttpClient httpClient = HttpClient.create(connectionProvider)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
        .responseTimeout(Duration.ofSeconds(10))
        .doOnConnected(conn -> conn
            .addHandlerLast(new ReadTimeoutHandler(8000, TimeUnit.MILLISECONDS))
            .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS))
        )
        .compress(true)
        .keepAlive(true);

    WebClient client = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();

    log.info("WebClient initialized with connection pooling and timeouts");
    return client;
  }
}

