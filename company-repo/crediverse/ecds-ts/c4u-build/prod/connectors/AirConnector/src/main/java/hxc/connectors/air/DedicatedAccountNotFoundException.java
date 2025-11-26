package hxc.connectors.air;

public class DedicatedAccountNotFoundException extends Exception {
    private String dedicatedAccountId;

    public DedicatedAccountNotFoundException(String dedicatedAccountId) {
        this.dedicatedAccountId = dedicatedAccountId;
    }

    public String getDedicatedAccountId() {
        return dedicatedAccountId;
    }
}
