import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class WarehouseContext {
    private static WarehouseContext context;
    private static Warehouse warehouse;
    private final JFrame frame;

    // --- State Identifiers (Rows in Matrix) ---
    public static final int LOGIN_STATE = 0;
    public static final int CLERK_STATE = 1;
    public static final int MANAGER_STATE = 2;
    public static final int CLIENT_STATE = 3;
    public static final int NUM_STATES = 4;

    // --- Event / Transition Identifiers (Columns in Matrix) ---
    public static final int CLERK_LOGIN = 0;
    public static final int MANAGER_LOGIN = 1;
    public static final int CLIENT_LOGIN = 2;
    public static final int BECOME_CLIENT = 3;
    public static final int BECOME_CLERK = 4;
    public static final int LOGOUT = 5;
    public static final int NUM_TRANSITIONS = 6;

    // --- Transition Matrix: [CurrentState][Event] -> NextState (-1 means invalid) ---
    private static final int[][] transitionTable = {
        /* Row 0: LOGIN_STATE   */ { CLERK_STATE, MANAGER_STATE, CLIENT_STATE, -1,           -1,          -1 },
        /* Row 1: CLERK_STATE   */ { -1,          -1,            -1,           CLIENT_STATE, -1,          LOGIN_STATE },
        /* Row 2: MANAGER_STATE */ { -1,          -1,            -1,           -1,           CLERK_STATE, LOGIN_STATE },
        /* Row 3: CLIENT_STATE  */ { -1,          -1,            -1,           -1,           -1,          -1 /* Handled dynamically */ }
    };

    private final WarehouseState[] states;
    private int currentState;
    private int previousState; // Remembers whether Client was launched from LOGIN or CLERK
    private String currentClientID;

    private WarehouseContext() {
        warehouse = Warehouse.instance();
        
        frame = new JFrame("Warehouse Management System");
        frame.setSize(750, 520);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        states = new WarehouseState[NUM_STATES];
        states[LOGIN_STATE] = LoginState.instance();
        states[CLERK_STATE] = ClerkMenuState.instance();
        states[MANAGER_STATE] = ManagerMenuState.instance();
        states[CLIENT_STATE] = ClientMenuState.instance();

        previousState = LOGIN_STATE;
        currentState = LOGIN_STATE;

        frame.getContentPane().setLayout(new BorderLayout());
        setPanel(states[currentState].getView());
        frame.setVisible(true);
    }

    public static WarehouseContext instance() {
        if (context == null) {
            context = new WarehouseContext();
        }
        return context;
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setClient(String cId) {
        this.currentClientID = cId;
    }

    public String getClient() {
        return currentClientID;
    }

    public void setPreviousState(int s) {
        this.previousState = s;
    }

    public int getPreviousState() {
        return previousState;
    }

    // Coordinates the FSM transitions using the matrix
    public void changeState(int transition) {
        int next;

        // Logout from client state dynamically returns to either Clerk or Login
        if (currentState == CLIENT_STATE && transition == LOGOUT) {
            next = previousState;
        } else {
            next = transitionTable[currentState][transition];
        }

        if (next != -1) {
            if (currentState == CLERK_STATE && transition == BECOME_CLIENT) {
                previousState = CLERK_STATE;
            } else if (currentState == LOGIN_STATE && transition == CLIENT_LOGIN) {
                previousState = LOGIN_STATE;
            }

            currentState = next;
            setPanel(states[currentState].getView());
        }
    }

    // Replaces the content pane of the JFrame with the current state's JPanel
    private void setPanel(JPanel panel) {
        frame.getContentPane().removeAll();
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    public static void main(String[] args) {
        WarehouseContext.instance();
    }
}
