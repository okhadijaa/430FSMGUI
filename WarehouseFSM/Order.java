import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Client client;
    private final Map<Product, Integer> items;

    public Order(Client client) {
        if (client == null) {
            throw new IllegalArgumentException(
                    "Client cannot be null.");
        }

        this.client = client;
        this.items = new LinkedHashMap<>();
    }

    public Client getClient() {
        return client;
    }

    public void addProduct(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null.");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be positive.");
        }

        int currentQuantity = items.getOrDefault(product, 0);

        items.put(product,
                Math.addExact(currentQuantity, quantity));
    }

    public boolean containsProduct(Product product) {
        return items.containsKey(product);
    }

    public int getQuantity(Product product) {
        return items.getOrDefault(product, 0);
    }

    public boolean removeProduct(Product product) {
        return items.remove(product) != null;
    }

    public Map<Product, Integer> getItems() {
        return Collections.unmodifiableMap(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public double calculateTotal() {
        double total = 0.0;

        for (Map.Entry<Product, Integer> entry
                : items.entrySet()) {

            Product product = entry.getKey();
            int quantity = entry.getValue();

            total += product.getUnitPrice() * quantity;
        }

        return total;
    }

    public void displayOrder() {
        System.out.println(
                "Order for: " + client.getClientName());

        if (items.isEmpty()) {
            System.out.println("Order is empty.");
            return;
        }

        for (Map.Entry<Product, Integer> entry
                : items.entrySet()) {

            Product product = entry.getKey();
            int quantity = entry.getValue();

            double subtotal =
                    product.getUnitPrice() * quantity;

            System.out.println(
                    "Product: " + product.getProductName()
                    + ", Quantity: " + quantity
                    + ", Unit Price: $"
                    + String.format("%.2f",
                            product.getUnitPrice())
                    + ", Subtotal: $"
                    + String.format("%.2f", subtotal));
        }

        System.out.println(
                "Requested total: $"
                + String.format("%.2f", calculateTotal()));
    }
}
