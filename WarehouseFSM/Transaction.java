import java.io.Serializable;
import java.time.LocalDate;

public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate date;
    private final String description;
    private final double amount;

    public Transaction(String description, double amount) {

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Transaction description cannot be empty.");
        }

        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Transaction amount cannot be negative.");
        }

        this.date = LocalDate.now();
        this.description = description;
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Date: " + date
                + ", Description: " + description
                + ", Amount: $" + String.format("%.2f", amount);
    }
}
