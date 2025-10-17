package com.java.slms.util;

import com.java.slms.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil
{
    // load from application.yml / env in real app
    private final String JWT_SECRET = "jwt.secret";
    private final long jwtExpirationInMs = 1000L * 60 * 60; // 1 hour

    private final Key key = Keys.hmacShaKeyFor(ConfigUtil.getRequired(JWT_SECRET).getBytes());

    public String generateToken(UserDetails userDetails)
    {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationInMs);

        // Cast to CustomUserDetails to get the schoolId
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        Long schoolId = customUserDetails.getSchoolId();

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().stream().map(Object::toString).toList())
                .claim("schoolId", schoolId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails)
    {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String extractUsername(String token)
    {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token)
    {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
    {
        final Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token)
    {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateTokenWithRoles(String token, UserDetails userDetails)
    {
        final String username = extractUsername(token);
        if (!username.equals(userDetails.getUsername()) || isTokenExpired(token))
        {
            return false;
        }

        // Extract roles from token claims
        Claims claims = extractAllClaims(token);
        List<String> tokenRoles = claims.get("roles", List.class);

        // Get roles from userDetails (DB)
        Set<String> dbRoles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Compare sets
        return tokenRoles != null && dbRoles.containsAll(tokenRoles) && tokenRoles.containsAll(dbRoles);
    }

    private Claims extractAllClaims(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractSchoolId(String token)
    {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Extract schoolId from the claims
        return claims.get("schoolId", Long.class);
    }


}
