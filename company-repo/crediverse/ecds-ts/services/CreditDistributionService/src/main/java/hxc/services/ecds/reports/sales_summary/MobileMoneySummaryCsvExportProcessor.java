package hxc.services.ecds.reports.sales_summary;

import hxc.ecds.protocol.rest.reports.SalesSummaryReportResultEntry;

public class MobileMoneySummaryCsvExportProcessor extends hxc.services.ecds.rest.batch.CsvExportProcessor<SalesSummaryReportResultEntry> {
    private static final String[] HEADINGS = new String[]{
        "NOMBRE_SUCCES_DE_TRANSACTIONS_CUMULE_MM", //	COUNT_SUCCESS_TRANSFER_MM
        "NOMBRE_ECHEC_DE_TRANSACTIONS_CUMULE_MM", //	COUNT_FAIL_TRANSFER_MM
        "NOMBRE_AGENT_UNIQUE_CUMULE_MM", //	COUNT_UNIQUE_AGENT_MM
        "MONTANT_TOTAL_CUMULE_MM", //	AMOUNT_TOTAL_TRANSFER_MM
        "MONTANT_MOYENNE_PAR_AGENT_MM", //	AVE_TRANSFER_AMOUNT_PER_AGENT_MM
        "MONTANT_MOYENNE_PAR_TRANSACTION_MM", //	AVE_TRANSFER_AMOUNT_PER_TRANSACTION_ MM
    };

    public MobileMoneySummaryCsvExportProcessor(int first) {
        super(HEADINGS, first, false);
    }

    @Override
    protected void write(SalesSummaryReportResultEntry record) {        
        put("NOMBRE_SUCCES_DE_TRANSACTIONS_CUMULE_MM", record.getMMTransferCountSuccess());
        put("NOMBRE_ECHEC_DE_TRANSACTIONS_CUMULE_MM", record.getMMTransferCountFailed());
        put("NOMBRE_AGENT_UNIQUE_CUMULE_MM", record.getMMReceivingAgentCount());        
        put("MONTANT_TOTAL_CUMULE_MM", record.getMMTotalTransferAmount());
        put("MONTANT_MOYENNE_PAR_AGENT_MM", record.getMMAverageTransferAmountPerAgent());
        put("MONTANT_MOYENNE_PAR_TRANSACTION_MM", record.getMMAverageTransferAmountPerTransaction());
    }
}
