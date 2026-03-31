package sparta.coffee_shop.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.coffee_shop.domain.point.entity.Point;
import sparta.coffee_shop.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User,Long> {

}
