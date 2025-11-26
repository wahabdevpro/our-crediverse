package hxc.services.reporting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.database.IDatabase;
import hxc.servicebus.ILocale;
import hxc.servicebus.IServiceBus;
import hxc.services.IService;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.utils.calendar.DateRange;
import hxc.utils.calendar.DateRange.Periods;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.reflection.ClassInfo;
import hxc.utils.reflection.FieldInfo;
import hxc.utils.reflection.ReflectionHelper;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterConfiguration;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

// iReport Designer (Recommended): http://community.jaspersoft.com/project/ireport-designer/releases
// Jaspersoft Studio: http://community.jaspersoft.com/project/jaspersoft-studio/releases

public class ReportingService implements IService, IReportingService
{
	final static Logger logger = LoggerFactory.getLogger(ReportingService.class);

	private IServiceBus esb;
	private IDatabase database;
	private List<IReport> reports;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IService Implementation
	//
	// /////////////////////////////////
	@Override
	public void initialise(IServiceBus esb)
	{
		this.esb = esb;
	}

	@Override
	public boolean start(String[] args)
	{
		// Get the database
		database = esb.getFirstConnector(IDatabase.class);
		if (database == null)
			return false;

		// Initialise the reports list
		reports = new ArrayList<IReport>();

		logger.info("Reporting Service Started");

		return true;
	}

	@Override
	public void stop()
	{

	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (ReportingConfiguration) config;
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		return true;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(category = "ReportingService", description = "View Reporting Service", name = "ViewReportingService", supplier = true),
			@Perm(category = "ReportingService", description = "Change Reporting Service", name = "ChangeReportingService", implies = "ViewReportingService", supplier = true) })
	class ReportingConfiguration extends ConfigurationBase
	{

		@Override
		public String getPath(String languageCode)
		{
			return "Technical Settings";
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return 4264284769147844209L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "Reporting Service";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

	}

	private ReportingConfiguration config = new ReportingConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IReportingService Implementation
	//
	// /////////////////////////////////
	@Override
	public IReport[] getReports()
	{
		// Return the reports list array
		logger.info("Retrieving reports [{}]", reports.size());
		return reports.toArray(new IReport[reports.size()]);
	}

	@Override
	public void addReport(final IReportDefinition<?> report)
	{
		logger.info("Adding {} to Reporting Service.", report.getName());
		Report r = new Report(report)
		{
			@Override
			public void downloadPdf(OutputStream outputStream, ReportParameters parameters)
			{
				logger.debug("Downloading pdf version of {}", getName());

				// Validate the parameters if provided
				if (parameters != null)
				{
					try
					{
						parameters.validate();

						// Generate the pdf with the parameters
						generatePdf(outputStream, report, parameters);
						return;
					}
					catch (ValidationException exc)
					{

					}
				}

				// Else use the default parameters
				generatePdf(outputStream, report, getDefaultParameters());
			}

			@Override
			public void downloadExcel(OutputStream outputStream, ReportParameters parameters)
			{
				logger.debug("Downloading excel version of {}", getName());

				// Validate the parameters if provided
				if (parameters != null)
				{
					try
					{
						parameters.validate();

						// Generate the excel with the parameters
						generateExcel(outputStream, report, parameters);
						return;
					}
					catch (ValidationException exc)
					{
					}
				}

				// Else use the default parameters
				generateExcel(outputStream, report, getDefaultParameters());
			}

			@Override
			public String getHtml(ReportParameters parameters)
			{
				logger.debug("Getting html version of {}", getName());

				// Validate the parameters if provided
				if (parameters != null)
				{
					try
					{
						parameters.validate();

						// Generate the html with the parameters
						return generateHtml(report, parameters);
					}
					catch (ValidationException exc)
					{
					}
				}

				// Else use the default parameters
				return generateHtml(report, getDefaultParameters());
			}
		};

		// Add the report to the reports list
		reports.add(r);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	// Generates a PDF to the Output Stream
	private void generatePdf(OutputStream outputStream, IReportDefinition<?> def, ReportParameters parameters)
	{
		try
		{
			// Fill the report with the parameters
			JasperPrint print = fillReport(def, parameters, false);

			// Ensure it is not null
			if (print == null)
				throw new NullPointerException(String.format("Could not fill %s.", def.getName()));

			// Create the exporter
			JRPdfExporter exporter = new JRPdfExporter();

			// Set the streams
			logger.trace("Setting the exporter streams for the Pdf.");
			exporter.setExporterInput(new SimpleExporterInput(print));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

			// Configure the pdf document
			logger.trace("Configuring the exporter.");
			SimplePdfReportConfiguration configuration = new SimplePdfReportConfiguration();

			// Set the configuration
			exporter.setConfiguration(configuration);

			// Export the report
			logger.info("Exporting {} to Pdf (.pdf)", def.getName());
			exporter.exportReport();
		}
		catch (Exception e)
		{
			logger.error("Failed to generatePdf", e);
		}
	}

	// Generates an Excel Spreadsheet to the Output Stream
	private void generateExcel(OutputStream outputStream, IReportDefinition<?> def, ReportParameters parameters)
	{
		try
		{
			// Fill the report with the parameters
			JasperPrint print = fillReport(def, parameters, true);

			// Ensure it is not null
			if (print == null)
				throw new NullPointerException(String.format("Could not fill %s.", def.getName()));

			// Create the exporter
			JRXlsExporter exporter = new JRXlsExporter();

			// Set the streams
			logger.trace("Setting the exporter streams for the Excel Spreadsheet.");
			exporter.setExporterInput(new SimpleExporterInput(print));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

			// Configure the spreadsheet
			logger.trace("Configuring the exporter.");
			SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
			configuration.setOnePagePerSheet(false);
			configuration.setDetectCellType(true);
			configuration.setRemoveEmptySpaceBetweenRows(true);

			// Set the configuration
			exporter.setConfiguration(configuration);

			// Export the report
			logger.info("Exporting {} to Excel (.xls)", def.getName());
			exporter.exportReport();
		}
		catch (Exception e)
		{
			logger.error("generateExcel failed", e);
		}
	}

	// Generate the HTML
	private String generateHtml(IReportDefinition<?> def, ReportParameters parameters)
	{
		try
		{
			// Fill the report with the parameters
			JasperPrint print = fillReport(def, parameters, true);

			// Ensure it is not null
			if (print == null)
				throw new NullPointerException(String.format("Could not fill %s.", def.getName()));

			// Create the exporter
			HtmlExporter exporter = new HtmlExporter();

			// Set the streams
			logger.trace("Setting the exporter streams for the HTML Document.");
			exporter.setExporterInput(new SimpleExporterInput(print));
			StringBuffer buffer = new StringBuffer();
			exporter.setExporterOutput(new SimpleHtmlExporterOutput(buffer));

			// Configure the spreadsheet
			logger.trace("Configuring the exporter.");
			SimpleHtmlExporterConfiguration configuration = new SimpleHtmlExporterConfiguration();

			// Set the configuration
			exporter.setConfiguration(configuration);

			// Export the report
			logger.info("Exporting %s to HTML (.html)", def.getName());
			exporter.exportReport();

			// Return the HTML
			return buffer.toString();
		}
		catch (Exception e)
		{
			logger.error("generateHtml failed", e);
		}
		return null;
	}

	// Fill the report with the parameters
	private JasperPrint fillReport(IReportDefinition<?> def, ReportParameters parameters, boolean ignorePagination)
	{
		// Get the database connection
		try
		{
			// Check the template for the report exists
			File file = new File(String.format("%s/reports/%s", esb.getBaseDirectory(), def.getTemplate()));
			if (!file.exists())
			{
				throw new FileNotFoundException(String.format("%s does not exist in %s.", def.getTemplate(), esb.getBaseDirectory()));
			}

			// Fill the report
			logger.debug("Filling the report with the report definition and parameter.");
			return JasperFillManager.fillReport(file.getAbsolutePath(), //
					generateParametersMap(def, parameters, ignorePagination), //
					new JRBeanCollectionDataSource(def.getReportData(parameters)));
		}
		catch (Exception e)
		{
			logger.error("fillReport failed", e);
			return null;
		}
	}

	// Generates the parameters map to fill the report
	private Map<String, Object> generateParametersMap(IReportDefinition<?> def, ReportParameters parameters, boolean ignorePagination)
	{
		// Initialise the map
		Map<String, Object> parameterMap = new HashMap<String, Object>();

		// Default parameters
		parameterMap.put(REPORT_NAME_VARIABLE, def.getName());

		// Insert the locale parameters into the map
		ILocale locale = esb.getLocale();
		parameterMap.put(REPORT_DATE_FORMAT_VARIABLE, locale.getDateFormat(locale.getDefaultLanguageID()));
		parameterMap.put(REPORT_CURRENCY_VARIABLE, locale.getCurrencyCode());
		parameterMap.put(REPORT_CURRENCY_DECIMAL_DIGITS, locale.getCurrencyDecimalDigits());

		// Ignore the pagination according to the parameter provided
		parameterMap.put(JRParameter.IS_IGNORE_PAGINATION, ignorePagination);

		// Check if parameters has been provided
		if (parameters != null)
		{
			// Reflect through the parameters class
			ClassInfo classInfo = ReflectionHelper.getClassInfo(parameters.getClass());
			LinkedHashMap<String, FieldInfo> fields = classInfo.getFields();

			// Iterate through the fields
			for (String fieldName : fields.keySet())
			{
				// Check the type of the field
				FieldInfo info = fields.get(fieldName);
				if (info.getType() == Periods.class)
				{
					// Get the period from the date ranges
					DateRange ranges[] = DateRange.GetAllRanges();

					// Get the period from the instance
					Periods period = null;
					try
					{
						period = (Periods) info.get(parameters);
					}
					catch (IllegalArgumentException | IllegalAccessException e)
					{
						continue;
					}

					// Compare against the date ranges to find the matching period
					for (DateRange range : ranges)
					{
						if (range.getPeriod() == period)
						{
							parameterMap.put(REPORT_START_DATE_VARIABLE, range.getStartDate());
							parameterMap.put(REPORT_STOP_DATE_VARIABLE, range.getEndDateInclusive());
						}
					}
				}
				// Else if it is a date range
				else if (info.getType() == DateRange.class)
				{
					// Get the date range from the instance
					DateRange range = null;
					try
					{
						range = (DateRange) info.get(parameters);
					}
					catch (IllegalArgumentException | IllegalAccessException e)
					{
						continue;
					}

					// Put the parameters into the map
					parameterMap.put(REPORT_START_DATE_VARIABLE, range.getStartDate());
					parameterMap.put(REPORT_STOP_DATE_VARIABLE, range.getEndDateInclusive());
				}

				try
				{
					// Put the field name and value into the map
					parameterMap.put(fieldName, fields.get(fieldName).get(parameters));
				}
				catch (IllegalArgumentException | IllegalAccessException e)
				{
				}
			}
		}

		return parameterMap;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	private static final String REPORT_NAME_VARIABLE = "name";
	private static final String REPORT_DATE_FORMAT_VARIABLE = "dateFormat";
	private static final String REPORT_START_DATE_VARIABLE = "startDate";
	private static final String REPORT_STOP_DATE_VARIABLE = "stopDate";
	private static final String REPORT_CURRENCY_VARIABLE = "currency";
	private static final String REPORT_CURRENCY_DECIMAL_DIGITS = "currencyDecimalDigits";

}
