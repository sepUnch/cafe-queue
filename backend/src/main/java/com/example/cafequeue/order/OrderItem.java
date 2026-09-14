package com.example.cafequeue.order;

import com.example.cafequeue.common.BaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Table("order_item")
public class OrderItem extends BaseEntity {

    @Id
    private UUID id;

    @Column("order_id")
    private UUID orderId;

    @Column("menu_item_name")
    private String menuItemName;

    @Column("quantity")
    private Integer quantity;

    @Column("price")
    private BigDecimal price;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public String getMenuItemName() { return menuItemName; }
    public void setMenuItemName(String menuItemName) { this.menuItemName = menuItemName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
