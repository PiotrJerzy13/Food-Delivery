package food.data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import food.domain.Cart;
import food.domain.Customer;

public class CustomerReader extends Reader {

    @Override
    public List<Customer> read(String path) {
        var lines = readFromFile(path);
        return lines
                .stream()
                .map(this::buildCustomer)
                .collect(Collectors.toList());
    }

    private Customer buildCustomer(String line) {
        var strings = splitLineByComma(line);
        var customerId = Long.parseLong(strings[2]);
        return new Customer.Builder()
            .userName(strings[0])
            .password(strings[1])
            .id(customerId)
            .name(strings[3])
            .balance(new BigDecimal(strings[4]))
            .cart(Cart.getEmptyCart())
            .build();
    }
}
