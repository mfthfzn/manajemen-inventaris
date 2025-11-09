package io.github.mfthfzn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mfthfzn.dto.LoginRequest;
import io.github.mfthfzn.dto.LoginResponse;
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
import java.util.Collection;
import java.util.Set;

@WebServlet(urlPatterns = "/login")
public class LoginController extends HttpServlet {

  private final LoginServiceImpl loginService =
          new LoginServiceImpl(new LoginRepositoryImpl(JpaUtil.getEntityManagerFactory()));

  ObjectMapper objectMapper = JsonUtil.getObjectMapper();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.getWriter().println("Hello World!");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String email = req.getParameter("email");
    String password = req.getParameter("password");

    LoginRequest loginRequest = new LoginRequest(email, password);
    Set<ConstraintViolation<Object>> constraintViolations = ValidatorUtil.validate(loginRequest);

    LoginResponse loginResponse = new LoginResponse();
    if (!constraintViolations.isEmpty()) {
      for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
        loginResponse.setResponse(constraintViolation.getMessage());
        break;
      }
      String json = objectMapper.writeValueAsString(loginResponse);
      resp.getWriter().println(json);
    } else {

    }



  }
}
