package io.github.mfthfzn.repository;

import io.github.mfthfzn.entity.Name;
import io.github.mfthfzn.entity.TokenSession;
import io.github.mfthfzn.entity.User;
import io.github.mfthfzn.enums.UserType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
public class TokenSessionRepositoryTest extends RepositoryTest {

  private TokenSessionRepositoryImpl tokenSessionRepository;
  private UserRepositoryImpl userRepository;

  public TokenSessionRepositoryTest() {
    this.tokenSessionRepository = new TokenSessionRepositoryImpl(entityManagerFactory);
    this.userRepository = new UserRepositoryImpl(entityManagerFactory);
  }

  @BeforeEach
  void setUp() {
    truncateAllTable();
  }

  @AfterEach
  void tearDown() {
    truncateAllTable();
  }

  @Test
  void testSetSessionAndFindByEmail() {

    transaction.begin();

    Name name = new Name();
    name.setFirstName("Eko");
    name.setMiddleName("Kurniawan");
    name.setLastName("Khannedy");

    User user = new User();
    String email = "eko@gmail.com";
    String password = "rahasia";
    user.setEmail(email);
    user.setName(name);
    user.setPassword(password);
    user.setRole(UserType.CASHIER);

    entityManager.persist(user);

    transaction.commit();

    transaction.begin();
    User userByEmail = userRepository.findUserByEmail(email);

    String token = UUID.randomUUID().toString();
    LocalDateTime expiredAt = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);

    boolean tokenSession = tokenSessionRepository.setTokenSession(userByEmail, token, expiredAt);
    transaction.commit();
    Assertions.assertTrue(tokenSession);

    TokenSession tokenSessionResult = tokenSessionRepository.findTokenByEmail(email);

    Assertions.assertEquals(token, tokenSessionResult.getToken());
    Assertions.assertEquals(email, tokenSessionResult.getEmail());
    Assertions.assertEquals(expiredAt, tokenSessionResult.getExpiredAt());

  }
}
