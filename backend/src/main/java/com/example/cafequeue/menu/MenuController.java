package com.example.cafequeue.menu;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public Flux<MenuItem> getAllMenus() {
        return menuService.getAllMenus();
    }

    @PostMapping
    public Mono<MenuItem> addMenu(@RequestBody MenuItem menuItem) {
        return menuService.addMenu(menuItem);
    }
}
