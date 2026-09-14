package com.example.cafequeue.menu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class MenuDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MenuDataSeeder.class);
    private final MenuItemRepository menuItemRepository;

    public MenuDataSeeder(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        menuItemRepository.count().subscribe(count -> {
            if (count == 0) {
                log.info("Menu collection is empty. Seeding initial data...");

                MenuItem kopiSusu = new MenuItem(
                        "Kopi Susu Gula Aren",
                        new BigDecimal("22000"),
                        "BEVERAGE",
                        true,
                        Map.of(
                                "sugarLevel", List.of("Normal", "Less", "No Sugar"),
                                "iceLevel", List.of("Normal", "Less", "No Ice")
                        )
                );

                MenuItem americano = new MenuItem(
                        "Americano",
                        new BigDecimal("18000"),
                        "BEVERAGE",
                        true,
                        Map.of(
                                "sugarLevel", List.of("Normal", "No Sugar"),
                                "iceLevel", List.of("Normal", "Less", "No Ice", "Hot")
                        )
                );

                MenuItem matcha = new MenuItem(
                        "Matcha Latte",
                        new BigDecimal("25000"),
                        "BEVERAGE",
                        true,
                        Map.of(
                                "sugarLevel", List.of("Normal", "Less"),
                                "milkType", List.of("Fresh Milk", "Oat Milk")
                        )
                );

                MenuItem nasiGoreng = new MenuItem(
                        "Nasi Goreng Spesial",
                        new BigDecimal("35000"),
                        "FOOD",
                        true,
                        Map.of(
                                "spicinessLevel", List.of("Level 0", "Level 1", "Level 2", "Level 3"),
                                "eggStyle", List.of("Dadar", "Mata Sapi", "Scrambled")
                        )
                );

                MenuItem mieGoreng = new MenuItem(
                        "Mie Goreng Jawa",
                        new BigDecimal("30000"),
                        "FOOD",
                        true,
                        Map.of(
                                "spicinessLevel", List.of("Level 0", "Level 1", "Level 2", "Level 3")
                        )
                );

                menuItemRepository.saveAll(List.of(kopiSusu, americano, matcha, nasiGoreng, mieGoreng))
                        .subscribe(saved -> log.info("Seeded menu: {}", saved.getName()));
            } else {
                log.info("Menu collection already contains data. Seeding skipped.");
            }
        });
    }
}
