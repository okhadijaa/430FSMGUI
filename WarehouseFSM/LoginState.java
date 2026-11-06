public class LoginState extends WarehouseState {
    private static LoginState state;

    private LoginState() { }

    public static LoginState instance() {
        if (state == null) state = new LoginState();
        return state;
    }

    @Override
    public void run() {
        System.out.println("\n========== LOGIN MENU ==========");
        System.out.println("1. Login as Client");
        System.out.println("2. Login as Clerk");
        System.out.println("3. Login as Manager");
        System.out.println("4. Exit");
        System.out.println("5. Save Data");
        System.out.println("6. Load Data");

        switch (context().getCommand(6)) {
            case 1:
                String id = context().getId("Enter Client ID:");
                if (warehouse().searchClient(id) == null) {
                    System.out.println("Client not found.");
                } else {
                    context().setClient(id);
                    changeState(WarehouseContext.LOGIN_CLIENT);
                }
                break;
            case 2:
                changeState(WarehouseContext.LOGIN_CLERK);
                break;
            case 3:
                changeState(WarehouseContext.LOGIN_MANAGER);
                break;
            case 4:
                changeState(WarehouseContext.QUIT);
                break;
            case 5:
                context().saveData();
                break;
            case 6:
                context().loadData();
                break;
            default:
                break;
        }
    }
}
