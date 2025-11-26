package hxc.services.ecds.reports.sales_summary;

import hxc.ecds.protocol.rest.reports.SalesSummaryReportResultEntry;

public class SalesSummaryCsvExportProcessor extends hxc.services.ecds.rest.batch.CsvExportProcessor<SalesSummaryReportResultEntry> {
    private static final String[] HEADINGS = new String[]{
        "MONTANT_TOTAL", //	TOTAL_AMOUNT
        "MONTANT_TOTAL_AIRTIME", //	TOTAL_AIRTIME_AMOUNT
        "MONTANT_TOTAL_NON_AIRTIME", //	TOTAL_AMOUNT_NON_AIRTIME
        "NOMBRE_D'AGENTS", //	AGENT_COUNT
        "NOMBRE_D'AGENTS_AIRTIME", //	AGENT_COUNT_AIRTIME
        "NOMBRE_D'AGENTS_NON_AIRTIME", //	AGENT_COUNT_NON_AIRTIME
        "NOMBRE_DE_TRANSACTIONS_RÉUSSIES", //	SUCCESSFUL_TRANSACTION_COUNT
        "NOMBRE_DE_TRANSACTIONS_RÉUSSIES_AIRTIME", //	SUCCESSFUL_AIRTIME_TRANSACTION_COUNT
        "NOMBRE_DE_TRANSACTIONS_RÉUSSIES_NON_AIRTIME", //	SUCCESSFUL_NON_AIRTIME_TRANSACTION_COUNT
        "NOMBRE_DE_TRANSACTIONS_ÉCHOUÉES", //	FAILED_TRANSACTION_COUNT
        "NOMBRE_DE_TRANSACTIONS_ÉCHOUÉES_AIRTIME", //	FAILED_AIRTIME_TRANSACTION_COUNT
        "NOMBRE_DE_TRANSACTIONS_ÉCHOUÉES_NON_AIRTIME", //	FAILED_NON_AIRTIME_TRANSACTION_COUNT
        "MONTANT_MOYEN_PAR_AGENT", //	AVERAGE_AMOUNT_PER_AGENT
        "MONTANT_MOYEN_PAR_AGENT_AIRTIME", //	AVERAGE_AIRTIME_AMOUNT_PER_AGENT
        "MONTANT_MOYEN_PAR_AGENT_NON_AIRTIME", //	AVERAGE_NON_AIRTIME_AMOUNT_PER_AGENT
        "MONTANT_MOYEN_PAR_TRANSACTION", //	AVERAGE_AMOUNT_PER_TRANSACTION
        "MONTANT_MOYEN_PAR_TRANSACTION_AIRTIME", //	AVERAGE_AIRTIME_AMOUNT_PER_TRANSACTION
        "MONTANT_MOYEN_PAR_TRANSACTION_NON_AIRTIME", //	AVERAGE_NON_AIRTIME_AMOUNT_PER_TRANSACTION
        /*"NOMBRE_SUCCES_DE_TRANSACTIONS_CUMULE_MM", //	COUNT_SUCCESS_TRANSFER_MM
        "NOMBRE_ECHEC_DE_TRANSACTIONS_CUMULE_MM", //	COUNT_FAIL_TRANSFER_MM
        "NOMBRE_AGENT_UNIQUE_CUMULE_MM", //	COUNT_UNIQUE_AGENT_MM
        "MONTANT_TOTAL_CUMULE_MM", //	AMOUNT_TOTAL_TRANSFER_MM
        "MONTANT_MOYENNE_PAR_AGENT_MM", //	AVE_TRANSFER_AMOUNT_PER_AGENT_MM
        "MONTANT_MOYENNE_PAR_TRANSACTION_MM", //	AVE_TRANSFER_AMOUNT_PER_TRANSACTION_ MM*/
    };

    public SalesSummaryCsvExportProcessor(int first) {
        super(HEADINGS, first, false);
    }

    @Override
    protected void write(SalesSummaryReportResultEntry record) {
        
        put("MONTANT_TOTAL", record.getTotalAmount());
        put("MONTANT_TOTAL_AIRTIME", record.getTotalAirtimeAmount());
        put("MONTANT_TOTAL_NON_AIRTIME", record.getTotalAmountNonAirtime());        
        put("NOMBRE_D'AGENTS", record.getAgentCount());
        put("NOMBRE_D'AGENTS_AIRTIME", record.getAgentCountAirtime());
        put("NOMBRE_D'AGENTS_NON_AIRTIME", record.getAgentCountNonAirtime());
        put("NOMBRE_DE_TRANSACTIONS_RÉUSSIES", record.getSuccessfulTransactionCount());
        put("NOMBRE_DE_TRANSACTIONS_RÉUSSIES_AIRTIME", record.getSuccessfulAirtimeTransactionCount());
        put("NOMBRE_DE_TRANSACTIONS_RÉUSSIES_NON_AIRTIME", record.getSuccessfulNonAirtimeTransactionCount());
        put("NOMBRE_DE_TRANSACTIONS_ÉCHOUÉES", record.getFailedTransactionCount());
        put("NOMBRE_DE_TRANSACTIONS_ÉCHOUÉES_AIRTIME", record.getFailedAirtimeTransactionCount());
        put("NOMBRE_DE_TRANSACTIONS_ÉCHOUÉES_NON_AIRTIME", record.getFailedNonAirtimeTransactionCount());
        put("MONTANT_MOYEN_PAR_AGENT", record.getAverageAmountPerAgent());
        put("MONTANT_MOYEN_PAR_AGENT_AIRTIME", record.getAverageAirtimeAmountPerAgent());
        put("MONTANT_MOYEN_PAR_AGENT_NON_AIRTIME", record.getAverageNonAirtimeAmountPerAgent());
        put("MONTANT_MOYEN_PAR_TRANSACTION", record.getAverageAmountPerTransaction());
        put("MONTANT_MOYEN_PAR_TRANSACTION_AIRTIME", record.getAverageAirtimeAmountPerTransaction());
        put("MONTANT_MOYEN_PAR_TRANSACTION_NON_AIRTIME", record.getAverageNonAirtimeAmountPerTransaction());
    }
}
