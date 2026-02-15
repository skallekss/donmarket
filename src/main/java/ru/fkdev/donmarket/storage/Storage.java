package ru.fkdev.donmarket.storage;

import ru.fkdev.donmarket.model.Category;
import ru.fkdev.donmarket.model.MarketOrder;
import ru.fkdev.donmarket.model.OrderStatus;
import ru.fkdev.donmarket.model.SortMode;

import java.util.List;
import java.util.UUID;

public interface Storage {

    void init();

    void shutdown();

    void createOrder(MarketOrder order);

    void updateOrderStatus(int orderId, OrderStatus status);

    List<MarketOrder> getActiveOrders(Category category, SortMode sortMode, int offset, int limit);

    int countActiveOrders(Category category);

    List<MarketOrder> getOrdersByPlayer(UUID owner);

    MarketOrder getOrderById(int id);

    double getTokenBalance(UUID player);

    void setTokenBalance(UUID player, double balance);

    void addTokenBalance(UUID player, double amount);

    double getCoinBalance(UUID player);

    void setCoinBalance(UUID player, double balance);

    void addCoinBalance(UUID player, double amount);
}
