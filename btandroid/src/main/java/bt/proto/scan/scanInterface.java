package bt.proto.scan;

import java.util.List;

public interface scanInterface {

    void setUpAdapterAndView(List<scanEntry> scanEntries);

    void chooseScanResultAt(int position);

    void forgetScanResultAt(int position);
}
