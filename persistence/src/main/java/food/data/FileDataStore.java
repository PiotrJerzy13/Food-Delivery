package food.data;

import food.domain.Customer;
import food.domain.Food;
import food.domain.Order;

import java.util.ArrayList;
import java.util.List;

public class FileDataStore implements DataStore {

    private final String folder;                  // e.g. "test"
    private final String CUSTOMERS = "customers.csv";
    private final String FOODS     = "foods.csv";
    private final String ORDERS    = "orders.csv";

    private final CustomerReader customerReader = new CustomerReader();
    private final FoodReader foodReader         = new FoodReader();
    private final OrderWriter orderWriter       = new OrderWriter();

    private List<Customer> customers = new ArrayList<>();
    private List<Food> foods         = new ArrayList<>();
    private List<Order> orders       = new ArrayList<>();

    public FileDataStore(String inputFolderPath) {
        this.folder = inputFolderPath;
    }

    @Override
    public void init() {
        customers = customerReader.read(path(CUSTOMERS));
        foods     = foodReader.read(path(FOODS));
        orders    = new ArrayList<>(); // start fresh for this run
    }

    @Override
    public List<Customer> getCustomers() {
        return customers;
    }

    @Override
    public List<Food> getFoods() {
        return foods;
    }

    @Override
    public List<Order> getOrders() {
        return orders;
    }

    @Override
    public Order createOrder(Order order) {
        if (order == null) throw new IllegalArgumentException("order is null");

        // Simple ID generation
        long nextId = orders.size();
        order.setOrderId(nextId);

        // Add to matching customer if found
        customers.stream()
                .filter(c -> c.getId() == order.getCustomerId())
                .findFirst()
                .ifPresent(c -> c.getOrders().add(order));

        // Always keep in memory
        orders.add(order);

        // Append to CSV
        orderWriter.appendOrder(order, path(ORDERS));

        return order;
    }

    @Override
    public void writeOrders() {
        orderWriter.writeOrders(orders, path(ORDERS));
    }

    private String path(String fileName) {
        return folder.endsWith("/") ? folder + fileName : folder + "/" + fileName;
    }
}
