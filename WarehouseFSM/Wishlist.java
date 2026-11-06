import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Wishlist implements Serializable {

    private static final long serialVersionUID = 1L;

    //product ID and quantity requested
    private final Map<String, Integer> items;

    public Wishlist() {
        items = new LinkedHashMap<>();
    }

    //add a product or update its existing quantity
    public void addOrUpdateProduct(String productId, int quantity) {
        validateProductId(productId);

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Quantity must be positive.");
        }

        items.put(productId, quantity);
    }

    public boolean removeProduct(String productId) {
        validateProductId(productId);
        return items.remove(productId) != null;
    }

    public boolean containsProduct(String productId) {
        validateProductId(productId);
        return items.containsKey(productId);
    }

    public int getQuantity(String productId) {
        validateProductId(productId);
        return items.getOrDefault(productId, 0);
    }

    public Map<String, Integer> getItems() {
        return Collections.unmodifiableMap(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void displayWishlist() {
        if (items.isEmpty()) {
            System.out.println("Wishlist is empty.");
            return;
        }

        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            System.out.println(
                "Product ID: " + entry.getKey()
                + ", Quantity: " + entry.getValue());
        }
    }

    private void validateProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Product ID cannot be empty.");
        }
    }
}
