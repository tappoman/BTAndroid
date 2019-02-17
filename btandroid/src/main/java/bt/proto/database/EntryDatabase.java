package bt.proto.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import bt.proto.database.SettingsDao;
import bt.proto.database.SettingsEntry;


@Database(entities = {
        SettingsEntry.class, }, version = 2)

public abstract class EntryDatabase extends RoomDatabase{

    private static final String DB_NAME = "Entries.db";
    private static EntryDatabase INSTANCE;

    public static EntryDatabase getDataBase(Context context){
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    EntryDatabase.class,DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }

    public abstract SettingsDao settingsDao();

}
