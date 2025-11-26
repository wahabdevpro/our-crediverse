package hxc.ecds.protocol.rest.util;

import java.util.HashMap;

public enum ChannelTypes
{
	MM("Mobile Money"),
    OT("Other");

    private String value;

    private ChannelTypes(String value) {
        this.value = value;
    }

    public String getValue()
    {
        return this.value;
    }

    public static HashMap<String, String> getChannelTypes() {
        HashMap<String, String> values = new HashMap<>();
        for (ChannelTypes type: ChannelTypes.values()) {
            values.put(type.name(), type.getValue());
        }

        return values;
    }

    public static ChannelTypes fromString(String value)
    {
        ChannelTypes result = OT;
        if (value != null)
        {
            switch (value)
            {
                case "Mobile Money":
                    result = MM;
                    break;
                case "Other":
                    result = OT;
                    break;
            }
        }
        return result;
    }
}
