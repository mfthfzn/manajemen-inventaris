package io.github.mfthfzn.controller;

import io.github.mfthfzn.dto.*;
import io.github.mfthfzn.exception.AuthenticateException;
import io.github.mfthfzn.repository.TokenRepositoryImpl;
import io.github.mfthfzn.repository.UserRepositoryImpl;
import io.github.mfthfzn.service.AuthServiceImpl;
import io.github.mfthfzn.service.TokenServiceImpl;
import io.github.mfthfzn.util.JpaUtil;
import io.github.mfthfzn.util.ValidatorUtil;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@WebServlet(urlPatterns = "/api/auth/login")
public class LoginController extends BaseController {

  private final UserRepositoryImpl userRepository =
          new UserRepositoryImpl(JpaUtil.getEntityManagerFactory());

  private final TokenServiceImpl tokenService =
          new TokenServiceImpl(
                  new TokenRepositoryImpl(JpaUtil.getEntityManagerFactory())
          );

  private final AuthServiceImpl authService =
          new AuthServiceImpl(
                  userRepository, tokenService
          );

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    LoginRequest loginRequest = new LoginRequest(req.getParameter("email"), req.getParameter("password"));
    Set<ConstraintViolation<Object>> constraintViolations = ValidatorUtil.validate(loginRequest);

    if (!constraintViolations.isEmpty()) {
      for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
        sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Data request invalid", Map.of(
                "message", constraintViolation.getMessage()
        ));
        break;
      }
      return;
    }

    try {
      // cek email and password
      LoginResponse loginResponse = authService.authenticate(loginRequest);

      // generate token
      loginResponse.setAccessToken(tokenService.generateAccessToken(loginResponse));
      if (loginResponse.getUser().getToken() != null) {
        tokenService.removeRefreshToken(loginResponse.getUser().getEmail());
      }

      loginResponse.setRefreshToken(tokenService.generateRefreshToken(loginResponse));
      // save refresh token
      tokenService.saveRefreshToken(loginResponse);
      // Cookie for access-token
      addCookie(resp, "access_token", loginResponse.getAccessToken(), 60 * 60);
      // Cookie for refresh-token
      addCookie(resp, "refresh_token", loginResponse.getRefreshToken(), 60 * 60 * 24 * 7);

      UserResponse userResponse = new UserResponse();
      userResponse.setRole(loginResponse.getUser().getRole().toString());
      sendSuccess(resp, HttpServletResponse.SC_OK, "Login success", userResponse);

    } catch (PersistenceException persistenceException) {
      sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Login failed", Map.of(
              "message", "An error occurred on the database server."
      ));
    } catch (AuthenticateException authenticateException) {
      sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Login failed", Map.of(
              "message", authenticateException.getMessage()
      ));
    }
  }

}
