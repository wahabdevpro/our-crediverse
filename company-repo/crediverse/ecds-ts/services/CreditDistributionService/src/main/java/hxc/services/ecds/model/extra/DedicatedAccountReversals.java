package hxc.services.ecds.model.extra;

import java.util.ArrayList;
import java.util.List;

public class DedicatedAccountReversals {

    private List<DedicatedAccountReverseInfo> dedicatedAccountReversals;

    public List<DedicatedAccountReverseInfo> getDedicatedAccountReversals() {
        return dedicatedAccountReversals;
    }

    public void addDedicatedAccountReversal(DedicatedAccountReverseInfo dedicatedAccountReverseInfo){
        if(dedicatedAccountReversals == null){
            dedicatedAccountReversals = new ArrayList<>();
        }

        dedicatedAccountReversals.add(dedicatedAccountReverseInfo);
    }
}
