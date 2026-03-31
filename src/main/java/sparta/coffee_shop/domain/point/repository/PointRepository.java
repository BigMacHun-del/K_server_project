package sparta.coffee_shop.domain.point.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.coffee_shop.domain.point.entity.Point;

public interface PointRepository extends JpaRepository<Point,Long> {
}
