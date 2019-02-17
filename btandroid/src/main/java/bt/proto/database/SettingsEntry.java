package bt.proto.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class SettingsEntry {


    @PrimaryKey(autoGenerate = true)
    private int id;
    private String mac;


    public SettingsEntry(int id, String mac) {
        this.id = id;
        this.mac = mac;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }



}

