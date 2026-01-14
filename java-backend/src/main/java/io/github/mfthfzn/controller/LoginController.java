package io.github.mfthfzn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mfthfzn.dto.AuthRequest;
import io.github.mfthfzn.dto.AuthResponse;
import io.github.mfthfzn.dto.JwtPayload;
import io.github.mfthfzn.repository.TokenRepositoryImpl;
import io.github.mfthfzn.repository.UserRepositoryImpl;
import io.github.mfthfzn.service.AuthServiceImpl;
import io.github.mfthfzn.service.TokenServiceImpl;
import io.github.mfthfzn.service.UserServiceImpl;
import io.github.mfthfzn.util.JpaUtil;
import io.github.mfthfzn.util.JsonUtil;
import io.github.mfthfzn.util.ValidatorUtil;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

@WebServlet(urlPatterns = "/api/auth/login")
public class LoginController extends HttpServlet {

  private final UserServiceImpl userService =
          new UserServiceImpl(
                  new UserRepositoryImpl(JpaUtil.getEntityManagerFactory())
          );

  private final TokenServiceImpl tokenService =
          new TokenServiceImpl(
                  new TokenRepositoryImpl(JpaUtil.getEntityManagerFactory())
          );

  private final AuthServiceImpl authService =
          new AuthServiceImpl(
                  userService, tokenService
          );

  ObjectMapper objectMapper = JsonUtil.getObjectMapper();

  private boolean isDuplicateEntryError(Throwable throwable) {
    Throwable current = throwable;
    while (current != null) {
      String message = current.getMessage();
      if (message != null && (message.contains("Duplicate entry") || message.contains("1062"))) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    AuthRequest authRequest = new AuthRequest(req.getParameter("email"), req.getParameter("password"));
    Set<ConstraintViolation<Object>> constraintViolations = ValidatorUtil.validate(authRequest);
    AuthResponse authResponse = new AuthResponse();
    String response;
    PrintWriter writer = resp.getWriter();
    resp.setContentType("application/json");

    if (!constraintViolations.isEmpty()) {
      for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
        authResponse.setMessage("Data request invalid");
        authResponse.setError(Map.of(
                "message", constraintViolation.getMessage()
        ));
        break;
      }
      response = objectMapper.writeValueAsString(authResponse);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      writer.println(response);
      return;
    }
    authResponse.setMessage("");

    try {
      authResponse = authService.authenticate(authRequest);
      if (!authResponse.isAuth()) {
        authResponse.setMessage("Login failed");
        authResponse.setError(Map.of(
                "message", "Email or password incorrect!"
        ));
        response = objectMapper.writeValueAsString(authResponse);
        writer.println(response);
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      } else {
        String refreshToken = authResponse.getRefreshToken();
        String accessToken = authResponse.getAccessToken();
        if (accessToken != null && refreshToken != null) {

          // Cookie for access-token
          Cookie acessCookie = new Cookie("access_token", accessToken);
          acessCookie.setHttpOnly(true);
          acessCookie.setSecure(false);
          acessCookie.setPath("/");
          acessCookie.setMaxAge(60 * 60 * 24);
          resp.addCookie(acessCookie);

          // Cookie for refresh-token
          Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
          refreshCookie.setHttpOnly(true);
          refreshCookie.setSecure(false);
          refreshCookie.setPath("/");
          refreshCookie.setMaxAge(60 * 60 * 24 * 30);
          resp.addCookie(refreshCookie);

          resp.setStatus(HttpServletResponse.SC_OK);
          authResponse.setMessage("Login Success!");
          authResponse.setData(Map.of(
                  "role", authResponse.getUserType().toString()
          ));

          response = objectMapper.writeValueAsString(authResponse);
          writer.println(response);
        }
      }
    } catch (PersistenceException persistenceException) {
      if (isDuplicateEntryError(persistenceException)) {
        authResponse.setMessage("Failed login");
        authResponse.setError(Map.of(
                "message", "The session token already exists."
        ));
        resp.setStatus(HttpServletResponse.SC_FOUND);

        String refreshToken = tokenService.getRefreshToken(req.getParameter("email"));
        JwtPayload jwtPayload = tokenService.getUserFromToken(refreshToken);
        String accessToken = tokenService.generateAccessToken(jwtPayload);

        // Cookie for refresh-token
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 30);
        resp.addCookie(refreshCookie);

        // Cookie for access-token
        Cookie acessCookie = new Cookie("access_token", accessToken);
        acessCookie.setHttpOnly(true);
        acessCookie.setSecure(false);
        acessCookie.setPath("/");
        acessCookie.setMaxAge(60 * 60 * 24);
        resp.addCookie(acessCookie);
      } else {
        authResponse.setMessage("Failed login");
        authResponse.setError(Map.of(
                "message", "An error occurred on the database server."
        ));
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      response = objectMapper.writeValueAsString(authResponse);
      writer.println(response);
    }
  }

}
