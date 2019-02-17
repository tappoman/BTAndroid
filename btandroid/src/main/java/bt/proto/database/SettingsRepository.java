package bt.proto.database;

public class SettingsRepository {

    private final SettingsDao settingsDao;

    public SettingsRepository(SettingsDao settingsDao) {
        this.settingsDao = settingsDao;
    }

    public long newSettingsEntry(SettingsEntry settingsEntry){
        return settingsDao.insertEntry(settingsEntry);
    }

    public int updateEntry(SettingsEntry settingsEntry){
        return settingsDao.updateEntry(settingsEntry);
    }

}
