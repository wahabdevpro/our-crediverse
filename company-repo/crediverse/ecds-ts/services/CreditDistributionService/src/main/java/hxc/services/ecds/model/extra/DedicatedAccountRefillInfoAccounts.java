package hxc.services.ecds.model.extra;

import java.util.ArrayList;
import java.util.List;

public class DedicatedAccountRefillInfoAccounts {

    private List<DedicatedAccountRefillInfo> dedicatedAccountRefillInfos;

    public List<DedicatedAccountRefillInfo> getDedicatedAccountRefillInfos() {
        return dedicatedAccountRefillInfos;
    }

    public void addDedicatedAccountRefillInfos(DedicatedAccountRefillInfo dedicatedAccountRefillInfo){
        if(dedicatedAccountRefillInfos == null){
            dedicatedAccountRefillInfos = new ArrayList<>();
        }

        dedicatedAccountRefillInfos.add(dedicatedAccountRefillInfo);
    }
}
