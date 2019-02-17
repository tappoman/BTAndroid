package bt.proto.scan;


public class scanEntry {

    private String mac;
    private String name;
    private int rssi;
    private boolean isChecked;

    public scanEntry(String mac, String name, int rssi, boolean isChecked){
        this.mac=mac;
        this.name=name;
        this.rssi=rssi;
        this.isChecked = isChecked;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public boolean getChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }


}
