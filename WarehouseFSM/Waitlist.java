import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Waitlist implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LinkedHashMap<String, Integer> waitingClients;

    public Waitlist() {
        waitingClients = new LinkedHashMap<>();
    }

    public void addClient(String clientId, int quantity) {

        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Client ID cannot be empty.");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be positive.");
        }

        int previousQuantity =
                waitingClients.getOrDefault(clientId, 0);

        waitingClients.put(
                clientId,
                Math.addExact(previousQuantity, quantity));
    }

    public String getNextClient() {

        if (waitingClients.isEmpty()) {
            return null;
        }

        return waitingClients.keySet().iterator().next();
    }

    public int getQuantity(String clientId) {
        return waitingClients.getOrDefault(clientId, 0);
    }

    public boolean containsClient(String clientId) {
        return waitingClients.containsKey(clientId);
    }

    public void fulfill(String clientId, int quantity) {

        int waiting = getQuantity(clientId);

        if (quantity <= 0 || quantity > waiting) {
            throw new IllegalArgumentException(
                    "Invalid fulfillment quantity.");
        }

        int remaining = waiting - quantity;

        if (remaining == 0) {
            waitingClients.remove(clientId);
        } else {
            waitingClients.put(clientId, remaining);
        }
    }

    public int getTotalQuantity() {

        int total = 0;

        for (int quantity : waitingClients.values()) {
            total = Math.addExact(total, quantity);
        }

        return total;
    }

    public int getClientCount() {
        return waitingClients.size();
    }

    public boolean isEmpty() {
        return waitingClients.isEmpty();
    }

    public Map<String, Integer> getWaitingClients() {
        return Collections.unmodifiableMap(waitingClients);
    }

    public void displayWaitlist() {

        if (waitingClients.isEmpty()) {
            System.out.println("Waitlist is empty.");
            return;
        }

        for (Map.Entry<String, Integer> entry
                : waitingClients.entrySet()) {

            System.out.println(
                    "Client ID: " + entry.getKey()
                    + ", Quantity Waiting: " + entry.getValue());
        }

        System.out.println(
                "Total Outstanding Quantity: "
                + getTotalQuantity());
    }
}
