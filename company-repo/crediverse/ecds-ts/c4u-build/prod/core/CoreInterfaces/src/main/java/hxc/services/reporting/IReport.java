package hxc.services.reporting;

import java.io.OutputStream;

public interface IReport
{
	public abstract String getName();

	public abstract ReportParameters getDefaultParameters();

	public abstract void downloadPdf(OutputStream outputStream, ReportParameters parameters);

	public abstract void downloadExcel(OutputStream outputStream, ReportParameters parameters);

	public abstract String getHtml(ReportParameters parameters);
}
