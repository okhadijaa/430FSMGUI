public abstract class WarehouseState {
    public abstract void run();

    protected WarehouseContext context() {
        return WarehouseContext.instance();
    }

    protected Warehouse warehouse() {
        return context().getWarehouse();
    }

    protected void changeState(int event) {
        context().changeState(event);
    }
}
