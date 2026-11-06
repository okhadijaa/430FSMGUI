public class ClerkMenuState extends WarehouseState {
    private static ClerkMenuState state;

    private ClerkMenuState() { }

    public static ClerkMenuState instance() {
        if (state == null) state = new ClerkMenuState();
        return state;
    }

    @Override
    public void run() {
        System.out.println("\n========== CLERK MENU ==========");
        System.out.println("1. Add Client");
        System.out.println("2. Show Products / Stock / Prices");
        System.out.println("3. Show All Clients");
        System.out.println("4. Show Outstanding Balances");
        System.out.println("5. Record Client Payment");
        System.out.println("6. Become Client");
        System.out.println("7. Logout");

        switch (context().getCommand(7)) {
            case 1: addClient(); break;
            case 2: warehouse().displayProducts(); break;
            case 3: warehouse().displayClients(); break;
            case 4: warehouse().displayOutstandingBalances(); break;
            case 5: recordPayment(); break;
            case 6: becomeClient(); break;
            case 7: changeState(WarehouseContext.LOGOUT); break;
            default: break;
        }
    }

    private void addClient() {
        String name = context().getToken("Enter Client Name:");
        String address = context().getToken("Enter Client Address:");
        Client client = warehouse().addClient(name, address);
        System.out.println(client == null ? "Client could not be added." : "Created: " + client);
    }

    private void recordPayment() {
        String id = context().getId("Enter Client ID:");
        Client client = warehouse().searchClient(id);
        if (client == null) {
            System.out.println("Client not found.");
            return;
        }
        System.out.printf("Current balance: $%.2f%n", client.getBalance());
        double amount = context().getDouble("Payment amount:");
        int result = warehouse().receivePayment(id, amount);
        if (result == Warehouse.OPERATION_COMPLETED) {
            System.out.printf("Payment recorded. New balance: $%.2f%n", client.getBalance());
        } else {
            System.out.println("Payment failed. Amount must be positive.");
        }
    }

    private void becomeClient() {
        String id = context().getId("Enter Client ID:");
        if (warehouse().searchClient(id) == null) {
            System.out.println("Client not found. Remaining in Clerk Menu.");
        } else {
            context().setClient(id);
            changeState(WarehouseContext.BECOME_CLIENT);
        }
    }
}
