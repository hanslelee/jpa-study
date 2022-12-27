package querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import querydsl.entity.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    List<User> findByUsername(String username);
}
