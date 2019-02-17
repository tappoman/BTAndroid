package bt.proto.device;

import java.util.ArrayList;
import java.util.List;

public class serviceEntryController {

    private final String TAG = "serviceEntryController";
    private serviceEntryInterface view;

    List<serviceEntry> deviceEntries = new ArrayList<>();

    serviceEntryController(serviceEntryInterface view){
        this.view=view;
    }


}
