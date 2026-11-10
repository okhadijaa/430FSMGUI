import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ManagerMenuState extends WarehouseState {
    private static ManagerMenuState state;
    private final JPanel panel;
    private final JTextArea displayArea;

    private ManagerMenuState() {
        panel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        JButton addProductBtn = new JButton("Add Product");
        JButton showWaitlistBtn = new JButton("Show Waitlist");
        JButton receiveShipmentBtn = new JButton("Receive Shipment");
        JButton becomeClerkBtn = new JButton("Become Clerk");
        JButton logoutBtn = new JButton("Logout");

        displayArea = new JTextArea(15, 40);
        displayArea.setEditable(false);

        // Dedicated action listener classes
        addProductBtn.addActionListener(new AddProductAction());
        showWaitlistBtn.addActionListener(new ShowWaitlistAction());
        receiveShipmentBtn.addActionListener(new ReceiveShipmentAction());
        becomeClerkBtn.addActionListener(new BecomeClerkAction());
        logoutBtn.addActionListener(new LogoutAction());

        buttonPanel.add(addProductBtn);
        buttonPanel.add(showWaitlistBtn);
        buttonPanel.add(receiveShipmentBtn);
        buttonPanel.add(becomeClerkBtn);
        buttonPanel.add(logoutBtn);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(displayArea), BorderLayout.CENTER);
    }

    public static ManagerMenuState instance() {
        if (state == null) {
            state = new ManagerMenuState();
        }
        return state;
    }

    @Override
    public JPanel getView() {
        displayArea.setText("");
        return panel;
    }

    // --- Dedicated Listener Classes ---

    private class AddProductAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog(panel, "Enter Product Name:");
            if (name == null || name.trim().isEmpty()) return;
            String qtyStr = JOptionPane.showInputDialog(panel, "Enter Initial Stock Quantity:");
            if (qtyStr == null || qtyStr.trim().isEmpty()) return;
            String priceStr = JOptionPane.showInputDialog(panel, "Enter Unit Price:");
            if (priceStr == null || priceStr.trim().isEmpty()) return;

            try {
                int qty = Integer.parseInt(qtyStr.trim());
                double price = Double.parseDouble(priceStr.trim());
                Product p = warehouse.addProduct(name.trim(), qty, price);
                if (p != null) {
                    displayArea.setText("Product added successfully:\n" + p);
                } else {
                    displayArea.setText("Failed to add product. Check input values.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid quantity or price format.");
            }
        }
    }

    private class ShowWaitlistAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String pId = JOptionPane.showInputDialog(panel, "Enter Product ID:");
            if (pId == null || pId.trim().isEmpty()) return;

            Product p = warehouse.searchProduct(pId.trim());
            if (p == null) {
                displayArea.setText("Product not found: " + pId);
                return;
            }

            displayArea.setText("=== Waitlist for Product " + pId.trim() + " (" + p.getProductName() + ") ===\n");
            displayArea.append("Total Outstanding Waitlist Quantity: " 
                + warehouse.getOutstandingQuantity(pId.trim()) + "\n");
        }
    }

    private class ReceiveShipmentAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String pId = JOptionPane.showInputDialog(panel, "Enter Product ID:");
            if (pId == null || pId.trim().isEmpty()) return;
            String qtyStr = JOptionPane.showInputDialog(panel, "Enter Shipment Quantity Received:");
            if (qtyStr == null || qtyStr.trim().isEmpty()) return;

            try {
                int qty = Integer.parseInt(qtyStr.trim());
                int res = warehouse.receiveShipment(pId.trim(), qty);
                if (res == Warehouse.OPERATION_COMPLETED) {
                    Product p = warehouse.searchProduct(pId.trim());
                    displayArea.setText("Shipment processed for " + pId.trim() + ".\n"
                        + "Updated Product Stock: " + p.getQuantity() + "\n"
                        + "Remaining Waitlist: " + warehouse.getOutstandingQuantity(pId.trim()));
                } else {
                    displayArea.setText("Shipment failed. Please verify product ID and positive quantity.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid shipment quantity.");
            }
        }
    }

    private static class BecomeClerkAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            WarehouseContext.instance().changeState(WarehouseContext.BECOME_CLERK);
        }
    }

    private static class LogoutAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            WarehouseContext.instance().changeState(WarehouseContext.LOGOUT);
        }
    }
}
