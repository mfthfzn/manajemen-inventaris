package io.github.mfthfzn.repository;

import io.github.mfthfzn.entity.Name;
import io.github.mfthfzn.entity.Store;
import io.github.mfthfzn.entity.TokenSession;
import io.github.mfthfzn.entity.User;
import io.github.mfthfzn.enums.UserType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
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
    Store store = new Store();
    store.setName("Toko Cabang Jakarta");
    store.setAddress("Jl. Sudirman No. 45, Jakarta Pusat");
    store.setCreatedAt(LocalDateTime.now());
    store.setUpdatedAt(LocalDateTime.now());
    entityManager.persist(store);

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
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    user.setStore(store);

    entityManager.persist(user);
    transaction.commit();

    Optional<User> userByEmailOptional = userRepository.findUserByEmail(email);
    if (userByEmailOptional.isPresent()) {
      User userByEmail = userByEmailOptional.get();
      log.info(userByEmail.toString());
      String token = UUID.randomUUID().toString();
      LocalDateTime expiredAt = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);

      TokenSession tokenSession = new TokenSession();
      tokenSession.setToken(token);
      tokenSession.setUser(userByEmail);
      tokenSession.setExpiredAt(expiredAt);

      tokenSessionRepository.saveTokenSession(tokenSession);

      Optional<TokenSession> tokenSessionResultOptional = tokenSessionRepository.findTokenSessionByEmail(email);
      TokenSession tokenSessionResult = tokenSessionResultOptional.get();
      Assertions.assertEquals(token, tokenSessionResult.getToken());
      Assertions.assertEquals(email, tokenSessionResult.getEmail());
      Assertions.assertEquals(expiredAt, tokenSessionResult.getExpiredAt());
    } else {
      throw new TestAbortedException();
    }
  }
}
