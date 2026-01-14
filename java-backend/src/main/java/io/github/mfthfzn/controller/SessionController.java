package io.github.mfthfzn.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mfthfzn.dto.JwtPayload;
import io.github.mfthfzn.dto.UserResponse;
import io.github.mfthfzn.exception.AccessTokenExpiredException;
import io.github.mfthfzn.exception.RefreshTokenExpiredException;
import io.github.mfthfzn.exception.TokenRequiredException;
import io.github.mfthfzn.repository.TokenRepositoryImpl;
import io.github.mfthfzn.service.TokenServiceImpl;
import io.github.mfthfzn.util.JpaUtil;
import io.github.mfthfzn.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = "/api/auth/session")
public class SessionController extends HttpServlet {

  private final TokenServiceImpl tokenService =
          new TokenServiceImpl(
                  new TokenRepositoryImpl(JpaUtil.getEntityManagerFactory())
          );

  ObjectMapper objectMapper = JsonUtil.getObjectMapper();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String accessToken = null;
    String refreshToken = null;
    resp.setContentType("application/json");
    UserResponse userResponse = new UserResponse();

    if (req.getCookies() != null) {
      try {
        for (Cookie cookie : req.getCookies()) {
          if (cookie.getName().equals("access_token")) accessToken = cookie.getValue();
          if (cookie.getName().equals("refresh_token")) refreshToken = cookie.getValue();
        }

        if (accessToken == null || refreshToken == null || accessToken.isEmpty() || refreshToken.isEmpty()) {
          throw new TokenRequiredException("Access token and refresh token required");
        }

        tokenService.verifyAccessToken(accessToken);

        JwtPayload jwtPayload = tokenService.getUserFromToken(accessToken);
        userResponse.setMessage("Access token is valid");
        userResponse.setData(Map.of(
                "email", jwtPayload.getEmail(),
                "name", jwtPayload.getName(),
                "role", jwtPayload.getRole(),
                "store_name", jwtPayload.getStoreName()
        ));
        String response = objectMapper.writeValueAsString(userResponse);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println(response);
      } catch (AccessTokenExpiredException accessTokenExpiredException) {
        try {

          tokenService.verifyRefreshToken(refreshToken);

          JwtPayload user = tokenService.getUserFromToken(refreshToken);

          // cek ke database
          String refreshTokenFromDatabase = tokenService.getRefreshToken(user.getEmail());

          if (!refreshTokenFromDatabase.equals(refreshToken)) throw new JWTVerificationException("Refresh Token Invalid");

          JwtPayload jwtPayload = tokenService.getUserFromToken(accessToken);
          String newAccessToken = tokenService.generateAccessToken(jwtPayload);

          // Cookie for access-token
          Cookie acessCookie = new Cookie("access_token", newAccessToken);
          acessCookie.setHttpOnly(true);
          acessCookie.setSecure(false);
          acessCookie.setPath("/");
          acessCookie.setMaxAge(60 * 60 * 24);
          resp.addCookie(acessCookie);

          userResponse.setMessage("Access token is valid");
          userResponse.setData(Map.of(
                  "email", jwtPayload.getEmail(),
                  "name", jwtPayload.getName(),
                  "role", jwtPayload.getRole(),
                  "store_name", jwtPayload.getStoreName()
          ));
          String response = objectMapper.writeValueAsString(userResponse);
          resp.setStatus(HttpServletResponse.SC_OK);
          resp.getWriter().println(response);
        }
        catch (RefreshTokenExpiredException refreshTokenExpiredException) {

          JwtPayload jwtPayload = tokenService.getUserFromToken(refreshToken);

          tokenService.removeRefreshToken(jwtPayload, refreshToken);

          Cookie acessCookie = new Cookie("access_token", "");
          acessCookie.setHttpOnly(true);
          acessCookie.setSecure(false);
          acessCookie.setPath("/");
          acessCookie.setMaxAge(0);
          resp.addCookie(acessCookie);

          // Cookie for refresh-token
          Cookie refreshCookie = new Cookie("refresh_token", "");
          refreshCookie.setHttpOnly(true);
          refreshCookie.setSecure(false);
          refreshCookie.setPath("/");
          refreshCookie.setMaxAge(0);
          resp.addCookie(refreshCookie);

          userResponse.setMessage("Failed to get data");
          userResponse.setError(Map.of(
                  "message", refreshTokenExpiredException.getMessage()
          ));
          resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          String response = objectMapper.writeValueAsString(userResponse);
          resp.getWriter().println(response);
        } catch (JWTVerificationException jwtVerificationException) {

          JwtPayload jwtPayload = tokenService.getUserFromToken(refreshToken);

          tokenService.removeRefreshToken(jwtPayload, refreshToken);

          Cookie acessCookie = new Cookie("access_token", "");
          acessCookie.setHttpOnly(true);
          acessCookie.setSecure(false);
          acessCookie.setPath("/");
          acessCookie.setMaxAge(0);
          resp.addCookie(acessCookie);

          // Cookie for refresh-token
          Cookie refreshCookie = new Cookie("refresh_token", "");
          refreshCookie.setHttpOnly(true);
          refreshCookie.setSecure(false);
          refreshCookie.setPath("/");
          refreshCookie.setMaxAge(0);
          resp.addCookie(refreshCookie);

          userResponse.setMessage("Failed to get data");
          userResponse.setError(Map.of(
                  "message", jwtVerificationException.getMessage()
          ));
          resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          String response = objectMapper.writeValueAsString(userResponse);
          resp.getWriter().println(response);
        }
      } catch (TokenRequiredException tokenRequiredException) {

        userResponse.setMessage("Failed to get data");
        userResponse.setError(Map.of(
                "message", tokenRequiredException.getMessage()
        ));
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String response = objectMapper.writeValueAsString(userResponse);
        resp.getWriter().println(response);

      } catch (JWTVerificationException jwtVerificationException) {

        JwtPayload jwtPayload = tokenService.getUserFromToken(refreshToken);

        tokenService.removeRefreshToken(jwtPayload, refreshToken);

        Cookie acessCookie = new Cookie("access_token", "");
        acessCookie.setHttpOnly(true);
        acessCookie.setSecure(false);
        acessCookie.setPath("/");
        acessCookie.setMaxAge(0);
        resp.addCookie(acessCookie);

        // Cookie for refresh-token
        Cookie refreshCookie = new Cookie("refresh_token", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        resp.addCookie(refreshCookie);

        userResponse.setMessage("Failed to get data");
        userResponse.setError(Map.of(
                "message", jwtVerificationException.getMessage()
        ));
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String response = objectMapper.writeValueAsString(userResponse);
        resp.getWriter().println(response);
      }
    } else {
      userResponse.setMessage("Failed to get data");
      userResponse.setError(Map.of(
              "message", "Access token and refresh token required"
      ));
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      String response = objectMapper.writeValueAsString(userResponse);
      resp.getWriter().println(response);
    }
  }

}
