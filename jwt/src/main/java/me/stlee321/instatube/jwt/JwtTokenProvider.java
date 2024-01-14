package me.stlee321.instatube.jwt;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Stream;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.accessTokenLifetime}")
    private Long accessTokenLifetime;
    @Value("${jwt.refreshTokenLifetime}")
    private Long refreshTokenLifetime;

    Key key;
    JwtParser jwtParser;

    SubjectValidator subjectValidator;
    public JwtTokenProvider(SubjectValidator subjectValidator) {
        this.subjectValidator = subjectValidator;
    }
    @PostConstruct
    void setJwtParser() {
        key = Keys.hmacShaKeyFor(Base64.getEncoder().encode(secret.getBytes()));
        jwtParser = Jwts.parserBuilder()
                .setSigningKey(key)
                .build();
    }

    public boolean validate(String token) {
        try {
            var body =jwtParser.parseClaimsJws(token).getBody();
            String subject = body.getSubject();
            if(!subjectValidator.isValidSubject(subject)) {
                return false;
            }
            return body.getExpiration().after(new Date());
        }catch(Exception e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        var tokenBody = jwtParser.parseClaimsJws(token).getBody();
        String username = tokenBody.getSubject();
        String[] authString = tokenBody.get("aut", String.class).split(",");
        var authorities = Stream.of(authString).map(SimpleGrantedAuthority::new).toList();
        return new UsernamePasswordAuthenticationToken(username, "", authorities);
    }
    public String createAccessToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(now.getTime() + accessTokenLifetime))
                .setIssuedAt(now)
                .claim("aut", "ALL")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    public String createRefreshToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(now.getTime() + refreshTokenLifetime))
                .setIssuedAt(now)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    public String getSubject(String token) {
        try {
            return jwtParser.parseClaimsJws(token).getBody().getSubject();
        }catch(Exception e) {
            return null;
        }
    }
}
