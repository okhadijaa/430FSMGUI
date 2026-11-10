import javax.swing.JPanel;

public abstract class WarehouseState {
    protected static Warehouse warehouse = Warehouse.instance();

    // Returns the GUI panel for this state to display in the main window
    public abstract JPanel getView();
}
