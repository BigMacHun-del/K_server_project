package sparta.coffee_shop.domain.menu.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sparta.coffee_shop.common.response.BaseResponse;
import sparta.coffee_shop.domain.menu.dto.response.MenuResponse;
import sparta.coffee_shop.domain.menu.dto.response.PopularMenuResponse;
import sparta.coffee_shop.domain.menu.service.MenuService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coffee/menus")
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<MenuResponse>>> getMenus() {
        return ResponseEntity.ok(
                BaseResponse.success("200", "메뉴 목록 조회 성공", menuService.getMenus())
        );
    }

    @GetMapping("/populars")
    public ResponseEntity<BaseResponse<List<PopularMenuResponse>>> getPopularMenus() {
        return ResponseEntity.ok(
                BaseResponse.success("200", "인기 메뉴 조회 성공", menuService.getPopularMenus())
        );
    }
}