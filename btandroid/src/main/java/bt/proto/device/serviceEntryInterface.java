package bt.proto.device;

import java.util.List;

public interface serviceEntryInterface {

    void setUpAdapterAndView(List<serviceEntry> serviceEntries);

    void chooseScanResultAt(int position);
}
