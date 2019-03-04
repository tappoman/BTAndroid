package bt.proto.device;

import java.util.List;

class serviceEntryController {

    private final String TAG = "serviceEntryController";
    private serviceEntryInterface view;


    serviceEntryController(serviceEntryInterface view){
        this.view=view;
    }

    void createServiceList(final List<serviceEntry> services) {
        view.setUpServiceAdapterAndView(services);
    }

}
