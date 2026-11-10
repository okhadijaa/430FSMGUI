import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class LoginState extends WarehouseState {
    private static LoginState state;
    private final JPanel panel;

    private LoginState() {
        panel = new JPanel(new FlowLayout());

        JButton clerkBtn = new JButton("Clerk Login");
        JButton managerBtn = new JButton("Manager Login");
        JButton clientBtn = new JButton("Client Login");

        // Dedicated action listener classes (avoids conditional source checking)
        clerkBtn.addActionListener(new ClerkLoginAction());
        managerBtn.addActionListener(new ManagerLoginAction());
        clientBtn.addActionListener(new ClientLoginAction());

        panel.add(new JLabel("Select Login Role:"));
        panel.add(clerkBtn);
        panel.add(managerBtn);
        panel.add(clientBtn);
    }

    public static LoginState instance() {
        if (state == null) {
            state = new LoginState();
        }
        return state;
    }

    @Override
    public JPanel getView() {
        return panel;
    }

    // --- Dedicated Listener Classes ---

    private static class ClerkLoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            WarehouseContext.instance().changeState(WarehouseContext.CLERK_LOGIN);
        }
    }

    private static class ManagerLoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            WarehouseContext.instance().changeState(WarehouseContext.MANAGER_LOGIN);
        }
    }

    private static class ClientLoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String clientId = JOptionPane.showInputDialog(
                WarehouseContext.instance().getFrame(),
                "Enter Client ID:"
            );

            if (clientId != null && !clientId.trim().isEmpty()) {
                String cleanId = clientId.trim();
                if (Warehouse.instance().searchClient(cleanId) != null) {
                    WarehouseContext.instance().setClient(cleanId);
                    WarehouseContext.instance().changeState(WarehouseContext.CLIENT_LOGIN);
                } else {
                    JOptionPane.showMessageDialog(
                        WarehouseContext.instance().getFrame(),
                        "Client not found."
                    );
                }
            }
        }
    }
}
