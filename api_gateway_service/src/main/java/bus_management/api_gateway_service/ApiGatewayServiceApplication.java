package bus_management.api_gateway_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class ApiGatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayServiceApplication.class, args);
	}

	// CORS cấu hình bằng mã để chắc chắn áp dụng
	@Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 1. Cho phép Cookie và thông tin xác thực
        config.setAllowCredentials(true);
        
        // 2. QUAN TRỌNG NHẤT: Dùng Pattern "*" để chấp nhận MỌI NGUỒN
        // (Không dùng addAllowedOrigin("*") vì nó xung đột với Credentials)
        config.addAllowedOriginPattern("*"); 
        
        // 3. Chấp nhận mọi Header và Method
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

	// Logging filter để debug (có thể tắt sau khi ổn định)
	@Bean
	public GlobalFilter loggingGlobalFilter() {
		Logger log = LoggerFactory.getLogger("GatewayLogger");
		return (exchange, chain) -> {
			String path = exchange.getRequest().getURI().toString();
			String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "?";
			log.info("[GW REQ] {} {}", method, path);
			return chain.filter(exchange).then(Mono.fromRunnable(() -> {
				log.info("[GW RESP] {} {} -> status {}", method, path, exchange.getResponse().getStatusCode());
			}));
		};
	}

}
