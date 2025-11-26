package cs.dto;

import hxc.ecds.protocol.rest.Agent;

public enum StateEnum {
    ACTIVE(Agent.STATE_ACTIVE),
    SUSPENDED(Agent.STATE_SUSPENDED),
    DEACTIVATED(Agent.STATE_DEACTIVATED),
    PERMANENT(Agent.STATE_PERMANENT);
    private String val;

    private StateEnum(String val) {
        this.val = val.toUpperCase();
    }

    public String getVal() {
        return this.val;
    }

    public static StateEnum fromString(String val) {
        StateEnum result = DEACTIVATED;
        if (val != null) {
            switch (val) {
                case Agent.STATE_ACTIVE:
                    result = ACTIVE;
                    break;
                case Agent.STATE_SUSPENDED:
                    result = SUSPENDED;
                    break;
                case Agent.STATE_DEACTIVATED:
                    result = DEACTIVATED;
                    break;
                case Agent.STATE_PERMANENT:
                    result = PERMANENT;
                    break;
            }
        }
        return result;
    }
}
