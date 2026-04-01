package sparta.coffee_shop.domain.point.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.coffee_shop.domain.point.entity.Point;
import sparta.coffee_shop.domain.user.entity.User;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, Long> {

    Optional<Point> findByUser(User user);
}