package bt.proto.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface SettingsDao {

    @Query("SELECT * FROM SettingsEntry")
    SettingsEntry getSettingsEntry();

    @Query("SELECT Count(*) FROM settingsEntry")
    int CheckIfEmpty();

    @Insert(onConflict = REPLACE)
    long insertEntry(SettingsEntry settingsEntry);

    @Update(onConflict = REPLACE)
    int updateEntry(SettingsEntry settingsEntry);

}