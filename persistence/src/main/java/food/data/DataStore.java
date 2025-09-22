package food.data;

import java.util.List;

import food.domain.Customer;
import food.domain.Food;
import food.domain.Order;

public interface DataStore {
    void init();
    List<Customer> getCustomers();

    List<Food> getFoods();

    List<Order> getOrders();

    Order createOrder(Order order);

    void writeOrders();
}
