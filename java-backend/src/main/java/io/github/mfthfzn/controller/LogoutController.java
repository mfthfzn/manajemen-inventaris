package io.github.mfthfzn.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mfthfzn.dto.AuthResponse;
import io.github.mfthfzn.dto.JwtPayload;
import io.github.mfthfzn.repository.TokenRepositoryImpl;
import io.github.mfthfzn.service.TokenServiceImpl;
import io.github.mfthfzn.util.JpaUtil;
import io.github.mfthfzn.util.JsonUtil;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = "/api/auth/logout")
public class LogoutController extends HttpServlet {

  private final TokenServiceImpl tokenService =
          new TokenServiceImpl(
                  new TokenRepositoryImpl(JpaUtil.getEntityManagerFactory())
          );

  ObjectMapper objectMapper = JsonUtil.getObjectMapper();

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String accessToken = null;
    String refreshToken = null;
    resp.setContentType("application/json");

    if (req.getCookies() != null) {
      try {
        for (Cookie cookie : req.getCookies()) {
          if (cookie.getName().equals("access_token")) {
            accessToken = cookie.getValue();
          }
          if (cookie.getName().equals("refresh_token")) {
            refreshToken = cookie.getValue();
          }
        }

        if (accessToken == null || refreshToken == null) {
          throw new JWTVerificationException("Access token and refresh token required");
        }

        JwtPayload jwtPayload = tokenService.getUserFromToken(refreshToken);
        tokenService.removeRefreshToken(jwtPayload, refreshToken);

        Cookie acessCookie = new Cookie("access_token", accessToken);
        acessCookie.setHttpOnly(true);
        acessCookie.setSecure(false);
        acessCookie.setPath("/");
        acessCookie.setMaxAge(0);
        resp.addCookie(acessCookie);

        // Cookie for refresh-token
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        resp.addCookie(refreshCookie);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("Success to logout");
        authResponse.setData(Map.of(
                "message", "Success to delete jwt token"
        ));

        String response = objectMapper.writeValueAsString(authResponse);
        resp.getWriter().println(response);
        resp.setStatus(HttpServletResponse.SC_OK);
      } catch (JWTVerificationException | PersistenceException exception) {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("Failed to logout");
        authResponse.setError(Map.of(
                "message", exception.getMessage()
        ));
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String response = objectMapper.writeValueAsString(authResponse);
        resp.getWriter().println(response);
      }
    } else {
      AuthResponse authResponse = new AuthResponse();
      authResponse.setMessage("Failed to logout");
      authResponse.setError(Map.of(
              "message", "Access token and refresh token required"
      ));
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      String response = objectMapper.writeValueAsString(authResponse);
      resp.getWriter().println(response);
    }

  }

}
