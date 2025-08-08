package com.eatcloud.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
@EnableConfigurationProperties
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}

@RestController
@Profile({"default", "local", "dev"})
class DevTokenController {
    @Value("${jwt.secret:your-256-bit-secret-your-256-bit-secret-key-here}")
    private String jwtSecret;

    @GetMapping("/dev/token")
    public Map<String, String> issueToken(
        @RequestParam(defaultValue = "123") String sub,
        @RequestParam(defaultValue = "customer") String type,
        @RequestParam(defaultValue = "3600") long expiresIn
    ) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", type);
        Instant now = Instant.now();
        String token = Jwts.builder()
            .setSubject(sub)
            .addClaims(claims)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusSeconds(expiresIn)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
        Map<String, String> res = new HashMap<>();
        res.put("token", token);
        return res;
    }
}
