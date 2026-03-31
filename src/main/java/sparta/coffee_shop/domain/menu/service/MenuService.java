package sparta.coffee_shop.domain.menu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.coffee_shop.domain.menu.dto.response.MenuResponse;
import sparta.coffee_shop.domain.menu.repository.MenuRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenus() {
        return menuRepository.findAllByIsActiveTrue()
                .stream()
                .map(MenuResponse::from)
                .toList();
    }
}
