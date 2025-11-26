package cs.dto.non_airtime;

import cs.dto.StateEnum;
import hxc.ecds.protocol.rest.Agent;

public class AgentDetails {
    private String agentId;
    private Tier tier;
    private StateEnum state;

    public AgentDetails(Agent agent, hxc.ecds.protocol.rest.Tier tier) {
        this.agentId = String.valueOf(agent.getId());
        this.state = StateEnum.fromString(agent.getState());
        this.tier = new Tier(tier);
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Tier getTier() {
        return tier;
    }

    public void setTier(Tier tier) {
        this.tier = tier;
    }

    public StateEnum getState() {
        return state;
    }

    public void setState(StateEnum state) {
        this.state = state;
    }

    public static class Tier {
        protected String name;
        protected String type;

        public Tier(hxc.ecds.protocol.rest.Tier tier) {
            this.type = tier.getType();
            this.name = tier.getName();
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }
}
