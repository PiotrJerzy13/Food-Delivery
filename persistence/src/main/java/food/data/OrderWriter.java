package food.data;

import food.domain.Order;
import food.domain.OrderItem;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class OrderWriter {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void writeOrders(List<Order> orders, String outputFile) {
        try (var fileWriter = new FileWriter(outputFile)) {
            fileWriter.write(addOrders(orders));
        } catch (IOException e) {
            throw new RuntimeException("IOException happened while writing order file: " + outputFile, e);
        }
    }

    private String addOrders(List<Order> orders) {
        return orders
                .stream()
                .map(order -> addOrderItems(
                        order.getOrderItems(),
                        order.getPrice(),
                        order.getTimestampCreated(),
                        order.getOrderId(),
                        order.getCustomerId()
                ))
                .collect(Collectors.joining("\n"));
    }

    private String addOrderItems(List<OrderItem> orderItems, BigDecimal totalPrice, LocalDateTime timestampCreated, Long orderId, long customerId) {
        return orderItems
                .stream()
                .map(orderItem -> addOrderItem(
                        orderItem,
                        totalPrice,
                        timestampCreated,
                        orderId,
                        customerId
                ))
                .collect(Collectors.joining("\n"));
    }

    private String addOrderItem(OrderItem orderItem, BigDecimal totalPrice, LocalDateTime timestampCreated, Long orderId, long customerId) {
        return String.join(
                ",",
                Long.toString(orderId),
                Long.toString(customerId),
                orderItem.getFood().getName(),
                Integer.toString(orderItem.getPieces()),
                orderItem.getPrice().toPlainString(),
                timestampCreated.format(DATE_TIME_FORMATTER),
                totalPrice.toPlainString());
    }

    public void appendOrder(Order order, String outputFile) {
    try (var writer = new java.io.FileWriter(outputFile, true)) {

        java.io.File file = new java.io.File(outputFile);
        if (file.length() > 0) {
            writer.write(System.lineSeparator());
        }

        StringBuilder sb = new StringBuilder();
        for (var item : order.getOrderItems()) {
            sb.append(String.join(",",
                    Long.toString(order.getOrderId()),
                    Long.toString(order.getCustomerId()),
                    item.getFood().getName(),
                    Integer.toString(item.getPieces()),
                    item.getPrice().toPlainString(),
                    order.getTimestampCreated().format(DATE_TIME_FORMATTER),
                    order.getPrice().toPlainString()
            ));
            sb.append(System.lineSeparator());
        }

        writer.write(sb.toString().trim());
    } catch (Exception e) {
        throw new RuntimeException("Failed to append order to file: " + outputFile, e);
    }
}

}
