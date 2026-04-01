package sparta.coffee_shop.domain.menu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sparta.coffee_shop.domain.menu.entity.Menu;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findAllByIsActiveTrue();
}