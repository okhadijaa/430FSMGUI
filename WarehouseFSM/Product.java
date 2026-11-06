import java.io.Serializable;
import java.util.Objects;

public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String productId;
    private String productName;
    private int quantity;
    private double unitPrice;

    public Product(String productId, String productName,
                   int quantity, double unitPrice) {

        if (productId == null || productId.trim().isEmpty()
                || productName == null
                || productName.trim().isEmpty()
                || quantity < 0
                || !Double.isFinite(unitPrice)
                || unitPrice < 0) {

            throw new IllegalArgumentException(
                "Invalid product details.");
        }

        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public boolean isInStock() {
        return quantity > 0;
    }

    public void addStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                "Stock increase must be positive.");
        }

        quantity = Math.addExact(quantity, amount);
    }

    public void reduceStock(int amount) {
        if (amount <= 0 || amount > quantity) {
            throw new IllegalArgumentException(
                "Invalid stock reduction.");
        }

        quantity -= amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Product)) {
            return false;
        }

        Product other = (Product) obj;
        return Objects.equals(productId, other.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "Product ID: " + productId
            + ", Product Name: " + productName
            + ", Quantity: " + quantity
            + ", Unit Price: $"
            + String.format("%.2f", unitPrice);
    }
}
