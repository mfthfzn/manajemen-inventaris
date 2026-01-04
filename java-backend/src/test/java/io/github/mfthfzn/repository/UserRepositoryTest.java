package io.github.mfthfzn.repository;

import io.github.mfthfzn.entity.Name;
import io.github.mfthfzn.entity.Store;
import io.github.mfthfzn.entity.User;
import io.github.mfthfzn.enums.UserType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
public class UserRepositoryTest extends RepositoryTest {

  private UserRepositoryImpl userRepository;

  @BeforeEach
  void setUp() {
    userRepository = new UserRepositoryImpl(entityManagerFactory);
    truncateAllTable();
  }

  @AfterEach
  void tearDown() {
    truncateAllTable();
  }

  @Test
  void testInsertIntoUserAndFindByEmail() {

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

    transaction.begin();
    Optional<User> userByEmailOptional = userRepository.findUserByEmail(email);
    transaction.commit();
    if (userByEmailOptional.isPresent()) {
      User userByEmail = userByEmailOptional.get();
      Assertions.assertNotNull(userByEmail);
      Assertions.assertEquals(email, userByEmail.getEmail());
      Assertions.assertEquals(password, userByEmail.getPassword());
      log.info(userByEmail.toString());
    } else {
      throw new TestAbortedException();
    }
  }
}
