package io.github.mfthfzn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mfthfzn.dto.LoginRequest;
import io.github.mfthfzn.dto.LoginResponse;
import io.github.mfthfzn.dto.SessionRequest;
import io.github.mfthfzn.dto.SessionResponse;
import io.github.mfthfzn.repository.TokenSessionRepositoryImpl;
import io.github.mfthfzn.repository.UserRepositoryImpl;
import io.github.mfthfzn.service.AuthServiceImpl;
import io.github.mfthfzn.service.SessionServiceImpl;
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
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@WebServlet(urlPatterns = "/api/session")
@Slf4j
public class AuthController extends HttpServlet {

  private final UserServiceImpl userService =
          new UserServiceImpl(
                  new UserRepositoryImpl(JpaUtil.getEntityManagerFactory())
          );

  private final SessionServiceImpl sessionService =
          new SessionServiceImpl(
                  new TokenSessionRepositoryImpl(JpaUtil.getEntityManagerFactory())
          );

  private final AuthServiceImpl authService =
          new AuthServiceImpl(
                  userService, sessionService
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
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    SessionRequest sessionRequest = new SessionRequest(req.getParameter("email"), req.getParameter("token"));
    Set<ConstraintViolation<Object>> constraintViolations = ValidatorUtil.validate(sessionRequest);

    SessionResponse sessionResponse = new SessionResponse();
    String response;
    PrintWriter writer = resp.getWriter();

    if (!constraintViolations.isEmpty()) {
      for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
        sessionResponse.setMessage(constraintViolation.getMessage());
        break;
      }
      sessionResponse.setExpired(true);
      response = objectMapper.writeValueAsString(sessionResponse);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      writer.println(response);
      return;
    }

    try {
      sessionResponse = sessionService.getSession(sessionRequest);

      resp.setContentType("application/json");
      if (!sessionResponse.isExpired()) {
        sessionResponse.setMessage("Session ditemukan");
        response = objectMapper.writeValueAsString(sessionResponse);
        writer.println(response);
        resp.setStatus(HttpServletResponse.SC_OK);
      } else {
        sessionResponse.setMessage("Session tidak ditemukan");
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response = objectMapper.writeValueAsString(sessionResponse);
        writer.println(response);
      }
    } catch (PersistenceException persistenceException) {
      sessionResponse.setMessage("Terjadi kesalahan di server database.");
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response = objectMapper.writeValueAsString(sessionResponse);
      writer.println(response);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    LoginRequest loginRequest = new LoginRequest(req.getParameter("email"), req.getParameter("password"));
    Set<ConstraintViolation<Object>> constraintViolations = ValidatorUtil.validate(loginRequest);
    LoginResponse loginResponse = new LoginResponse();
    String response;
    PrintWriter writer = resp.getWriter();
    resp.setContentType("application/json");

    if (!constraintViolations.isEmpty()) {
      for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
        loginResponse.setMessage(constraintViolation.getMessage());
        break;
      }
      response = objectMapper.writeValueAsString(loginResponse);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      writer.println(response);
      return;
    }

    try {
      loginResponse = authService.authenticate(loginRequest);
      if (!loginResponse.isAuth()) {
        loginResponse.setMessage("Email atau password yang Anda masukkan salah!");
        response = objectMapper.writeValueAsString(loginResponse);
        writer.println(response);
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      } else {
        String token;
        if ((token = loginResponse.getToken()) != null) {
          log(token);
          // Cookie for session-token
          Cookie cookieToken = new Cookie("tokenSession", token);
          cookieToken.setHttpOnly(false);
          cookieToken.setSecure(false);
          cookieToken.setPath("/");
          cookieToken.setMaxAge(60 * 60 * 24);
          resp.addCookie(cookieToken);

          // Cookie for email
          Cookie cookieEmail = new Cookie("email", loginResponse.getEmail());
          cookieEmail.setHttpOnly(false);
          cookieEmail.setSecure(false);
          cookieEmail.setMaxAge(60 * 60 * 24);
          cookieEmail.setPath("/");
          resp.addCookie(cookieEmail);

          // Cookie for email
          Cookie cookieName = new Cookie("name", loginResponse.getName().getFirstName() + "-" +
                  loginResponse.getName().getMiddleName() + "-" + loginResponse.getName().getLastName());
          cookieName.setHttpOnly(false);
          cookieName.setSecure(false);
          cookieName.setMaxAge(60 * 60 * 24);
          cookieName.setPath("/");
          resp.addCookie(cookieName);

          // Cookie for role
          Cookie cookieRole = new Cookie("role", loginResponse.getRole().toString());
          cookieRole.setHttpOnly(false);
          cookieRole.setSecure(false);
          cookieRole.setMaxAge(60 * 60 * 24);
          cookieRole.setPath("/");
          resp.addCookie(cookieRole);

          resp.setStatus(HttpServletResponse.SC_OK);
          loginResponse.setMessage("Berhasil login!");

          response = objectMapper.writeValueAsString(loginResponse);
          writer.println(response);
        }
      }
    } catch (PersistenceException persistenceException) {
      if (isDuplicateEntryError(persistenceException)) {
        loginResponse.setMessage("Token session sudah ada.");
        resp.setStatus(HttpServletResponse.SC_FOUND);
      }  else {
        loginResponse.setMessage("Terjadi kesalahan di server database.");
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      response = objectMapper.writeValueAsString(loginResponse);
      writer.println(response);
    }
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    SessionRequest sessionRequest = new SessionRequest(req.getParameter("email"), req.getParameter("token"));
    Set<ConstraintViolation<Object>> constraintViolations = ValidatorUtil.validate(sessionRequest);

    SessionResponse sessionResponse = new SessionResponse();
    String response;
    PrintWriter writer = resp.getWriter();

    if (!constraintViolations.isEmpty()) {
      for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
        sessionResponse.setMessage(constraintViolation.getMessage());
        break;
      }
      sessionResponse.setExpired(true);
      response = objectMapper.writeValueAsString(sessionResponse);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      writer.println(response);
      return;
    }
    try {
      sessionResponse = sessionService.removeSession(sessionRequest);
      resp.setContentType("application/json");
      if (sessionResponse.isRemoved()) {
        sessionResponse.setMessage("Session berhasil dihapus");

        // DELETE Cookie for session-token
        Cookie cookieToken = new Cookie("tokenSession", "");
        cookieToken.setHttpOnly(false);
        cookieToken.setSecure(false);
        cookieToken.setPath("/");
        cookieToken.setMaxAge(0);
        resp.addCookie(cookieToken);

        // DELETE Cookie for email
        Cookie cookieEmail = new Cookie("email", "");
        cookieEmail.setHttpOnly(false);
        cookieEmail.setSecure(false);
        cookieEmail.setPath("/");
        cookieEmail.setMaxAge(0);
        resp.addCookie(cookieEmail);

        // DELETE Cookie for email
        Cookie cookieName = new Cookie("name", "");
        cookieName.setHttpOnly(false);
        cookieName.setSecure(false);
        cookieName.setPath("/");
        cookieName.setMaxAge(0);
        resp.addCookie(cookieName);

        // DELETE Cookie for role
        Cookie cookieRole = new Cookie("role", "");
        cookieRole.setHttpOnly(false);
        cookieRole.setSecure(false);
        cookieRole.setPath("/");
        cookieRole.setMaxAge(0);
        resp.addCookie(cookieRole);

        response = objectMapper.writeValueAsString(sessionResponse);
        writer.println(response);
        resp.setStatus(HttpServletResponse.SC_OK);
      } else {
        sessionResponse.setMessage("Session tidak berhasil dihapus");
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response = objectMapper.writeValueAsString(sessionResponse);
        writer.println(response);
      }
    } catch (PersistenceException persistenceException) {
      sessionResponse.setMessage("Terjadi kesalahan di server database.");
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response = objectMapper.writeValueAsString(sessionResponse);
      writer.println(response);
    }

  }
}
