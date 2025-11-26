package hxc.ecds.protocol.rest;

import static hxc.ecds.protocol.rest.MobileNumberFormatConfig.Phase.BEFORE;

import java.util.List;

public class MobileNumberFormatConfig extends TransactionServerResponse implements IValidatable {

    public enum Phase {
        BEFORE(0),
        MAINTENANCE_WINDOW(1),
        DUAL_PHASE(2),
        AFTER(3);

        private final int value;

        Phase(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
        
        public static Phase fromValue(int value) {
            for (Phase phase : values()) {
                if (phase.value == value) {
                    return phase;
                }
            }
            throw new RuntimeException("Wrong Phase value: " + value);
        }
    }

    protected Integer oldNumberLength = 8;
	protected Phase phase = BEFORE;
	protected String wrongBNumberMessageEn;
	protected String wrongBNumberMessageFr;

    @Override
    public List<Violation> validate() {
        return new Validator()
                .notLess("Old number length", oldNumberLength, 2)
                .notMore("Old number length", oldNumberLength, 50)
                .toList();
    }

    public Integer getOldNumberLength() {
        return oldNumberLength;
    }

    public void setOldNumberLength(Integer oldNumberLength) {
        this.oldNumberLength = oldNumberLength;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public String getWrongBNumberMessageEn() {
        return wrongBNumberMessageEn;
    }

    public void setWrongBNumberMessageEn(String wrongBNumberMessageEn) {
        this.wrongBNumberMessageEn = wrongBNumberMessageEn;
    }

    public String getWrongBNumberMessageFr() {
        return wrongBNumberMessageFr;
    }

    public void setWrongBNumberMessageFr(String wrongBNumberMessageFr) {
        this.wrongBNumberMessageFr = wrongBNumberMessageFr;
    }
}
