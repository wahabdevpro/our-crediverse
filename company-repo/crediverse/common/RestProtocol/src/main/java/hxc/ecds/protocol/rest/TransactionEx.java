package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

public class TransactionEx extends Transaction
{
	public static final String VIRTUAL_FILTER_MSISDNAB = "virtual_msisdnab";
	public static final String VIRTUAL_FILTER_AGENTIDAB = "virtual_idab";
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected String a_FirstName;
	protected String a_Surname;
	protected String a_TierName;
	protected String a_TierType;
	protected String a_GroupName;
	protected String a_AreaName;
	protected String a_AreaType;
	protected String a_OwnerFirstName;
	protected String a_OwnerSurname;
	protected String a_CellGroupCode;

	protected String b_FirstName;
	protected String b_Surname;
	protected String b_TierName;
	protected String b_TierType;
	protected String b_GroupName;
	protected String b_AreaName;
	protected String b_AreaType;
	protected String b_OwnerFirstName;
	protected String b_OwnerSurname;
	protected String b_CellGroupCode;
	protected List<DedicatedAccountRefillInfo> dedicatedAccountRefillInfos;
	protected Iterator<DedicatedAccountRefillInfo> dedicatedAccountRefillInfoIterator;
	protected List<DedicatedAccountReverseInfo> dedicatedAccountReverseInfo;
	protected List<DedicatedAccountInfo> dedicatedAccountCurrentBalanceInfos;
	protected BigDecimal mainAccountCurrentBalance;

	protected String aCgi;
	protected String bCgi;
	private Boolean balanceAndDateFailed = Boolean.FALSE;
	protected Boolean DABonusReversalEnabled = Boolean.FALSE;
	protected String nonAirtimeItemDescription;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public Boolean isDABonusReversalEnabled() {
		return this.DABonusReversalEnabled;
	}

	public void setDABonusReversalEnabled(Boolean DABonusReversalEnabled) {
		this.DABonusReversalEnabled = DABonusReversalEnabled;
	}

	public Boolean isBalanceAndDateFailed() {
		return this.balanceAndDateFailed;
	}

	public void setBalanceAndDateFailed(Boolean balanceAndDateFailed) {
		this.balanceAndDateFailed = balanceAndDateFailed;
	}

	public String getACgi() {
		return aCgi;
	}

	public void setACgi(String aCgi) {
		this.aCgi = aCgi;
	}
	
	public String getBCgi() {
		return bCgi;
	}

	public void setBCgi(String bCgi) {
		this.bCgi = bCgi;
	}

	public String getA_FirstName()
	{
		return a_FirstName;
	}

	public void setA_FirstName(String a_FirstName)
	{
		this.a_FirstName = a_FirstName;
	}

	public String getA_Surname()
	{
		return a_Surname;
	}

	public void setA_Surname(String a_Surname)
	{
		this.a_Surname = a_Surname;
	}

	public String getA_TierName()
	{
		return a_TierName;
	}

	public void setA_TierName(String a_TierName)
	{
		this.a_TierName = a_TierName;
	}

	public String getA_TierType()
	{
		return a_TierType;
	}

	public void setA_TierType(String a_TierType)
	{
		this.a_TierType = a_TierType;
	}

	public String getA_GroupName()
	{
		return a_GroupName;
	}

	public void setA_GroupName(String a_GroupName)
	{
		this.a_GroupName = a_GroupName;
	}

	public String getA_AreaName()
	{
		return a_AreaName;
	}

	public void setA_AreaName(String a_AreaName)
	{
		this.a_AreaName = a_AreaName;
	}

	public String getA_AreaType()
	{
		return a_AreaType;
	}

	public void setA_AreaType(String a_AreaType)
	{
		this.a_AreaType = a_AreaType;
	}

	public String getB_AreaType()
	{
		return b_AreaType;
	}

	public void setB_AreaType(String b_AreaType)
	{
		this.b_AreaType = b_AreaType;
	}

	public String getA_OwnerFirstName()
	{
		return a_OwnerFirstName;
	}

	public void setA_OwnerFirstName(String a_OwnerFirstName)
	{
		this.a_OwnerFirstName = a_OwnerFirstName;
	}

	public String getA_OwnerSurname()
	{
		return a_OwnerSurname;
	}

	public void setA_OwnerSurname(String a_OwnerSurname)
	{
		this.a_OwnerSurname = a_OwnerSurname;
	}
	
	public String getA_CellGroupCode()
	{
		return a_CellGroupCode;
	}

	public void setA_CellGroupCode(String a_CellGroupCode)
	{
		this.a_CellGroupCode = a_CellGroupCode;
	}


	public String getB_FirstName()
	{
		return b_FirstName;
	}

	public void setB_FirstName(String b_FirstName)
	{
		this.b_FirstName = b_FirstName;
	}

	public String getB_Surname()
	{
		return b_Surname;
	}

	public void setB_Surname(String b_Surname)
	{
		this.b_Surname = b_Surname;
	}

	public String getB_TierName()
	{
		return b_TierName;
	}

	public void setB_TierName(String b_TierName)
	{
		this.b_TierName = b_TierName;
	}

	public String getB_TierType()
	{
		return b_TierType;
	}

	public void setB_TierType(String b_TierType)
	{
		this.b_TierType = b_TierType;
	}

	public String getB_GroupName()
	{
		return b_GroupName;
	}

	public void setB_GroupName(String b_GroupName)
	{
		this.b_GroupName = b_GroupName;
	}

	public String getB_AreaName()
	{
		return b_AreaName;
	}

	public void setB_AreaName(String b_AreaName)
	{
		this.b_AreaName = b_AreaName;
	}

	public String getB_OwnerFirstName()
	{
		return b_OwnerFirstName;
	}

	public void setB_OwnerFirstName(String b_OwnerFirstName)
	{
		this.b_OwnerFirstName = b_OwnerFirstName;
	}

	public String getB_OwnerSurname()
	{
		return b_OwnerSurname;
	}

	public void setB_OwnerSurname(String b_OwnerSurname)
	{
		this.b_OwnerSurname = b_OwnerSurname;
	}

	public String getB_CellGroupCode()
	{
		return b_CellGroupCode;
	}

	public void setB_CellGroupCode(String b_CellGroupCode)
	{
		this.b_CellGroupCode = b_CellGroupCode;
	}

	public String getNonAirtimeItemDescription() {
		return nonAirtimeItemDescription;
	}

	public void setNonAirtimeItemDescription(String nonAirtimeItemDescription) {
		this.nonAirtimeItemDescription = nonAirtimeItemDescription;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public TransactionEx()
	{

	}

	public TransactionEx(Transaction transaction)
	{
		super(transaction);
	}

	public List<DedicatedAccountRefillInfo> getDedicatedAccountRefillInfos() {
		return dedicatedAccountRefillInfos;
	}

	public void setDedicatedAccountRefillInfos(List<DedicatedAccountRefillInfo> dedicatedAccountRefillInfos) {
		this.dedicatedAccountRefillInfos = dedicatedAccountRefillInfos;
	}

	public void resetDedicatedAccountRefillInfosIterator() {

		dedicatedAccountRefillInfoIterator = dedicatedAccountRefillInfos.iterator();
	}

	public DedicatedAccountRefillInfo getNextDedicatedAccountRefillInfos() {
		if(dedicatedAccountRefillInfoIterator == null || !dedicatedAccountRefillInfoIterator.hasNext()) {
			if(dedicatedAccountRefillInfos != null && !dedicatedAccountRefillInfos.isEmpty()){
				dedicatedAccountRefillInfoIterator = dedicatedAccountRefillInfos.iterator();
			}
		}

		return dedicatedAccountRefillInfoIterator !=null && dedicatedAccountRefillInfos != null
				? dedicatedAccountRefillInfoIterator.next() : null;
	}

	public boolean  getHasNextDedicatedAccountRefillInfos() {

		return dedicatedAccountRefillInfoIterator != null && dedicatedAccountRefillInfoIterator.hasNext();
	}

	public void setDedicatedAccountReverseInfo(List<DedicatedAccountReverseInfo> dedicatedAccountReverseInfo) {
		this.dedicatedAccountReverseInfo = dedicatedAccountReverseInfo;
	}

	public List<DedicatedAccountReverseInfo> getDedicatedAccountReverseInfo() {
		return dedicatedAccountReverseInfo;
	}

	public String getDedicatedAccountReverseInfoString() {
		String daReverseDisplay = "Dedicated Account Reversal Info: ";
		if(this.dedicatedAccountReverseInfo != null){

			for (DedicatedAccountReverseInfo accountReverseInfo : this.dedicatedAccountReverseInfo) {
				daReverseDisplay += "[ID: "  + accountReverseInfo.getDedicatedAccountID() + "  Amount: " + accountReverseInfo.getReverseAmount() + "] ---";
			}
		}

		return daReverseDisplay;
	}

	public List<DedicatedAccountInfo> getDedicatedAccountCurrentBalanceInfos() {
		return dedicatedAccountCurrentBalanceInfos;
	}

	public void setDedicatedAccountCurrentBalanceInfos(List<DedicatedAccountInfo> dedicatedAccountCurrentBalanceInfos) {
		this.dedicatedAccountCurrentBalanceInfos = dedicatedAccountCurrentBalanceInfos;
	}

	public BigDecimal getMainAccountCurrentBalance() {
		return mainAccountCurrentBalance;
	}

	public void setMainAccountCurrentBalance(BigDecimal mainAccountCurrentBalance) {
		this.mainAccountCurrentBalance = mainAccountCurrentBalance;
	}
}
