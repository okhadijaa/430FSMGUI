import java.io.Serializable;
import java.util.Objects;

public class Client implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String clientId;
    private String clientName;
    private String address;
    private double balance;
    private final Wishlist wishlist;

    public Client(String clientId, String clientName, String address) {

        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Client ID cannot be empty.");
        }

        if (clientName == null || clientName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Client name cannot be empty.");
        }

        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Client address cannot be empty.");
        }

        this.clientId = clientId;
        this.clientName = clientName;
        this.address = address;
        this.balance = 0.0;
        this.wishlist = new Wishlist();
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getAddress() {
        return address;
    }

    public double getBalance() {
        return balance;
    }

    public Wishlist getWishlist() {
        return wishlist;
    }

    public void addDebit(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Debit amount cannot be negative.");
        }

        balance += amount;
    }

    public void addCredit(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Credit amount cannot be negative.");
        }

        balance -= amount;
    }

    public boolean hasOutstandingBalance() {
        return balance > 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Client)) {
            return false;
        }

        Client other = (Client) obj;
        return Objects.equals(clientId, other.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId);
    }

    @Override
    public String toString() {
        return "Client ID: " + clientId
                + ", Client Name: " + clientName
                + ", Address: " + address
                + ", Balance: $"
                + String.format("%.2f", balance);
    }
}
