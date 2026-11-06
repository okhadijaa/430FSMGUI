import java.io.*;
import java.util.*;

public class Warehouse implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int OPERATION_COMPLETED = 1;
    public static final int OPERATION_FAILED = 2;
    public static final int CLIENT_NOT_FOUND = 3;
    public static final int PRODUCT_NOT_FOUND = 4;

    private static Warehouse warehouse;

    private final Map<String, Client> clients;
    private final Map<String, Product> products;
    private final Map<String, Waitlist> waitlists;
    private final Map<String, List<Invoice>> invoices;
    private final Map<String, List<Transaction>> transactions;

    private int nextClientId;
    private int nextProductId;

    private Warehouse() {
        clients = new LinkedHashMap<>();
        products = new LinkedHashMap<>();
        waitlists = new LinkedHashMap<>();
        invoices = new LinkedHashMap<>();
        transactions = new LinkedHashMap<>();

        nextClientId = 1;
        nextProductId = 1;
    }

    public static Warehouse instance() {
        if (warehouse == null) {
            warehouse = new Warehouse();
        }
        return warehouse;
    }

    public Client addClient(String name, String address) {
        if (name == null || name.trim().isEmpty()
                || address == null || address.trim().isEmpty()) {
            return null;
        }

        String id = "C" + nextClientId;
        Client client = new Client(id, name, address);

        clients.put(id, client);
        nextClientId++;

        return client;
    }

    public Product addProduct(String name, int quantity,
                              double price) {
        if (name == null || name.trim().isEmpty()
                || quantity < 0
                || !Double.isFinite(price)
                || price < 0) {
            return null;
        }

        String id = "P" + nextProductId;
        Product product = new Product(id, name, quantity, price);

        products.put(id, product);
        nextProductId++;

        return product;
    }

    public Client searchClient(String clientId) {
        return clients.get(clientId);
    }

    public Product searchProduct(String productId) {
        return products.get(productId);
    }

    public Iterator<Client> getClients() {
        return clients.values().iterator();
    }

    public Iterator<Product> getProducts() {
        return products.values().iterator();
    }

    public void displayClients() {
        for (Client client : clients.values()) {
            System.out.println(client);
        }
    }

    public void displayProducts() {
        for (Product product : products.values()) {
            System.out.println(
                product + ", Waitlisted: "
                + getOutstandingQuantity(product.getProductId()));
        }
    }

    public int addToWishlist(String clientId,
                             String productId,
                             int quantity) {
        Client client = searchClient(clientId);

        if (client == null) {
            return CLIENT_NOT_FOUND;
        }

        if (searchProduct(productId) == null) {
            return PRODUCT_NOT_FOUND;
        }

        if (quantity <= 0) {
            return OPERATION_FAILED;
        }

        client.getWishlist().addOrUpdateProduct(
            productId, quantity);

        return OPERATION_COMPLETED;
    }

    public void displayWishlist(String clientId) {
        Client client = searchClient(clientId);

        if (client == null) {
            System.out.println("Client not found.");
            return;
        }

        System.out.println(
            "Wishlist for " + client.getClientName()
            + " (" + clientId + "):");

        client.getWishlist().displayWishlist();
    }

    public Invoice processOrder(String clientId) {
        Client client = searchClient(clientId);

        if (client == null) {
            return null;
        }

        return processOrder(
            clientId,
            new LinkedHashMap<>(
                client.getWishlist().getItems()));
    }

  
    public Invoice processOrder(String clientId,
                                Map<String, Integer> selected) {
        Client client = searchClient(clientId);

        if (client == null || selected == null) {
            return null;
        }

        for (Map.Entry<String, Integer> entry
                : selected.entrySet()) {
            if (searchProduct(entry.getKey()) == null
                    || entry.getValue() == null
                    || entry.getValue() < 0) {
                return null;
            }
        }

        Order order = new Order(client);

        for (Map.Entry<String, Integer> entry
                : selected.entrySet()) {
            if (entry.getValue() > 0) {
                order.addProduct(
                    searchProduct(entry.getKey()),
                    entry.getValue());
            }
        }

        Invoice invoice = new Invoice(client);

        for (Map.Entry<Product, Integer> entry
                : order.getItems().entrySet()) {

            Product product = entry.getKey();
            int requested = entry.getValue();
            int shipped = Math.min(
                requested, product.getQuantity());
            int missing = requested - shipped;

            String productId = product.getProductId();

            client.getWishlist().removeProduct(productId);

            if (shipped > 0) {
                product.reduceStock(shipped);
                invoice.addProduct(product, shipped);
            }

            if (missing > 0) {
                Waitlist waitlist = waitlists.computeIfAbsent(
                    productId, key -> new Waitlist());

                waitlist.addClient(clientId, missing);
            }
        }

        if (!invoice.isEmpty()) {
            recordInvoice(invoice);
        }

        return invoice;
    }

    private void recordInvoice(Invoice invoice) {
        Client client = invoice.getClient();
        String clientId = client.getClientId();
        double total = invoice.calculateTotal();

        invoices.computeIfAbsent(
            clientId, key -> new ArrayList<>()
        ).add(invoice);

        client.addDebit(total);

        transactions.computeIfAbsent(
            clientId, key -> new ArrayList<>()
        ).add(new Transaction("Debit - invoice", total));
    }

    public int receivePayment(String clientId, double amount) {
        Client client = searchClient(clientId);

        if (client == null) {
            return CLIENT_NOT_FOUND;
        }

        if (!Double.isFinite(amount) || amount <= 0) {
            return OPERATION_FAILED;
        }

        client.addCredit(amount);

        transactions.computeIfAbsent(
            clientId, key -> new ArrayList<>()
        ).add(new Transaction("Credit - payment", amount));

        return OPERATION_COMPLETED;
    }

    public int receiveShipment(String productId, int quantity) {
        Product product = searchProduct(productId);

        if (product == null) {
            return PRODUCT_NOT_FOUND;
        }

        if (quantity <= 0) {
            return OPERATION_FAILED;
        }

        Waitlist waitlist = waitlists.get(productId);
        int remaining = quantity;

        while (waitlist != null
                && !waitlist.isEmpty()
                && remaining > 0) {

            String clientId = waitlist.getNextClient();
            Client client = searchClient(clientId);

            if (client == null) {
                throw new IllegalStateException(
                    "Waitlisted client not found.");
            }

            int requested = waitlist.getQuantity(clientId);
            int shipped = Math.min(requested, remaining);

            Invoice invoice = new Invoice(client);
            invoice.addProduct(product, shipped);
            recordInvoice(invoice);

            waitlist.fulfill(clientId, shipped);
            remaining -= shipped;
        }

        if (waitlist != null && waitlist.isEmpty()) {
            waitlists.remove(productId);
        }

        if (remaining > 0) {
            product.addStock(remaining);
        }

        return OPERATION_COMPLETED;
    }

    public int getOutstandingQuantity(String productId) {
        Waitlist waitlist = waitlists.get(productId);

        if (waitlist == null) {
            return 0;
        }

        return waitlist.getTotalQuantity();
    }

    public void displayWaitlist(String productId) {
        if (searchProduct(productId) == null) {
            System.out.println("Product not found.");
            return;
        }

        System.out.println("Waitlist for " + productId + ":");

        Waitlist waitlist = waitlists.get(productId);

        if (waitlist == null || waitlist.isEmpty()) {
            System.out.println("No outstanding orders.");
            return;
        }

        waitlist.displayWaitlist();
    }

    public void displayInvoices(String clientId) {
        if (searchClient(clientId) == null) {
            System.out.println("Client not found.");
            return;
        }

        List<Invoice> list = invoices.get(clientId);

        if (list == null || list.isEmpty()) {
            System.out.println("No invoices.");
            return;
        }

        for (Invoice invoice : list) {
            invoice.displayInvoice();
        }
    }

    public void displayTransactions(String clientId) {
        if (searchClient(clientId) == null) {
            System.out.println("Client not found.");
            return;
        }

        List<Transaction> list = transactions.get(clientId);

        if (list == null || list.isEmpty()) {
            System.out.println("No transactions.");
            return;
        }

        for (Transaction transaction : list) {
            System.out.println(transaction);
        }
    }

    public void displayOutstandingBalances() {
        for (Client client : clients.values()) {
            if (client.hasOutstandingBalance()) {
                System.out.println(client);
            }
        }
    }

    public static boolean save() {
        try (ObjectOutputStream output =
                new ObjectOutputStream(
                    new FileOutputStream("WarehouseData"))) {

            output.writeObject(instance());
            return true;

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public static Warehouse retrieve() {
        try (ObjectInputStream input =
                new ObjectInputStream(
                    new FileInputStream("WarehouseData"))) {

            warehouse = (Warehouse) input.readObject();
            return warehouse;

        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        return "Warehouse: " + clients.size()
            + " clients, " + products.size() + " products";
    }
}
