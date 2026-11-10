import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ClerkMenuState extends WarehouseState {
    private static ClerkMenuState state;
    private final JPanel panel;
    private final JTextArea displayArea;

    private ClerkMenuState() {
        panel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        JButton addClientBtn = new JButton("Add Client");
        JButton showProductsBtn = new JButton("Show Products");
        JButton showClientsBtn = new JButton("Show All Clients");
        JButton showBalancesBtn = new JButton("Clients With Balance");
        JButton recordPaymentBtn = new JButton("Record Payment");
        JButton becomeClientBtn = new JButton("Become Client");
        JButton logoutBtn = new JButton("Logout");

        displayArea = new JTextArea(15, 40);
        displayArea.setEditable(false);

        // Dedicated action listener classes
        addClientBtn.addActionListener(new AddClientAction());
        showProductsBtn.addActionListener(new ShowProductsAction());
        showClientsBtn.addActionListener(new ShowClientsAction());
        showBalancesBtn.addActionListener(new ShowBalancesAction());
        recordPaymentBtn.addActionListener(new RecordPaymentAction());
        becomeClientBtn.addActionListener(new BecomeClientAction());
        logoutBtn.addActionListener(new LogoutAction());

        buttonPanel.add(addClientBtn);
        buttonPanel.add(showProductsBtn);
        buttonPanel.add(showClientsBtn);
        buttonPanel.add(showBalancesBtn);
        buttonPanel.add(recordPaymentBtn);
        buttonPanel.add(becomeClientBtn);
        buttonPanel.add(logoutBtn);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(displayArea), BorderLayout.CENTER);
    }

    public static ClerkMenuState instance() {
        if (state == null) {
            state = new ClerkMenuState();
        }
        return state;
    }

    @Override
    public JPanel getView() {
        displayArea.setText("");
        return panel;
    }

    // --- Dedicated Listener Classes ---

    private class AddClientAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog(panel, "Enter Client Name:");
            if (name == null || name.trim().isEmpty()) return;
            String address = JOptionPane.showInputDialog(panel, "Enter Client Address:");
            if (address == null || address.trim().isEmpty()) return;

            Client c = warehouse.addClient(name.trim(), address.trim());
            if (c != null) {
                displayArea.setText("Client added successfully:\n" + c);
            } else {
                displayArea.setText("Failed to add client. Please verify fields are not empty.");
            }
        }
    }

    private class ShowProductsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            displayArea.setText("=== Products in Catalog ===\n");
            Iterator<Product> it = warehouse.getProducts();
            while (it.hasNext()) {
                Product p = it.next();
                displayArea.append(p.toString() + ", Waitlisted: " 
                    + warehouse.getOutstandingQuantity(p.getProductId()) + "\n");
            }
        }
    }

    private class ShowClientsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            displayArea.setText("=== All Clients ===\n");
            Iterator<Client> it = warehouse.getClients();
            while (it.hasNext()) {
                displayArea.append(it.next().toString() + "\n");
            }
        }
    }

    private class ShowBalancesAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            displayArea.setText("=== Clients with Outstanding Balances ===\n");
            Iterator<Client> it = warehouse.getClients();
            while (it.hasNext()) {
                Client c = it.next();
                if (c.hasOutstandingBalance()) {
                    displayArea.append(c.toString() + "\n");
                }
            }
        }
    }

    private class RecordPaymentAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cId = JOptionPane.showInputDialog(panel, "Enter Client ID:");
            if (cId == null || cId.trim().isEmpty()) return;
            String amtStr = JOptionPane.showInputDialog(panel, "Enter Payment Amount:");
            if (amtStr == null || amtStr.trim().isEmpty()) return;

            try {
                double amount = Double.parseDouble(amtStr.trim());
                int res = warehouse.receivePayment(cId.trim(), amount);
                if (res == Warehouse.OPERATION_COMPLETED) {
                    Client c = warehouse.searchClient(cId.trim());
                    displayArea.setText("Payment recorded.\nUpdated Client Balance: $" 
                        + String.format("%.2f", c.getBalance()));
                } else {
                    displayArea.setText("Payment rejected. Check Client ID and amount.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid amount entered.");
            }
        }
    }

    private static class BecomeClientAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cId = JOptionPane.showInputDialog(
                WarehouseContext.instance().getFrame(),
                "Enter Client ID to switch to:"
            );
            if (cId != null && !cId.trim().isEmpty()) {
                String cleanId = cId.trim();
                if (Warehouse.instance().searchClient(cleanId) != null) {
                    WarehouseContext.instance().setClient(cleanId);
                    WarehouseContext.instance().changeState(WarehouseContext.BECOME_CLIENT);
                } else {
                    JOptionPane.showMessageDialog(
                        WarehouseContext.instance().getFrame(),
                        "Client not found."
                    );
                }
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
