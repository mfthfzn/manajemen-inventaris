package io.github.mfthfzn.service;

import io.github.mfthfzn.dto.SessionRequest;
import io.github.mfthfzn.dto.SessionResponse;
import io.github.mfthfzn.entity.User;

public interface SessionService {

  String generateToken(User user);

  SessionResponse getSession(SessionRequest sessionRequest);

  SessionResponse removeSession(SessionRequest sessionRequest);
}
