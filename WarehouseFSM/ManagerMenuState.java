public class ManagerMenuState extends WarehouseState {
    private static ManagerMenuState state;

    private ManagerMenuState() { }

    public static ManagerMenuState instance() {
        if (state == null) state = new ManagerMenuState();
        return state;
    }

    @Override
    public void run() {
        System.out.println("\n========== MANAGER MENU ==========");
        System.out.println("1. Add Product");
        System.out.println("2. Display Product Waitlist");
        System.out.println("3. Receive Shipment");
        System.out.println("4. Become Clerk");
        System.out.println("5. Logout");

        switch (context().getCommand(5)) {
            case 1: addProduct(); break;
            case 2: warehouse().displayWaitlist(context().getId("Enter Product ID:")); break;
            case 3: receiveShipment(); break;
            case 4: changeState(WarehouseContext.BECOME_CLERK); break;
            case 5: changeState(WarehouseContext.LOGOUT); break;
            default: break;
        }
    }

    private void addProduct() {
        String name = context().getToken("Enter Product Name:");
        int quantity = context().getNonNegativeNumber("Initial quantity:");
        double price = context().getDouble("Unit price:");
        Product product = warehouse().addProduct(name, quantity, price);
        System.out.println(product == null ? "Product could not be added." : "Created: " + product);
    }

    private void receiveShipment() {
        String id = context().getId("Enter Product ID:");
        if (warehouse().searchProduct(id) == null) {
            System.out.println("Product not found.");
            return;
        }
        int quantity = context().getPositiveNumber("Shipment quantity:");
        int result = warehouse().receiveShipment(id, quantity);
        if (result == Warehouse.OPERATION_COMPLETED) {
            System.out.println("Shipment received. Waitlisted orders fulfilled first.");
            System.out.println("Stock on hand: " + warehouse().searchProduct(id).getQuantity());
            System.out.println("Still waitlisted: " + warehouse().getOutstandingQuantity(id));
        } else {
            System.out.println("Shipment failed.");
        }
    }
}
