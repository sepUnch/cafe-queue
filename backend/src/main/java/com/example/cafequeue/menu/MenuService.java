package com.example.cafequeue.menu;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    public MenuService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    public Flux<MenuItem> getAllMenus() {
        return menuItemRepository.findAll()
                .filter(item -> !item.isMarkForDelete() && item.isAvailable());
    }

    public Mono<MenuItem> addMenu(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }
}
