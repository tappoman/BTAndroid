package bt.proto.device;

import java.util.List;

class characteristicEntryController {

    private final String TAG = "serviceEntryController";
    private characteristicEntryInterface view;


    characteristicEntryController(characteristicEntryInterface view){
        this.view=view;
    }

    void createCharacteristicList(final List<characteristicEntry> characteristics) {
        view.setUpCharacteristicAdapterAndView(characteristics);
    }

}
