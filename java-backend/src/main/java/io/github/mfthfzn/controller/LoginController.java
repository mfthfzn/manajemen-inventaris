package io.github.mfthfzn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mfthfzn.dto.LoginRequest;
import io.github.mfthfzn.dto.LoginResponse;
import io.github.mfthfzn.entity.Name;
import io.github.mfthfzn.entity.User;
import io.github.mfthfzn.repository.LoginRepositoryImpl;
import io.github.mfthfzn.service.LoginServiceImpl;
import io.github.mfthfzn.util.JpaUtil;
import io.github.mfthfzn.util.JsonUtil;
import io.github.mfthfzn.util.ValidatorUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@WebServlet(urlPatterns = "/login")
public class LoginController extends HttpServlet {

  private final LoginServiceImpl loginService =
          new LoginServiceImpl(new LoginRepositoryImpl(JpaUtil.getEntityManagerFactory()));

  ObjectMapper objectMapper = JsonUtil.getObjectMapper();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String email = req.getParameter("email");
    String password = req.getParameter("password");

    LoginRequest loginRequest = new LoginRequest(email, password);
    Set<ConstraintViolation<Object>> constraintViolations = ValidatorUtil.validate(loginRequest);

    LoginResponse loginResponse = new LoginResponse();
    String json;
    PrintWriter writer = resp.getWriter();
    if (!constraintViolations.isEmpty()) {
      for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
        loginResponse.setMessage(constraintViolation.getMessage());
        break;
      }
      json = objectMapper.writeValueAsString(loginResponse);
      resp.setContentType("application/json");
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      writer.println(json);
      return;
    }

    if (!loginService.authenticate(loginRequest)) {
      loginResponse.setMessage("Email atau password yang Anda masukkan salah!");
      json = objectMapper.writeValueAsString(loginResponse);
      writer.println(json);
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    } else {
      User user = loginService.getUserByEmail(loginRequest);
      Name name = user.getName();
      loginResponse.setMessage("Berhasil login!");
      loginResponse.setName(name);
      loginResponse.setEmail(user.getEmail());
      loginResponse.setRole(user.getRole());

      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setContentType("application/json");

      json = objectMapper.writeValueAsString(loginResponse);
      writer.println(json);
    }
  }
}
