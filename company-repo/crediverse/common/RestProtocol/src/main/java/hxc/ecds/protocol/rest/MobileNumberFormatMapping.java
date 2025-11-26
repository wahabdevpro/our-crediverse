package hxc.ecds.protocol.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MobileNumberFormatMapping extends TransactionServerResponse implements IValidatable {
    private Map<String, Set<String>> mapping;

    public MobileNumberFormatMapping() {
        mapping = new HashMap<>();
    }

    @Override
    public List<Violation> validate() {
        Validator validator = new Validator();
        for (Map.Entry<String, Set<String>> entry : mapping.entrySet()) {
            validator.exactLength("New prefix", entry.getKey(), 2)
                    .onlyDigits("New prefix", entry.getKey());
            for (String oldCode : entry.getValue()) {
                validator.exactLength("Old code", oldCode, 2)
                        .onlyDigits("Old code", oldCode);
            }
        }
        return validator.toList();
    }

    public Map<String, Set<String>> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, Set<String>> mapping) {
        this.mapping = mapping;
    }
}
