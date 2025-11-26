package hxc.services.ecds.model;

import java.math.BigDecimal;

public interface IAntiLaunder<T>
{
	public abstract BigDecimal getMaxTransactionAmount();

	public abstract T setMaxTransactionAmount(BigDecimal maxTransactionAmount);

	public abstract Integer getMaxDailyCount();

	public abstract T setMaxDailyCount(Integer maxDailyCount);

	public abstract BigDecimal getMaxDailyAmount();

	public abstract T setMaxDailyAmount(BigDecimal maxDailyAmount);

	public abstract Integer getMaxMonthlyCount();

	public abstract T setMaxMonthlyCount(Integer maxMonthlyCount);

	public abstract BigDecimal getMaxMonthlyAmount();

	public abstract T setMaxMonthlyAmount(BigDecimal maxMonthlyAmount);
}
