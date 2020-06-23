package org.onedatashare.transfer.service;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ODSAuthenticationManager implements ReactiveAuthenticationManager {
    @Autowired
    private JWTUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();

        String userName = null;
        try{
            userName = jwtUtil.getEmailFromToken(authToken);
        } catch (Exception e){
            Mono.empty();
        }

        if(userName != null && jwtUtil.validateToken(authToken)){
            Claims claims = jwtUtil.getAllClaimsFromToken(authToken);
            List<String> roleList = claims.get("role", List.class);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userName,
                    authToken,
                    null
            );
            return Mono.just(auth);
        }
        return Mono.empty();
    }
}
