import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Invoice implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Client client;
    private final LocalDate invoiceDate;
    private final Map<Product, Integer> items;
    private final Map<Product, Double> unitPrices;

    public Invoice(Client client) {
        if (client == null) {
            throw new IllegalArgumentException(
                    "Client cannot be null.");
        }

        this.client = client;
        this.invoiceDate = LocalDate.now();
        this.items = new LinkedHashMap<>();
        this.unitPrices = new LinkedHashMap<>();
    }

    public Client getClient() {
        return client;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
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

        // Preserve the price recorded when this product
        // was first added to the invoice.
        unitPrices.putIfAbsent(
                product, product.getUnitPrice());
    }

    public Map<Product, Integer> getItems() {
        return Collections.unmodifiableMap(items);
    }

    public int getQuantity(Product product) {
        return items.getOrDefault(product, 0);
    }

    public double getUnitPrice(Product product) {
        return unitPrices.getOrDefault(product, 0.0);
    }

    public double calculateTotal() {
        double total = 0.0;

        for (Map.Entry<Product, Integer> entry
                : items.entrySet()) {

            Product product = entry.getKey();
            int quantity = entry.getValue();
            double price = unitPrices.get(product);

            total += price * quantity;
        }

        return total;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void displayInvoice() {
        System.out.println("========== INVOICE ==========");
        System.out.println(
                "Client: " + client.getClientName());
        System.out.println(
                "Client ID: " + client.getClientId());
        System.out.println(
                "Invoice Date: " + invoiceDate);
        System.out.println("-----------------------------");

        if (items.isEmpty()) {
            System.out.println("No products to invoice.");
            System.out.println("=============================");
            return;
        }

        for (Map.Entry<Product, Integer> entry
                : items.entrySet()) {

            Product product = entry.getKey();
            int quantity = entry.getValue();
            double price = unitPrices.get(product);
            double subtotal = price * quantity;

            System.out.println(
                    "Product: " + product.getProductName());
            System.out.println(
                    "Quantity: " + quantity);
            System.out.println(
                    "Unit Price: $" + String.format("%.2f", price));
            System.out.println(
                    "Subtotal: $" + String.format("%.2f", subtotal));
            System.out.println("-----------------------------");
        }

        System.out.println(
                "TOTAL: $" +
                String.format("%.2f", calculateTotal()));

        System.out.println("=============================");
    }
}
