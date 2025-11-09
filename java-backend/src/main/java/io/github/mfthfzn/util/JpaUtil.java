package io.github.mfthfzn.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.Getter;


public class JpaUtil {

  private static EntityManagerFactory entityManagerFactory;

  public static EntityManagerFactory getEntityManagerFactory() {
    if (entityManagerFactory == null) {
      entityManagerFactory = Persistence.createEntityManagerFactory("MANAJEMEN-INVENTARIS");
    }
    return entityManagerFactory;
  }

}
