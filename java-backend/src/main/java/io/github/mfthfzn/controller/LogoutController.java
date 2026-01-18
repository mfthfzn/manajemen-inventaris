package io.github.mfthfzn.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mfthfzn.dto.LoginResponse;
import io.github.mfthfzn.dto.JwtPayload;
import io.github.mfthfzn.exception.TokenRequiredException;
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
public class LogoutController extends BaseController {

  private final TokenServiceImpl tokenService =
          new TokenServiceImpl(
                  new TokenRepositoryImpl(JpaUtil.getEntityManagerFactory())
          );

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {

      String accessToken = getCookieValue(req, "access_token");
      String refreshToken = getCookieValue(req, "refresh_token");

      if (accessToken == null || refreshToken == null) {
        throw new TokenRequiredException("Access token and refresh token required");
      }

      JwtPayload jwtPayload = tokenService.getUserFromToken(refreshToken);
      tokenService.removeRefreshToken(jwtPayload.getEmail());

      removeCookie(resp, "access_token");
      removeCookie(resp, "refresh_token");

      sendSuccess(resp, HttpServletResponse.SC_OK, "Success to logout", Map.of(
              "message", "Success to delete jwt token"
      ));
    } catch (JWTVerificationException | PersistenceException | TokenRequiredException exception) {
      sendSuccess(resp, HttpServletResponse.SC_UNAUTHORIZED, "Failed to logout", Map.of(
              "message", exception.getMessage()
      ));
    }

  }

}
