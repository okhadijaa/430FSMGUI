import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClientMenuState extends WarehouseState {
    private static ClientMenuState state;

    private ClientMenuState() { }

    public static ClientMenuState instance() {
        if (state == null) state = new ClientMenuState();
        return state;
    }

    @Override
    public void run() {
        Client client = warehouse().searchClient(context().getClient());
        if (client == null) {
            System.out.println("Current client not found. Logging out.");
            changeState(WarehouseContext.LOGOUT);
            return;
        }

        System.out.println("\n========== CLIENT MENU (" + client.getClientId() + ") ==========");
        System.out.println("1. Show Client Details / Balance");
        System.out.println("2. Show Products and Prices");
        System.out.println("3. Show My Transactions");
        System.out.println("4. Add or Update Wishlist Item");
        System.out.println("5. Display My Wishlist");
        System.out.println("6. Place Order");
        System.out.println("7. Logout");

        switch (context().getCommand(7)) {
            case 1: showClientDetails(); break;
            case 2: showProductsWithPrices(); break;
            case 3: warehouse().displayTransactions(context().getClient()); break;
            case 4: addItemToWishlist(); break;
            case 5: warehouse().displayWishlist(context().getClient()); break;
            case 6: placeOrder(); break;
            case 7: changeState(WarehouseContext.LOGOUT); break;
            default: break;
        }
    }

    private void showClientDetails() {
        System.out.println(warehouse().searchClient(context().getClient()));
    }

    private void showProductsWithPrices() {
        Iterator<Product> products = warehouse().getProducts();
        if (!products.hasNext()) {
            System.out.println("No products available.");
        }
        while (products.hasNext()) {
            Product product = products.next();
            System.out.printf("%s | %s | $%.2f%n", product.getProductId(),
                    product.getProductName(), product.getUnitPrice());
        }
    }

    private void addItemToWishlist() {
        String productId = context().getId("Enter Product ID:");
        int quantity = context().getPositiveNumber("Enter desired quantity:");
        int result = warehouse().addToWishlist(context().getClient(), productId, quantity);
        switch (result) {
            case Warehouse.OPERATION_COMPLETED:
                System.out.println("Wishlist updated."); break;
            case Warehouse.PRODUCT_NOT_FOUND:
                System.out.println("Product not found."); break;
            case Warehouse.CLIENT_NOT_FOUND:
                System.out.println("Client not found."); break;
            default:
                System.out.println("Unable to update wishlist."); break;
        }
    }

    private void placeOrder() {
        String clientId = context().getClient();
        Client client = warehouse().searchClient(clientId);
        if (client.getWishlist().isEmpty()) {
            System.out.println("Wishlist is empty. Nothing to order.");
            return;
        }
        warehouse().displayWishlist(clientId);
        if (!context().yesOrNo("Order ALL items in this wishlist?")) {
            System.out.println("Order canceled.");
            return;
        }

        Map<String, Integer> requested =
                new LinkedHashMap<>(client.getWishlist().getItems());
        Invoice invoice = warehouse().processOrder(clientId);
        if (invoice == null) {
            System.out.println("Order failed.");
            return;
        }
        if (invoice.isEmpty()) {
            System.out.println("No items shipped now; all requested items were waitlisted.");
        } else {
            invoice.displayInvoice();
        }
        for (Map.Entry<String, Integer> item : requested.entrySet()) {
            Product product = warehouse().searchProduct(item.getKey());
            int shipped = invoice.getQuantity(product);
            int missing = item.getValue() - shipped;
            if (missing > 0) {
                System.out.println("Waitlisted " + missing + " unit(s) of " + item.getKey());
            }
        }
        System.out.printf("Account balance: $%.2f%n", client.getBalance());
    }
}
