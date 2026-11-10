import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ClientMenuState extends WarehouseState {
    private static ClientMenuState state;
    private final JPanel panel;
    private final JLabel clientLabel;
    private final JTextArea displayArea;

    private ClientMenuState() {
        panel = new JPanel(new BorderLayout());

        clientLabel = new JLabel("Active Client: ");
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(clientLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        JButton showDetailsBtn = new JButton("Show Client Details");
        JButton showProductsBtn = new JButton("Show Products (Price)");
        JButton showTransactionsBtn = new JButton("Show Transactions");
        JButton addToWishlistBtn = new JButton("Add to Wishlist");
        JButton displayWishlistBtn = new JButton("Display Wishlist");
        JButton placeOrderBtn = new JButton("Place Order");
        JButton logoutBtn = new JButton("Logout");

        displayArea = new JTextArea(15, 40);
        displayArea.setEditable(false);

        // Dedicated action listener classes
        showDetailsBtn.addActionListener(new ShowDetailsAction());
        showProductsBtn.addActionListener(new ShowProductsAction());
        showTransactionsBtn.addActionListener(new ShowTransactionsAction());
        addToWishlistBtn.addActionListener(new AddToWishlistAction());
        displayWishlistBtn.addActionListener(new DisplayWishlistAction());
        placeOrderBtn.addActionListener(new PlaceOrderAction());
        logoutBtn.addActionListener(new LogoutAction());

        buttonPanel.add(showDetailsBtn);
        buttonPanel.add(showProductsBtn);
        buttonPanel.add(showTransactionsBtn);
        buttonPanel.add(addToWishlistBtn);
        buttonPanel.add(displayWishlistBtn);
        buttonPanel.add(placeOrderBtn);
        buttonPanel.add(logoutBtn);

        topPanel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(displayArea), BorderLayout.CENTER);
    }

    public static ClientMenuState instance() {
        if (state == null) {
            state = new ClientMenuState();
        }
        return state;
    }

    @Override
    public JPanel getView() {
        String clientId = WarehouseContext.instance().getClient();
        clientLabel.setText("Active Client ID: " + (clientId != null ? clientId : "None"));
        displayArea.setText("");
        return panel;
    }

    // --- Dedicated Listener Classes ---

    private class ShowDetailsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cId = WarehouseContext.instance().getClient();
            Client c = warehouse.searchClient(cId);
            if (c != null) {
                displayArea.setText("=== Client Details ===\n" + c);
            } else {
                displayArea.setText("Client not found.");
            }
        }
    }

    private class ShowProductsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            displayArea.setText("=== Product Catalog (Prices) ===\n");
            Iterator<Product> it = warehouse.getProducts();
            while (it.hasNext()) {
                Product p = it.next();
                displayArea.append(p.getProductName() + " (ID: " + p.getProductId() 
                    + ") - Unit Price: $" + String.format("%.2f", p.getUnitPrice()) + "\n");
            }
        }
    }

    private class ShowTransactionsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cId = WarehouseContext.instance().getClient();
            displayArea.setText("=== Transactions for Client " + cId + " ===\n");
            warehouse.displayTransactions(cId);
            // Outputs to console via warehouse, or view balance details directly:
            Client c = warehouse.searchClient(cId);
            if (c != null) {
                displayArea.append("Current Outstanding Balance: $" 
                    + String.format("%.2f", c.getBalance()) + "\n");
                displayArea.append("(See console terminal for full itemized transaction logs)\n");
            }
        }
    }

    private class AddToWishlistAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cId = WarehouseContext.instance().getClient();
            String pId = JOptionPane.showInputDialog(panel, "Enter Product ID:");
            if (pId == null || pId.trim().isEmpty()) return;
            String qtyStr = JOptionPane.showInputDialog(panel, "Enter Desired Quantity:");
            if (qtyStr == null || qtyStr.trim().isEmpty()) return;

            try {
                int qty = Integer.parseInt(qtyStr.trim());
                int res = warehouse.addToWishlist(cId, pId.trim(), qty);
                if (res == Warehouse.OPERATION_COMPLETED) {
                    displayArea.setText("Product " + pId.trim() + " added/updated in Wishlist with quantity " + qty);
                } else if (res == Warehouse.PRODUCT_NOT_FOUND) {
                    displayArea.setText("Product " + pId.trim() + " not found in catalog.");
                } else {
                    displayArea.setText("Failed to update wishlist. Quantity must be positive.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid quantity number.");
            }
        }
    }

    private class DisplayWishlistAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cId = WarehouseContext.instance().getClient();
            Client c = warehouse.searchClient(cId);
            if (c == null) return;

            Wishlist wl = c.getWishlist();
            if (wl.isEmpty()) {
                displayArea.setText("Wishlist is currently empty.");
                return;
            }

            displayArea.setText("=== Wishlist for " + c.getClientName() + " (" + cId + ") ===\n");
            for (Map.Entry<String, Integer> entry : wl.getItems().entrySet()) {
                displayArea.append("Product ID: " + entry.getKey() + " | Quantity Desired: " + entry.getValue() + "\n");
            }
        }
    }

    private class PlaceOrderAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cId = WarehouseContext.instance().getClient();
            Client c = warehouse.searchClient(cId);
            if (c == null) return;

            Wishlist wl = c.getWishlist();
            if (wl.isEmpty()) {
                displayArea.setText("Wishlist is empty. Cannot place an order.");
                return;
            }

            // Order all items currently on the wishlist
            Map<String, Integer> toOrder = new LinkedHashMap<>(wl.getItems());
            Invoice invoice = warehouse.processOrder(cId, toOrder);

            if (invoice != null) {
                displayArea.setText("=== Order Processed Successfully ===\n");
                displayArea.append("Invoice Date: " + invoice.getInvoiceDate() + "\n");
                displayArea.append("Billed Shipped Total: $" + String.format("%.2f", invoice.calculateTotal()) + "\n");
                displayArea.append("--- Shipped Products ---\n");
                if (invoice.getItems().isEmpty()) {
                    displayArea.append("None (all requested items moved to product waitlist).\n");
                } else {
                    for (Map.Entry<Product, Integer> item : invoice.getItems().entrySet()) {
                        displayArea.append(item.getKey().getProductName() + " (ID: " + item.getKey().getProductId() 
                            + ") x " + item.getValue() + " units\n");
                    }
                }
                displayArea.append("Updated Account Balance: $" + String.format("%.2f", c.getBalance()) + "\n");
            } else {
                displayArea.setText("Order processing failed.");
            }
        }
    }

    private static class LogoutAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            WarehouseContext.instance().changeState(WarehouseContext.LOGOUT);
        }
    }
}
