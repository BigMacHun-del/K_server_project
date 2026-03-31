package sparta.coffee_shop.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.coffee_shop.domain.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserKey(String userKey);
}