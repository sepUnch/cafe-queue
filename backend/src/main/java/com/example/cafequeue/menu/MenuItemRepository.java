package com.example.cafequeue.menu;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends ReactiveMongoRepository<MenuItem, String> {
}
