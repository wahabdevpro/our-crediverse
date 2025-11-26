package hxc.services.ecds.olapmodel;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import hxc.ecds.protocol.rest.Transaction;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Permission;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RuleCheckException;

@Table(name = "ap_analytics")
@Entity
@NamedQueries({ //
		@NamedQuery(name = "OlapAnalyticsHistory.getUniqueAgentsCount", query = "SELECT analytics FROM OlapAnalyticsHistory analytics WHERE transaction_type='UA' AND data_type='count' AND dt >= :startDate AND dt <= :endDate"), //
		@NamedQuery(name = "OlapAnalyticsHistory.getSalesCount", query = "SELECT analytics FROM OlapAnalyticsHistory analytics WHERE transaction_type='SL' AND data_type='count' AND dt >= :startDate AND dt <= :endDate"), //
		@NamedQuery(name = "OlapAnalyticsHistory.getTransfersCount", query = "SELECT analytics FROM OlapAnalyticsHistory analytics WHERE transaction_type='TX' AND data_type='count' AND dt >= :startDate AND dt <= :endDate"), //
		
		@NamedQuery(name = "OlapAnalyticsHistory.getSalesValue", query = "SELECT analytics FROM OlapAnalyticsHistory analytics WHERE transaction_type='SL' AND data_type='value' AND dt >= :startDate AND dt <= :endDate"), //
		@NamedQuery(name = "OlapAnalyticsHistory.getTransfersValue", query = "SELECT analytics FROM OlapAnalyticsHistory analytics WHERE transaction_type='TX' AND data_type='value' AND dt >= :startDate AND dt <= :endDate"), //
		@NamedQuery(name = "OlapAnalyticsHistory.getReplenishesValue", query = "SELECT analytics FROM OlapAnalyticsHistory analytics WHERE transaction_type='RP' AND data_type='value' AND dt >= :startDate AND dt <= :endDate"), //
})
public class OlapAnalyticsHistory implements Serializable
{
	private static final long serialVersionUID = 8509291050654030180L;
	
	public static final Permission MAY_VIEW = new Permission(false, true, Permission.GROUP_ANALYTICS, Permission.PERM_VIEW, "May view Analytics");
	public static final Permission MAY_CONFIGURE = new Permission(false, true, Permission.GROUP_ANALYTICS, Permission.PERM_CONFIGURE, "May configure Analytics");
	public static final Permission MAY_UPDATE = new Permission(false, true, Permission.GROUP_ANALYTICS, Permission.PERM_UPDATE, "May update Analytics");
	
	protected Date date;
	protected Long value;
	protected String txType;		// Although this is SET to a Transaction.Type.<TYPE>.getCode() -- it can be different and SPECIFIC to Analytics, so should NOT be labelled as a "Transaction.Type", but rather a "String"
	protected String dataType;
	
	public static enum DataType {
		COUNT_DISTINCT("count"),
		COUNT("count"),
		VALUE("value");
		
		private String dataType;
		
		DataType(String dataType)
		{
			this.dataType = dataType;
		}
		
		public String toString()
		{
			return this.dataType;
		}
	}
	
	public static enum QueryData {
		/*
		 * ("<QUERY NAME>",						STRING			// QUERY NAME / OlapAnalyticsHistory QUERY NAME as seen in the annotations @NamedQuery above
		 * DataType.<TYPE>						enum DataType	// What type of query action will we perform on the data? // COUNT * / SUM(column_name) / COUNT(DISTINCT(column_name)) -- see (enum) DataType above
		 * "<CODE>"								STRING			// Either "getCode()" of a REAL Transaction.Type .... or a MANUALLY ENTERED STRING as a custom code (like "UA" which means "UniqueAgents") specifically for analytics collection ONLY
		 * "<COLUMN_NAME>"						STRING			// the column name from the database (see getters for valid names below) that will be used in the case of "SUM()" or "COUNT(DISTINCT())"
		 */
		UACOUNT("OlapAnalyticsHistory.getUniqueAgentsCount",
				DataType.COUNT_DISTINCT,
				"UA",
				"a_MSISDN"),
		
		SLCOUNT("OlapAnalyticsHistory.getSalesCount",
				DataType.COUNT,
				Transaction.Type.SELL.getCode(),
				null),
		
		TXCOUNT("OlapAnalyticsHistory.getTransfersCount",
				DataType.COUNT,
				Transaction.Type.TRANSFER.getCode(),
				null),
		
		SLVALUE("OlapAnalyticsHistory.getSalesValue",
				DataType.VALUE,
				Transaction.Type.SELL.getCode(),
				"amount"),
		
		TXVALUE("OlapAnalyticsHistory.getTransfersValue",
				DataType.VALUE,
				Transaction.Type.TRANSFER.getCode(),
				"amount"),
		
		RPVALUE("OlapAnalyticsHistory.getReplenishesValue",
				DataType.VALUE,
				Transaction.Type.REPLENISH.getCode(),
				"amount");
		
		private String name;
		private final OlapAnalyticsHistory.DataType type;
		private String code;
		private String column;
		
		QueryData(String name, OlapAnalyticsHistory.DataType type, String code, String column)
		{
			this.name			= name;
			this.type			= type;
			this.code			= code;
			this.column			= column;
		}
		
		public String getName()
		{
			return this.name;
		}
		
		public OlapAnalyticsHistory.DataType dataType()
		{
			return this.type;
		}
		
		public String txTypeToString()
		{
			return this.code;
		}
		
		public String columnNameToString()
		{
			return this.column;
		}
	}
	
	@Id
	@Column(name = "dt", nullable = false)
	@Temporal(TemporalType.DATE)
	public Date getDate()
	{
		return this.date;
	}

	public OlapAnalyticsHistory setDate(Date date)
	{
		this.date = date;
		return this;
	}
	
	@Id
	@Column(name = "transaction_type", nullable = false)
	public String getTxType()
	{
		return this.txType;
	}

	public OlapAnalyticsHistory setTxType(String txType)
	{
		this.txType = txType;
		return this;
	}

	@Id
	@Column(name = "data_type", nullable = false)
	public String getDataType()
	{
		return this.dataType;
	}

	public OlapAnalyticsHistory setDataType(String dataType)
	{
		this.dataType = dataType;
		return this;
	}
	
	@Column(name = "value", nullable = false)
	public Long getValue()
	{
		return this.value;
	}

	public OlapAnalyticsHistory setValue(Long value)
	{
		this.value = value;
		return this;
	}
	
	public static List<OlapAnalyticsHistory> getAnalyticsHistory(EntityManagerEx apEm, QueryData queryData, String startDate, String endDate)
	{
		TypedQuery<OlapAnalyticsHistory> query = apEm.createNamedQuery(queryData.getName(), OlapAnalyticsHistory.class);
		
		query.setParameter("startDate", startDate);
		query.setParameter("endDate", endDate);
		
		List<OlapAnalyticsHistory> results = query.getResultList();

		return results;
	}
	
	public void setAll(String longDate, String txType, String dataType, Long value) throws Exception
	{
		this.setTxType(txType);
		this.setDataType(dataType);
		this.setValue(value);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		this.setDate(sdf.parse(longDate));
	}
	
	public static void loadMRD(EntityManager em, EntityManager emAp, Session session) throws RuleCheckException
	{
		Permission.loadMRD(emAp, MAY_VIEW, session);
		Permission.loadMRD(emAp, MAY_CONFIGURE, session);
		Permission.loadMRD(emAp, MAY_UPDATE, session);
	}
}
