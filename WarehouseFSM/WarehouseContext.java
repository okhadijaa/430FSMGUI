import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.NoSuchElementException;

public class WarehouseContext {

    public static final int LOGIN = 0;
    public static final int CLIENT = 1;
    public static final int CLERK = 2;
    public static final int MANAGER = 3;
    private static final int EXIT = 4;

    public static final int LOGIN_CLIENT = 0;
    public static final int LOGIN_CLERK = 1;
    public static final int LOGIN_MANAGER = 2;
    public static final int BECOME_CLIENT = 3;
    public static final int BECOME_CLERK = 4;
    public static final int LOGOUT = 5;
    public static final int QUIT = 6;

    private static final int INVALID = -1;
    private static final int RETURN = -2;


    private static final int[][] MATRIX = {
        /* LOGIN   */     { CLIENT,    CLERK,     MANAGER,     INVALID,      INVALID,    INVALID, EXIT },
        /* CLIENT  */     { INVALID,   INVALID,   INVALID,     INVALID,      INVALID,    RETURN,  INVALID },
        /* CLERK   */     { INVALID,   INVALID,   INVALID,     CLIENT,       INVALID,    RETURN,  INVALID },
        /* MANAGER */     { INVALID,   INVALID,   INVALID,     INVALID,      CLERK,      LOGIN,   INVALID }
    };

    private static WarehouseContext context;
    private final WarehouseState[] states;
    private final BufferedReader reader;
    private int currentState;
    private int previousState;     
    private int clerkReturnState;  
    private String currentClientID;

    private WarehouseContext() {
        states = new WarehouseState[] {
            LoginState.instance(),
            ClientMenuState.instance(),
            ClerkMenuState.instance(),
            ManagerMenuState.instance()
        };
        reader = new BufferedReader(new InputStreamReader(System.in));
        currentState = LOGIN;
        previousState = LOGIN;
        clerkReturnState = LOGIN;
    }

    public static WarehouseContext instance() {
        if (context == null) {
            context = new WarehouseContext();
        }
        return context;
    }

    public Warehouse getWarehouse() {
        return Warehouse.instance();
    }

    public String getClient() {
        return currentClientID;
    }

    public void setClient(String clientId) {
        currentClientID = clientId;
    }

    public int getPreviousState() {
        return previousState;
    }

    public void setPreviousState(int state) {
        previousState = state;
    }

    public int getCurrentState() {
        return currentState;
    }

    public void changeState(int event) {
        if (event < 0 || event >= MATRIX[currentState].length) {
            System.out.println("Unknown transition.");
            return;
        }
        int next = MATRIX[currentState][event];
        if (next == INVALID) {
            System.out.println("Transition not allowed from this menu.");
            return;
        }
        if (next == RETURN) {
            next = (currentState == CLIENT) ? previousState : clerkReturnState;
        }

        if (event == LOGIN_CLIENT || event == BECOME_CLIENT) {
            previousState = currentState;
        }
        if (event == LOGIN_CLERK || event == BECOME_CLERK) {
            clerkReturnState = currentState;
        }
        if (currentState == CLIENT && event == LOGOUT) {
            currentClientID = null;
            previousState = LOGIN;
        }
        if (currentState == CLERK && event == LOGOUT) {
            clerkReturnState = LOGIN;
        }
        if (next == LOGIN) {
            currentClientID = null;
        }
        currentState = next;
    }

    public String getToken(String prompt) {
        while (true) {
            System.out.print(prompt + " ");
            try {
                String line = reader.readLine();
                if (line == null) {
                    throw new NoSuchElementException("End of input");
                }
                if (!line.trim().isEmpty()) {
                    return line.trim();
                }
            } catch (IOException e) {
                throw new NoSuchElementException("Input error: " + e.getMessage());
            }
            System.out.println("Input cannot be blank.");
        }
    }

    public String getId(String prompt) {
        return getToken(prompt).toUpperCase(Locale.ROOT);
    }

    public int getNumber(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(getToken(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    public int getNonNegativeNumber(String prompt) {
        while (true) {
            int number = getNumber(prompt);
            if (number >= 0) return number;
            System.out.println("Number cannot be negative.");
        }
    }

    public int getPositiveNumber(String prompt) {
        while (true) {
            int number = getNumber(prompt);
            if (number > 0) return number;
            System.out.println("Number must be greater than zero.");
        }
    }

    public double getDouble(String prompt) {
        while (true) {
            try {
                double number = Double.parseDouble(getToken(prompt));
                if (Double.isFinite(number) && number >= 0) return number;
            } catch (NumberFormatException e) {
            }
            System.out.println("Enter a finite, non-negative amount.");
        }
    }

    public int getCommand(int max) {
        while (true) {
            int option = getNumber("Choose option (1-" + max + "):");
            if (option >= 1 && option <= max) return option;
            System.out.println("Invalid option.");
        }
    }

    public boolean yesOrNo(String prompt) {
        String answer = getToken(prompt + " (Y/N):");
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    public void saveData() {
        System.out.println(Warehouse.save() ? "Data saved." : "Save failed.");
    }

    public void loadData() {
        if (!new File("WarehouseData").isFile()) {
            System.out.println("No saved WarehouseData file was found.");
        } else if (Warehouse.retrieve() == null) {
            System.out.println("Load failed.");
        } else {
            System.out.println("Saved warehouse data loaded.");
        }
    }

    public void process() {
        System.out.println("WAREHOUSE FINITE STATE MACHINE");
        while (currentState != EXIT) {
            try {
                states[currentState].run();
            } catch (NoSuchElementException e) {
                System.out.println("Input closed; exiting.");
                break;
            } catch (IllegalArgumentException | ArithmeticException e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
        }
        System.out.println("Warehouse program ended.");
    }

    public static void main(String[] args) {
        WarehouseContext.instance().process();
    }
}
