package hxc.services.ecds.olapmodel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class OlapResultByArea {
    // AS CALLED BY HIBERNATE -- see OlapTransaction  @SqlResultSetMappings
    public static class RowMapping {
        private Integer a_CellID;
        private Integer a_AgentID;
        private Long count;
        private BigDecimal sum;
        private String txCode;
        private Boolean success;

        public RowMapping(
            Integer a_CellID,
            Integer a_AgentID,
            Long count,
            BigDecimal sum,
            String txCode,
            Boolean success
        ) {
            this.a_CellID = a_CellID;
            this.a_AgentID = a_AgentID;
            this.count = count;
            this.sum = sum;
            this.txCode = txCode;
            this.success = success;
        }

        public Integer getA_CellID() { return a_CellID; }
        public Integer getA_AgentID() { return a_AgentID; }
        public Long getCount() { return count; }
        public BigDecimal getSum() { return sum; }
        public String getTxCode() { return txCode; }
        public Boolean isSuccess() { return success; }
    }
    
	public static class AreaKey {
        public AreaKey(String name, String type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return this.name;
		}

		public String getType() {
			return this.type;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			AreaKey that = (AreaKey)o;
			return this.name.equals(that.name) && this.type.equals(that.type);
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}

		public String toString() {
			return name + ":" + type;
		}

		private String name;
		private String type;
		private int hashCode;
	}

    public static class AreaData {
        private String type;
        private List<AreaDataDetail> detail = new ArrayList<>();
        private HashSet<AreaKey> children = new HashSet<>();

        public AreaData(String type, List<String> transactionTypes) {
            this.type = type;
            for(String txType : transactionTypes) {
                detail.add(new AreaDataDetail(txType));
            }
        }

        public String getType() { return type; }
        public List<AreaDataDetail> getDetailList() { return detail; }

		public AreaData addChild(AreaKey child) {
			children.add(child);
			return this;
		}
		public HashSet<AreaKey> getChildren() {
			return children;
		}

		public void aggregate(HashMap<AreaKey, AreaData> areaData, List<AreaDataDetail> areaTotals) {
			for(AreaDataDetail aggAreaDetail: areaTotals) {
				for(AreaDataDetail thisAreaDetail: detail) {
					if(aggAreaDetail.getTransactionType().equals(thisAreaDetail.getTransactionType())) {
						aggAreaDetail.updateFromAreaData(thisAreaDetail);
					}
				}
			}
			for(AreaKey child : getChildren()) {
				AreaData childArea = areaData.get(child);
				if( childArea != null ) {
					childArea.aggregate(areaData, areaTotals);
				}
			}
		}

        public static class AreaDataDetail {
            private String transactionType;

            private BigDecimal successTransactionAmount = new BigDecimal(0);
            private BigDecimal failTransactionAmount = new BigDecimal(0);

            private Long successTransactionCount = 0L;
            private Long failTransactionCount = 0L;
            
            private HashSet<Integer> uniqueSuccessfulAgentIDs = new HashSet<>();


            public AreaDataDetail(String transactionType) {
                this.transactionType = transactionType;
            }

            public String getTransactionType() { return transactionType; }

            public Long getTotalTransactionCount() { return successTransactionCount + failTransactionCount; }

            public Long getSuccessTransactionCount() { return this.successTransactionCount; }
            public AreaDataDetail addToSuccessTransactionCount(Long successTransactionCount) {
                this.successTransactionCount += successTransactionCount;
                return this;
            }

            public Long getFailTransactionCount() { return failTransactionCount; }
            public AreaDataDetail addToFailTransactionCount(Long failTransactionCount) {
                this.failTransactionCount += failTransactionCount;
                return this;
            }

            public BigDecimal getTotalSumAmount() { return successTransactionAmount.add(failTransactionAmount); }
            
            public BigDecimal getSuccessTransactionAmount() { return successTransactionAmount; }
            public AreaDataDetail addToSuccessTransactionAmount(BigDecimal successTransactionAmount) {
                this.successTransactionAmount = this.successTransactionAmount.add(successTransactionAmount);
                return this;
            }

            public BigDecimal getFailTransactionAmount() { return failTransactionAmount; }
            public AreaDataDetail addToFailTransactionAmount(BigDecimal failTransactionAmount) {
                this.failTransactionAmount = this.failTransactionAmount.add(failTransactionAmount);
                return this;
            }



            public BigDecimal getAverageTransactionAmount() {
                if (this.getSuccessTransactionAmount().compareTo(BigDecimal.ZERO) == 0) return BigDecimal.valueOf(0);
                BigDecimal totalSuccessfulTXCount = BigDecimal.valueOf(this.getSuccessTransactionCount());
                return this.getSuccessTransactionAmount().divide(totalSuccessfulTXCount, 2, RoundingMode.HALF_EVEN);
            }

            public BigDecimal getAverageAgentAmount() {
                if (this.getSuccessTransactionAmount().compareTo(BigDecimal.ZERO) == 0) return BigDecimal.valueOf(0);
                BigDecimal uniqueSuccessfulAgentCount = BigDecimal.valueOf(this.getUniqueSuccessfulAgents());
                return this.getSuccessTransactionAmount().divide(uniqueSuccessfulAgentCount, 2, RoundingMode.HALF_EVEN);
            }

            public HashSet<Integer> getAllSuccessfulAgents() { return uniqueSuccessfulAgentIDs; }
            public Integer getUniqueSuccessfulAgents() { return uniqueSuccessfulAgentIDs.size(); }
            public AreaDataDetail addSuccessfulAgentID(Integer agentID) {
                this.uniqueSuccessfulAgentIDs.add(agentID);
                return this;
            }

            public void updateFromAreaData(AreaDataDetail areaData) {
                for(Integer agentID : areaData.getAllSuccessfulAgents()) {
                    this.addSuccessfulAgentID(agentID);
                }

                // counts
                this.addToFailTransactionCount(areaData.getFailTransactionCount());
                this.addToSuccessTransactionCount(areaData.getSuccessTransactionCount());

                // amounts
                this.addToFailTransactionAmount(areaData.getFailTransactionAmount());
                this.addToSuccessTransactionAmount(areaData.getSuccessTransactionAmount());
            }
        }
    }
}
