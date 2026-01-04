package io.github.mfthfzn.repository;

import io.github.mfthfzn.util.JpaUtilTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class RepositoryTest {

  protected static EntityManagerFactory entityManagerFactory;
  protected static EntityManager entityManager;
  protected static EntityTransaction transaction;

  @BeforeAll
  static void beforeAll() {
    entityManagerFactory = JpaUtilTest.getEntityManagerFactory();
    entityManager = entityManagerFactory.createEntityManager();
    transaction = entityManager.getTransaction();
  }

  @AfterAll
  static void afterAll() {
    entityManager.close();
    entityManagerFactory.close();
  }

  public void truncateAllTable() {
    try {
      transaction.begin();
      entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

      entityManager.createNativeQuery("TRUNCATE TABLE token_sessions").executeUpdate();
      entityManager.createNativeQuery("TRUNCATE TABLE users").executeUpdate();
      entityManager.createNativeQuery("TRUNCATE TABLE stores").executeUpdate();

      entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

      transaction.commit();
    } catch (Exception e) {
      entityManager.getTransaction().rollback();
      throw e;
    }
  }
}
