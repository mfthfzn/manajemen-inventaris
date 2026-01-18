package io.github.mfthfzn.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import io.github.mfthfzn.dto.JwtPayload;
import io.github.mfthfzn.dto.UserResponse;
import io.github.mfthfzn.enums.InternalErrorCode;
import io.github.mfthfzn.exception.AccessTokenExpiredException;
import io.github.mfthfzn.exception.TokenRequiredException;
import io.github.mfthfzn.repository.TokenRepositoryImpl;
import io.github.mfthfzn.service.TokenServiceImpl;
import io.github.mfthfzn.util.JpaUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = "/api/auth/session")
public class SessionController extends BaseController {

  private final TokenServiceImpl tokenService =
          new TokenServiceImpl(
                  new TokenRepositoryImpl(JpaUtil.getEntityManagerFactory())
          );

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      String accessToken = getCookieValue(req, "access_token");

      if (accessToken == null || accessToken.isEmpty()) {
        throw new TokenRequiredException("Access token and refresh token required");
      }

      tokenService.verifyAccessToken(accessToken);
      JwtPayload jwtPayload = tokenService.getUserFromToken(accessToken);
      UserResponse userResponse = new UserResponse(
              jwtPayload.getEmail(),
              jwtPayload.getName(),
              jwtPayload.getRole(),
              jwtPayload.getStoreName()
      );
      sendSuccess(resp, HttpServletResponse.SC_OK, "Success get data", userResponse);

    } catch (TokenRequiredException tokenRequiredException) {
      sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Failed to get data", Map.of(
              "message", tokenRequiredException.getMessage(),
              "internal_error_code", InternalErrorCode.TOKEN_MISSING
      ));
    } catch (AccessTokenExpiredException accessTokenExpiredException) {
      sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Failed to get data", Map.of(
              "message", accessTokenExpiredException.getMessage(),
              "internal_error_code", InternalErrorCode.ACCESS_TOKEN_EXPIRED
      ));
    } catch (JWTVerificationException jwtVerificationException) {
      String refreshToken = getCookieValue(req, "refresh_token");
      tokenService.removeRefreshToken(tokenService.getUserFromToken(refreshToken).getEmail());
      removeCookie(resp, "access_token");
      removeCookie(resp, "refresh_token");

      sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Failed to get data", Map.of(
              "message", jwtVerificationException.getMessage(),
              "internal_error_code", InternalErrorCode.TOKEN_INVALID
      ));
    }
  }
}