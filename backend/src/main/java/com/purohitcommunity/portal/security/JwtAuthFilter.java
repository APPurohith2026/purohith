package com.purohitcommunity.portal.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component @RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwt;
  private final CustomUserDetailsService uds;
  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
    String h=req.getHeader("Authorization");
    if(h!=null && h.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication()==null){
      try{
        String token=h.substring(7); String email=jwt.subject(token); UserDetails ud=uds.loadUserByUsername(email);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(ud,null,ud.getAuthorities()));
      }catch(Exception ignored){}
    }
    chain.doFilter(req,res);
  }
}
