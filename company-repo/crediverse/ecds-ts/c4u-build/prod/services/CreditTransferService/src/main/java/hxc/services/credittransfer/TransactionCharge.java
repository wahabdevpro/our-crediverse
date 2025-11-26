package hxc.services.credittransfer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import hxc.configuration.Config;
import hxc.configuration.Configurable;
import hxc.configuration.ValidationException;

@Configurable
public class TransactionCharge implements Serializable, Comparable<TransactionCharge>
{
	// /////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////

	private long fixedCharge = 0L;
	private long percentageCharge = 0L;
	private AmountRange amountRange = null;// new AmountRange(0L,0L);
	private int[] applicableServiceClasses;

	// /////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////
	public TransactionCharge()
	{

	}

	public TransactionCharge(long _floor, long _ceil, long fixedCharge, long percentageCharge, int[] serviceClasses)
	{
		this.fixedCharge = fixedCharge;
		this.percentageCharge = percentageCharge;
		this.amountRange = new AmountRange(_floor, _ceil);
		this.applicableServiceClasses = serviceClasses;

	}

	@Config(description = "", hidden = true)
	public AmountRange getAmountRange()
	{
		return this.amountRange;
	}

	@Config(description = "Minimum Range Amount", hidden = true)
	public long getMinRange()
	{
		return this.amountRange.getMinValue();
	}

	public void setMinRange(long minValue) throws ValidationException
	{
		if (this.amountRange == null)
		{
			this.amountRange = new AmountRange(minValue, minValue);
		}

		try
		{
			this.amountRange.setMinValue(minValue);
		}
		catch (ValidationException ve)
		{
			throw ve;
		}
	}

	@Config(description = "Maximum Range Amount", hidden = true)
	public long getMaxRange()
	{
		return this.amountRange.getMaxValue();
	}

	public void setMaxRange(long maxValue) throws ValidationException
	{
		if (this.amountRange == null)
		{
			this.amountRange = new AmountRange(maxValue, maxValue);
		}

		try
		{
			this.amountRange.setMaxValue(maxValue);
		}
		catch (ValidationException ve)
		{
			throw ve;
		}
	}

	// fixedCharge (getter/setter)
	@Config(description = "Fixed Charge Amount", hidden = true)
	public long getFixedCharge()
	{
		return this.fixedCharge;
	}

	public void setFixedCharge(long fCharge) throws ValidationException
	{
		if (fCharge < 0)
			throw new ValidationException("Fixed charge can't be negative");

		this.fixedCharge = fCharge;
	}

	// variableCharge (getter/setter)
	@Config(description = "Percentage Charge Amount", hidden = true)
	public long getPercentageCharge()
	{
		return this.percentageCharge;
	}

	public void setPercentageCharge(long vCharge) throws ValidationException
	{
		if (vCharge < 0)
			throw new ValidationException("Percentage charge can't be negative");

		this.percentageCharge = vCharge;
	}

	public int[] getApplicableServiceClasses()
	{
		return applicableServiceClasses;
	}

	public void setApplicableServiceClasses(int[] applicableServiceClasses) throws ValidationException
	{
		if (checkDuplicates(applicableServiceClasses))
			throw new ValidationException("Duplicate service classes");

		for (int sc : applicableServiceClasses)
		{
			if (sc < 0)
				throw new ValidationException("Negative service classes not allowed");
		}

		this.applicableServiceClasses = applicableServiceClasses;
	}

	@Override
	public int compareTo(TransactionCharge arg0)
	{
		// if (this.fixedCharge + this.percentageCharge < arg0.fixedCharge + arg0.percentageCharge)
		if (this.amountRange.getMinValue() < arg0.amountRange.getMinValue())
			return -1;
		// if (this.fixedCharge + this.percentageCharge > arg0.fixedCharge + arg0.percentageCharge)
		else if (this.amountRange.getMinValue() > arg0.amountRange.getMinValue())
			return 1;

		return 0;
	}

	boolean checkDuplicates(int[] integerArray)
	{
		List<Integer> list = new ArrayList<Integer>();
		for (int i : integerArray)
			if (!list.contains(i))
				list.add(i);
			else
				return true;
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// GUI Properties (Helper properties for pre-scaling values for the GUI)
	//
	// /////////////////////////////////

	// Set and get currency values rounded to 2 decimal places
	// Min Range

	@Config(description = "Minimum Range Amount")
	public String getMinRangeUI()
	{
		return longToString(this.getMinRange(), 2);
	}

	public void setMinRangeUI(String quantity) throws ValidationException
	{
		setMinRange(stringToLong(quantity, 2));
	}

	// Max range

	@Config(description = "Maximum Range Amount")
	public String getMaxRangeUI()
	{
		return longToString(this.getMaxRange(), 2);
	}

	public void setMaxRangeUI(String quantity) throws ValidationException
	{
		setMaxRange(stringToLong(quantity, 2));
	}

	// Fixed Charge

	@Config(description = "Fixed Charge Amount")
	public String getFixedChargeUI()
	{
		return longToString(this.getFixedCharge(), 2);
	}

	public void setFixedChargeUI(String quantity) throws ValidationException
	{
		setFixedCharge(stringToLong(quantity, 2));
	}

	// Percentage Charge

	// Get and set percentages as entered without rounding
	@Config(description = "Percentage Charge Amount", decimalDigitsToDisplay = 2)
	public String getPercentageChargeUI()
	{
		return longToString(this.percentageCharge, 0);
	}

	public void setPercentageChargeUI(String quantity) throws ValidationException
	{
		setPercentageCharge(stringToLong(quantity, 0));
	}

	// Helper methods

	public static String longToString(long longValue, int rounding)
	{
		BigDecimal quantity = new BigDecimal(longValue);
		quantity = quantity.divide(new BigDecimal(CreditTransferBase.getChargeScalingfactor()));
		if (rounding == 0)
			return quantity.toPlainString();
		else
			return quantity.setScale(rounding, RoundingMode.HALF_UP).toPlainString();
	}

	public static long stringToLong(String stringValue, int rounding) throws ValidationException
	{
		long longValue = 0;
		DecimalFormat format = new DecimalFormat();
		format.setParseBigDecimal(true);
		try
		{
			BigDecimal bdValue = (BigDecimal) format.parse(stringValue);
			BigDecimal chargeScalingFactor = new BigDecimal(CreditTransferBase.getChargeScalingfactor());
			if (rounding != 0)
				bdValue = bdValue.setScale(rounding, BigDecimal.ROUND_HALF_UP);
			bdValue = bdValue.multiply(chargeScalingFactor);
			longValue = bdValue.longValue();
		}
		catch (Exception e)
		{
			throw new ValidationException("Invalid input");
		}

		return longValue;
	}

}
