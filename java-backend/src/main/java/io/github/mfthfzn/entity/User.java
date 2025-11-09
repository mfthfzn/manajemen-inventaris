package io.github.mfthfzn.entity;

import io.github.mfthfzn.enums.UserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Table(
        name = "users"
)
@Getter
@Setter
@NoArgsConstructor
public class User {

  @Id
  private String email;

  private String password;

  @Embedded
  private Name name;

  @Enumerated(EnumType.STRING)
  private UserType role;

  @Override
  public final boolean equals(Object object) {
    if (this == object) return true;
    if (object == null) return false;
    Class<?> oEffectiveClass = object instanceof HibernateProxy ? ((HibernateProxy) object).getHibernateLazyInitializer().getPersistentClass() : object.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    User user = (User) object;
    return getEmail() != null && Objects.equals(getEmail(), user.getEmail());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
  }
}
