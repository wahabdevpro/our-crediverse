package hxc.services.ecds.reports;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

import hxc.ecds.protocol.rest.reports.DailyPerformanceByAreaReportParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.util.DateHelper;
import hxc.ecds.protocol.rest.util.TimeInterval;
import hxc.services.ecds.model.Area;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.olapmodel.OlapResultByArea;
import hxc.services.ecds.olapmodel.OlapResultByArea.AreaKey;
import hxc.services.ecds.olapmodel.OlapResultByArea.AreaData;
import hxc.services.ecds.olapmodel.OlapResultByArea.AreaData.AreaDataDetail;
import hxc.services.ecds.olapmodel.OlapTransaction;

public class DailyPerformanceByAreaReport
	extends hxc.ecds.protocol.rest.reports.ReportsByArea
{
	private static Logger logger = LoggerFactory.getLogger(DailyPerformanceByAreaReport.class);

	public static class ResultEntry extends hxc.ecds.protocol.rest.reports.DailyPerformanceByAreaResultEntry
	{
		public ResultEntry(String areaName, String areaType,long transactionCount_SL, long successTransactionCount_SL, long failTransactionCount_SL,
			long uniqueAgentCount_SL, BigDecimal totalAmount_SL, BigDecimal averageAgentAmount_SL, BigDecimal averageTransactionAmount_SL,
			long transactionCount_ST, long successTransactionCount_ST, long failTransactionCount_ST,
			long uniqueAgentCount_ST, BigDecimal totalAmount_ST, BigDecimal averageAgentAmount_ST, BigDecimal averageTransactionAmount_ST)
		{
			super();
			this.setAreaName(areaName);
			this.setAreaType(areaType);
			this.setTransactionCount_SL(Long.valueOf(transactionCount_SL).intValue());
			this.setSuccessTransactionCount_SL(Long.valueOf(successTransactionCount_SL).intValue());
			this.setFailTransactionCount_SL(Long.valueOf(failTransactionCount_SL).intValue());
			this.setUniqueAgentCount_SL(Long.valueOf(uniqueAgentCount_SL).intValue());
			this.setTotalAmount_SL(totalAmount_SL);
			this.setAverageAgentAmount_SL(averageAgentAmount_SL);
			this.setAverageTransactionAmount_SL(averageTransactionAmount_SL);

			this.setTransactionCount_ST(Long.valueOf(transactionCount_ST).intValue());
			this.setSuccessTransactionCount_ST(Long.valueOf(successTransactionCount_ST).intValue());
			this.setFailTransactionCount_ST(Long.valueOf(failTransactionCount_ST).intValue());
			this.setUniqueAgentCount_ST(Long.valueOf(uniqueAgentCount_ST).intValue());
			this.setTotalAmount_ST(totalAmount_ST);
			this.setAverageAgentAmount_ST(averageAgentAmount_ST);
			this.setAverageTransactionAmount_ST(averageTransactionAmount_ST);
		}
	}

	public static class ResultFieldFactory implements Report.IResultFieldFactory
	{
		private static ResultFieldFactory INSTANCE = new ResultFieldFactory();

		@Override
		public Class<ResultField> getResultFieldClass()
		{
			return ResultField.class;
		}

		@Override
		public ResultField fromIdentifier(String identifier) throws Exception
		{
			return ResultField.fromIdentifier(identifier);
		}

		public static ResultFieldFactory getInstance()
		{
			return INSTANCE;
		}
	}

	public static class Processor
	{
		private final double unknownAreasWarningThresholdPercent = 2D;
		private final List<String> areaAggregationTXTypes = Arrays.asList(Transaction.Type.Code.SELL, Transaction.Type.Code.SELF_TOPUP);

		private EntityManager oltpEm;
		private EntityManager em;
		private int companyID;

		private TimeInterval timeInterval;
		private Report.RelativeTimeRange relativeTimeRange;

		public EntityManager getEm()
		{
			return this.em;
		}

		public TimeInterval getTimeInterval()
		{
			return this.timeInterval;
		}

		public Report.RelativeTimeRange getRelativeTimeRange()
		{
			return relativeTimeRange;
		}

		public Processor(EntityManager oltpEm, EntityManager em, int companyID) throws Exception
		{
			this.oltpEm = oltpEm;
			this.em = em;
			this.companyID = companyID;
		}
		public Processor(EntityManager oltpEm,EntityManager em, int companyID, DailyPerformanceByAreaReportParameters parameters, Date relativeTimeRangeReference) throws Exception
		{
			this.oltpEm = oltpEm;
			this.em = em;
			this.companyID = companyID;
			this.setParameters(parameters, relativeTimeRangeReference);
		}
		public void setParameters(DailyPerformanceByAreaReportParameters parameters, String relativeTimeRangeReferenceString) throws Exception
		{
			Date relativeTimeRangeReference = null;
			if ( relativeTimeRangeReferenceString != null )
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
				relativeTimeRangeReference = sdf.parse(relativeTimeRangeReferenceString);
			}
			this.setParameters(parameters, relativeTimeRangeReference);
		}

		public void setParameters(DailyPerformanceByAreaReportParameters parameters, Date relativeTimeRangeReference) throws Exception
		{
			if ( parameters.getRelativeTimeRange() != null )
			{
				this.relativeTimeRange = parameters.getRelativeTimeRange();
				this.timeInterval = this.relativeTimeRange.resolve(relativeTimeRangeReference != null ? relativeTimeRangeReference : new Date());
			}
			else {
				this.timeInterval = parameters.getTimeInterval();
			}
		}


		public void setCurrentDayTimeInterval() throws Exception
		{
			this.relativeTimeRange = Report.RelativeTimeRange.PREVIOUS_HOUR;
			TimeInterval timeInterval = this.relativeTimeRange.resolve(new Date());
			timeInterval.setStartDate(DateHelper.startOf(timeInterval.getStartDate(), Calendar.DATE).getTime());
			this.timeInterval = timeInterval;
		}
		
		private static class ByAreaReportData {
			public static HashMap<Integer, Area.SimpleAreaMapping> cellIDToAreaMapping = new HashMap<>();
			public static HashMap<Integer, Area.SimpleAreaMapping> areaIDToAreaMapping = new HashMap<>();
			public static HashMap<Integer, Area.SimpleAreaMapping> childAreasWithRecursiveParentMapping = new HashMap<>();

			public static HashSet<String> listOfRootAreas = new HashSet<>();
			public static HashMap<Area.SimpleAreaMapping, Area.SimpleAreaMapping> rootAreaToNextParentMapping = new HashMap<>();
			public static HashMap<AreaKey, AreaData> finalAreaData = new HashMap<>();

			public static void resetByAreaReportData() {
				ByAreaReportData.cellIDToAreaMapping = new HashMap<>();
				ByAreaReportData.childAreasWithRecursiveParentMapping = new HashMap<>();
				ByAreaReportData.listOfRootAreas = new HashSet<>();
				ByAreaReportData.rootAreaToNextParentMapping = new HashMap<>();
				ByAreaReportData.finalAreaData = new HashMap<>();
			}
		}

		public List<DailyPerformanceByAreaReport.ResultEntry> entries(int first, int max) throws Exception
		{
			List<DailyPerformanceByAreaReport.ResultEntry> entries = new ArrayList<DailyPerformanceByAreaReport.ResultEntry> ();
			if (first != 0 || max != 0)
			{
				// If we have the CURRENT_DAY , then we actually want to round DOWN the endTime to the nearest hour boundary
				//  This is to comply with stakeholder requirements
				if (this.relativeTimeRange.name().equals("CURRENT_DAY") || this.timeInterval == null) {
					if (this.timeInterval == null) logger.warn("No Time Interval set, resorting to DAY");
					
					this.setCurrentDayTimeInterval();
				}

				List<OlapResultByArea.RowMapping> txSellResults = OlapTransaction.getAggregationByArea(this.em, Transaction.Type.Code.SELL, this.timeInterval.getStartDate(), this.timeInterval.getEndDate());
				List<OlapResultByArea.RowMapping> txSelfTopupResults = OlapTransaction.getAggregationByArea(this.em, Transaction.Type.Code.SELF_TOPUP, this.timeInterval.getStartDate(), this.timeInterval.getEndDate());
				List<OlapResultByArea.RowMapping> completeResults = new ArrayList<>();
				completeResults.addAll(txSellResults);
				completeResults.addAll(txSelfTopupResults);

				// Set to empty hash sets / hash maps
				ByAreaReportData.resetByAreaReportData();

				entries = postprocessResult(completeResults, first, max);
			}

			return entries;
		}

		private List<ResultEntry> postprocessResult(List<OlapResultByArea.RowMapping> queryResults, int first, int max) {
			List<DailyPerformanceByAreaReport.ResultEntry> entries = new ArrayList<DailyPerformanceByAreaReport.ResultEntry> ();

			ByAreaReportData.finalAreaData.put(
				new AreaKey(Area.UNKNOWN_AREA, Area.UNKNOWN_AREA_TYPE),
				new AreaData(Area.UNKNOWN_AREA_TYPE, this.areaAggregationTXTypes)
			);

			this.getCellAreas(queryResults, ByAreaReportData.cellIDToAreaMapping, ByAreaReportData.areaIDToAreaMapping);

			this.aggreagateAreaData(queryResults);

			Double percentageInUnknownArea = this.getUnknownAreasPercent();

			if (percentageInUnknownArea > this.unknownAreasWarningThresholdPercent) {
				logger.warn("{}% of areas are 'Unknown'. Concurrent Support may be requested to investigate.", percentageInUnknownArea);
			}

			for (Map.Entry<AreaKey, AreaData> singleEntry : ByAreaReportData.finalAreaData.entrySet() ) {
				String areaName = singleEntry.getKey().getName();
				String areaType = singleEntry.getKey().getType();

				// SL = Sale transactions
				Long successTransactionCount_SL = 0L;
				Long failTransactionCount_SL = 0L;
				Long uniqueSuccessfulAgentCount_SL = 0L;
				BigDecimal averageAgentAmount_SL = new BigDecimal("0");
				BigDecimal averageTransactionAmount_SL = new BigDecimal("0");
				Long transactionCount_SL = 0L;
				BigDecimal totalAmount_SL = new BigDecimal("0");

				// ST = Self Topup transactions
				Long successTransactionCount_ST = 0L;
				Long failTransactionCount_ST = 0L;
				Long uniqueSuccessfulAgentCount_ST = 0L;
				BigDecimal averageAgentAmount_ST = new BigDecimal("0");
				BigDecimal averageTransactionAmount_ST = new BigDecimal("0");
				Long transactionCount_ST = 0L;
				BigDecimal totalAmount_ST = new BigDecimal("0");

				List<AreaData.AreaDataDetail> areaDataDetails = new ArrayList<>();
				areaDataDetails.add(new AreaData.AreaDataDetail(Transaction.Type.Code.SELL));
				areaDataDetails.add(new AreaData.AreaDataDetail(Transaction.Type.Code.SELF_TOPUP));

				singleEntry.getValue().aggregate(ByAreaReportData.finalAreaData, areaDataDetails);

				for(AreaData.AreaDataDetail areaDetail: areaDataDetails) {
					if (areaDetail.getTransactionType().equals(Transaction.Type.Code.SELL)) {
						successTransactionCount_SL = areaDetail.getSuccessTransactionCount();
						failTransactionCount_SL = areaDetail.getFailTransactionCount();
						uniqueSuccessfulAgentCount_SL = Long.valueOf(areaDetail.getUniqueSuccessfulAgents());
						averageAgentAmount_SL = areaDetail.getAverageAgentAmount();
						averageTransactionAmount_SL = areaDetail.getAverageTransactionAmount();
						transactionCount_SL = areaDetail.getTotalTransactionCount();
						// total is success only
						totalAmount_SL = areaDetail.getSuccessTransactionAmount();
					} else if (areaDetail.getTransactionType().equals(Transaction.Type.Code.SELF_TOPUP)) {
						successTransactionCount_ST = areaDetail.getSuccessTransactionCount();
						failTransactionCount_ST = areaDetail.getFailTransactionCount();
						uniqueSuccessfulAgentCount_ST = Long.valueOf(areaDetail.getUniqueSuccessfulAgents());
						averageAgentAmount_ST = areaDetail.getAverageAgentAmount();
						averageTransactionAmount_ST = areaDetail.getAverageTransactionAmount();
						transactionCount_ST = areaDetail.getTotalTransactionCount();
						// total is success only
						totalAmount_ST = areaDetail.getSuccessTransactionAmount();
					}
				}

				ResultEntry areaEntry = new ResultEntry(
					areaName,
					areaType,
					// SL
					transactionCount_SL,
					successTransactionCount_SL,
					failTransactionCount_SL,
					uniqueSuccessfulAgentCount_SL,
					totalAmount_SL,
					averageAgentAmount_SL,
					averageTransactionAmount_SL,
					// ST
					transactionCount_ST,
					successTransactionCount_ST,
					failTransactionCount_ST,
					uniqueSuccessfulAgentCount_ST,
					totalAmount_ST,
					averageAgentAmount_ST,
					averageTransactionAmount_ST

				);
				entries.add(areaEntry);
			}	


			return entries;
		}
	
		private void getCellAreas(List<OlapResultByArea.RowMapping> aggregationByArea, HashMap<Integer, Area.SimpleAreaMapping> cellIDToArea, HashMap<Integer, Area.SimpleAreaMapping> areaIDToArea) {
			List<Area.SimpleAreaMapping> areas = Area.findAllSimple(this.oltpEm, companyID);

			for(Area.SimpleAreaMapping areaToCellMapping : areas) {
				cellIDToArea.put(areaToCellMapping.getCellID(), areaToCellMapping);
				areaIDToArea.put(areaToCellMapping.getAreaID(), areaToCellMapping);
			}
		}

		private void aggreagateAreaData(List<OlapResultByArea.RowMapping> queryResults) {
			/**
			 * CREATE ROOT and Unknown areas ... first pass
			 */
			List<OlapResultByArea.RowMapping> nullCellsOrWithoutArea = new ArrayList<>();
			for (OlapResultByArea.RowMapping row : queryResults) {

				AreaData currentArea;

				Integer cellID = row.getA_CellID();
				Boolean unknownCellArea = cellID == null;
				AreaKey unkAreaKey = new AreaKey(Area.UNKNOWN_AREA, Area.UNKNOWN_AREA_TYPE);

				if (unknownCellArea) {
					nullCellsOrWithoutArea.add(row);
					currentArea = ByAreaReportData.finalAreaData.get(unkAreaKey);
				} else {
					Area.SimpleAreaMapping cellArea = ByAreaReportData.cellIDToAreaMapping.get(cellID);

					if (cellArea == null) {
						nullCellsOrWithoutArea.add(row);
						currentArea = ByAreaReportData.finalAreaData.get(unkAreaKey);
					} else {
						//ByAreaReportData.rootAreaToNextParentMapping.put(cellArea, null);
						String areaName = cellArea.getAreaName();
						String areaType = cellArea.getAreaType();
						AreaKey areaKey = new AreaKey(areaName, areaType);

						// cellID is real // cellArea exists
						Boolean noFinalArea = ByAreaReportData.finalAreaData.get(areaKey) == null;
						if (noFinalArea) {
							ByAreaReportData.finalAreaData.put(areaKey, new AreaData(areaType, this.areaAggregationTXTypes));
						}
						currentArea = ByAreaReportData.finalAreaData.get(areaKey);

						Integer nextParentID = cellArea.getParentID();
						AreaKey lastChildKey = areaKey;
						while (nextParentID != null) {
							Area.SimpleAreaMapping parentArea = ByAreaReportData.areaIDToAreaMapping.get(nextParentID);
							if (parentArea != null) {
								AreaKey parentAreaKey = new AreaKey(parentArea.getAreaName(), parentArea.getAreaType());
								AreaData parentAreaData = ByAreaReportData.finalAreaData.get(parentAreaKey);
								if (parentAreaData==null) {
									ByAreaReportData.finalAreaData.put(parentAreaKey, new AreaData(parentArea.getAreaType(), this.areaAggregationTXTypes));
									parentAreaData = ByAreaReportData.finalAreaData.get(parentAreaKey);
								}
								parentAreaData.addChild(lastChildKey);
								nextParentID = parentArea.getParentID();
								lastChildKey = parentAreaKey;
							}
							else
								nextParentID = null;
						}
					}
				}

				for(AreaData.AreaDataDetail currentAreaDetail: currentArea.getDetailList()) {
					if (row.getTxCode().equals(currentAreaDetail.getTransactionType())) {
						if (row.isSuccess()) {
							currentAreaDetail
								.addToSuccessTransactionCount(row.getCount())
								.addToSuccessTransactionAmount(row.getSum())
								.addSuccessfulAgentID(row.getA_AgentID());
						} else {
							currentAreaDetail
								.addToFailTransactionCount(row.getCount())
								.addToFailTransactionAmount(row.getSum());
						}
					}
				}
			}
			queryResults.removeAll(nullCellsOrWithoutArea);
		}
		
		private double getUnknownAreasPercent() {
			Double unknownAreaCount = 0D;
			Double knownAreaCount = 0D;

			for(Map.Entry<AreaKey, AreaData> entry : ByAreaReportData.finalAreaData.entrySet()) {
				String name = entry.getKey().getName();
				AreaData area = entry.getValue();
				for (AreaDataDetail areaDetail : area.getDetailList())
				{
					Long count = areaDetail.getTotalTransactionCount();
					if (name.equals(Area.UNKNOWN_AREA)) {
						unknownAreaCount += count;
					} else {
						knownAreaCount += count;
					}
				}
			}


			double totalAreaCount = unknownAreaCount + knownAreaCount;
			double percentageInUnknownArea = (double) (unknownAreaCount * 100 / totalAreaCount);

			// 2 decimal places
			return Double.valueOf(Math.round(percentageInUnknownArea * 100)) / 100;
		}
	}

	public static class CsvExportProcessor extends hxc.services.ecds.rest.batch.CsvExportProcessor<DailyPerformanceByAreaReport.ResultEntry>
	{
		private static final String[] HEADINGS = new String[] {
				"NOME_DE_LA_ZONE",
				"TYPE_DE_ZONE",
				"NOMBRE_DE_TRANSACTIONS_AIRTIME",
				"NOMBRE_DE_TRANSACTIONS_SUCCES_AIRTIME",
				"NOMBRE_DE_TRANSACTIONS_ECHEC_AIRTIME",
				"NOMBRE_DE_CABINES_AIRTIME",
				"MONTANT_TOTAL_AIRTIME",
				"MOYENNE_PAR_CABINE_AIRTIME",
				"MONYENNE_PAR_TRANSACTION_AIRTIME",

				"NOMBRE_DE_TRANSACTIONS_SELFTOPUP",
				"NOMBRE_DE_TRANSACTIONS_SUCCES_SELFTOPUP",
				"NOMBRE_DE_TRANSACTIONS_ECHEC_SELFTOPUP",
				"NOMBRE_DE_CABINES_SELFTOPUP",
				"MONTANT_TOTAL_SELFTOPUP",
				"MOYENNE_PAR_CABINE_SELFTOPUP",
				"MONYENNE_PAR_TRANSACTION_SELFTOPUP",
		};

		public CsvExportProcessor(int first)
		{
			super(HEADINGS, first, false);
		}

		@Override
		protected void write(DailyPerformanceByAreaReport.ResultEntry record)
		{
			put("NOME_DE_LA_ZONE", record.getAreaName());
            		put("TYPE_DE_ZONE", record.getAreaType());
			put("NOMBRE_DE_TRANSACTIONS_AIRTIME", record.getTransactionCount_SL());
			put("NOMBRE_DE_TRANSACTIONS_SUCCES_AIRTIME",record.getSuccessTransactionCount_SL());
			put("NOMBRE_DE_TRANSACTIONS_ECHEC_AIRTIME", record.getFailTransactionCount_SL());
			put("NOMBRE_DE_CABINES_AIRTIME", record.getUniqueAgentCount_SL());
			put("MONTANT_TOTAL_AIRTIME", record.getTotalAmount_SL());
			put("MOYENNE_PAR_CABINE_AIRTIME", record.getAverageAgentAmount_SL());
			put("MONYENNE_PAR_TRANSACTION_AIRTIME", record.getAverageTransactionAmount_SL());

			put("NOMBRE_DE_TRANSACTIONS_SELFTOPUP", record.getTransactionCount_ST());
			put("NOMBRE_DE_TRANSACTIONS_SUCCES_SELFTOPUP",record.getSuccessTransactionCount_ST());
			put("NOMBRE_DE_TRANSACTIONS_ECHEC_SELFTOPUP", record.getFailTransactionCount_ST());
			put("NOMBRE_DE_CABINES_SELFTOPUP", record.getUniqueAgentCount_ST());
			put("MONTANT_TOTAL_SELFTOPUP", record.getTotalAmount_ST());
			put("MOYENNE_PAR_CABINE_SELFTOPUP", record.getAverageAgentAmount_ST());
			put("MONYENNE_PAR_TRANSACTION_SELFTOPUP", record.getAverageTransactionAmount_ST());
		}
	}
}
