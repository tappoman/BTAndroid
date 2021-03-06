package bt.proto.device;

import android.support.v7.widget.RecyclerView;

public class serviceEntry {

    private String name;
    private String uuid;
    private RecyclerView recyclerView;

    public serviceEntry(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
