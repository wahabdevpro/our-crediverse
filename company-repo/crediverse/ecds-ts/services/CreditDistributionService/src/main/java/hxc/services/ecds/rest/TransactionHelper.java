package hxc.services.ecds.rest;

import static hxc.services.ecds.model.TransactionExtraData.Key.DEDICATED_ACCOUNT_REVERSE_INFO;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import hxc.connectors.air.AirException;
import hxc.connectors.air.DedicatedAccountNotFoundException;
import hxc.connectors.air.proxy.AccountUpdate;
import hxc.connectors.air.proxy.Subscriber;
import hxc.ecds.protocol.rest.config.ReversalsConfig;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.IMasterData;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransactionExtraData;
import hxc.services.ecds.model.extra.DedicatedAccountRefillInfo;
import hxc.services.ecds.model.extra.DedicatedAccountRefillInfoAccounts;
import hxc.services.ecds.model.extra.DedicatedAccountReversals;
import hxc.services.ecds.model.extra.DedicatedAccountReverseInfo;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.transactions.ITransaction;
import hxc.services.transactions.Reversal;
import hxc.utils.protocol.ucip.DedicatedAccountInformation;
import hxc.utils.protocol.ucip.DedicatedAccountRefillInformation;
import hxc.utils.protocol.ucip.RefillResponse;
import hxc.utils.protocol.ucip.UpdateBalanceAndDateResponseMember;

public class TransactionHelper {

    /**
     * For any detached or managed entity set LastTime and for any detached entity call merge.
     * Call RequiresTransaction.commit after that.
     * Be aware that if commit is already called for the given trans, RequiresTransaction.commit will do nothing! 
     * @param entities One or more entities in detached or managed state.
     * @throws RuleCheckException Thrown from persist.
     */
    public static void updateInDb(EntityManager em, RequiresTransaction trans, IMasterData...entities) throws RuleCheckException {
        for (IMasterData entity : entities) {
            entity.setLastTime(new Date());
            if (!em.contains(entity)) {
                em.merge(entity);
            }
        }
        trans.commit();
    }

    /**
     * Get account by calling Account.findByAgentID with 'forUpdate' = true.
     * If the account is null RuntimeException is thrown.
     * @return The account with the given accountId.
     */
    public static Account findAccount(EntityManager em, int accountId) {
        Account account = Account.findByAgentID(em, accountId, true);
        if (account == null) {
            throw new RuntimeException("Cannot find account with ID: " + accountId);
        }
        return account;
    }

    /**
     * Checks whether collection is null or contains no elements. 
     * @param collection Collection to be checked.
     * @return True when collection is null or empty.
     */
    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks whether the string is null, empty or contains only spaces. 
     * @param s String to be checked.
     * @return True when string is null, empty or contains only spaces.
     */
    public static boolean isEmptyString(String s) {
        return s == null || s.replaceAll("\\s+", "").isEmpty();
    }

    /**
     * Populate A balance after, A bonus after and A on-hold balance after in the given transaction.
     * @param transaction Transaction to which to set the values.
     * @param aAccount The A account.
     */
    public static void setTransactionAAfter(Transaction transaction, Account aAccount) {
        if (aAccount != null) {
            transaction
                    .setA_BalanceAfter(aAccount.getBalance())
                    .setA_BonusBalanceAfter(aAccount.getBonusBalance())
                    .setA_OnHoldBalanceAfter(aAccount.getOnHoldBalance());
        }
    }

    /**
     * Populate A balance before, A bonus before and A on-hold balance before in the given transaction.
     * @param transaction Transaction to which to set the values.
     * @param aAccount The A account.
     */
    public static void setTransactionABefore(Transaction transaction, Account aAccount) {
        if (aAccount != null) {
            transaction
                    .setA_BalanceBefore(aAccount.getBalance())
                    .setA_BonusBalanceBefore(aAccount.getBonusBalance())
                    .setA_OnHoldBalanceBefore(aAccount.getOnHoldBalance());
        }
    }

    /**
     * Populate B balance before and B bonus before in the given transaction.
     * @param transaction Transaction to which to set the values.
     * @param bAccount The B account.
     */
    public static void setTransactionBBefore(Transaction transaction, Account bAccount) {
        if (bAccount != null) {
            transaction
                    .setB_BalanceBefore(bAccount.getBalance())
                    .setB_BonusBalanceBefore(bAccount.getBonusBalance());
        }
    }

    /**
     * Populate B balance after, B bonus after and B on-hold balance after in the given transaction.
     * @param transaction Transaction to which values are set.
     * @param bAccount The B account from where we read the values.
     */
    public static void setTransactionBAfter(Transaction transaction, Account bAccount) {
        if (bAccount != null) {
            transaction
                    .setB_BalanceAfter(bAccount.getBalance())
                    .setB_BonusBalanceAfter(bAccount.getBonusBalance());
        }
    }

	/**
	 * Create a list with DA accounts and amount information.
	 * @param result from the AIR.
	 * @return List with DA information
	 */
    public static DedicatedAccountRefillInfoAccounts createDaRefillInfoList(RefillResponse result) {
        Sell.logger.debug("result.member.refillInformation {}", result.member.refillInformation);
        DedicatedAccountRefillInfoAccounts dARefillInfoList = null;
        
        if (result.member.refillInformation != null && result.member.refillInformation.refillValueTotal != null
                && result.member.refillInformation.refillValueTotal.dedicatedAccountRefillInformation != null) {

            Sell.logger.debug("result.member.refillInformation.refillValueTotal{}", result.member.refillInformation.refillValueTotal);
            DedicatedAccountRefillInformation[] dedicatedAccountRefillInformation = result.member.refillInformation.refillValueTotal.dedicatedAccountRefillInformation;
            dARefillInfoList = new DedicatedAccountRefillInfoAccounts();

            for (DedicatedAccountRefillInformation accountRefillInformation : dedicatedAccountRefillInformation) {
                Sell.logger.debug("dedicated accountRefillInformation {}", accountRefillInformation);
                Sell.logger.debug("dedicated accountRefillInformation.dedicatedAccountID {}", accountRefillInformation.dedicatedAccountID);
                Sell.logger.debug("dedicated accountRefillInformation.refillAmount1 {}", accountRefillInformation.refillAmount1);
                Sell.logger.debug("dedicated accountRefillInformation.dedicatedAccountUnitType {}", accountRefillInformation.dedicatedAccountUnitType);
                DedicatedAccountRefillInfo dedicatedAccountRefillInfoToSave = new DedicatedAccountRefillInfo();
                dedicatedAccountRefillInfoToSave.setDedicatedAccountID(Integer.valueOf(accountRefillInformation.dedicatedAccountID).toString());
                dedicatedAccountRefillInfoToSave.setDedicatedAccountUnitType(accountRefillInformation.dedicatedAccountUnitType.toString());
                dedicatedAccountRefillInfoToSave.setRefillAmount1(BigDecimal.valueOf(accountRefillInformation.refillAmount1));
                dARefillInfoList.addDedicatedAccountRefillInfos(dedicatedAccountRefillInfoToSave);
            }
        }
        return dARefillInfoList;
    }

    static ITransaction defineAirTransaction(Transaction transaction) {
        return new ITransaction() {
            @Override
            public Date getStartTime() {
                return transaction.getStartTime();
            }

            @Override
            public String getTransactionID() {
                return transaction.getNumber();
            }

            @Override
            public int getResultCode() {
                return 0;
            }

            @Override
            public void setResultCode(int resultCode) {
                transaction.setLastExternalResultCode(Integer.toString(resultCode));
            }

            @Override
            public void addReversal(Reversal reversal) {

            }

            @Override
            public void setLastNotification(String message) {
                transaction.setAdditionalInformation(message);
            }
        };
    }

    static void reverseDedicatedAccounts(EntityManager em, ReversalsConfig config, List<String> externalDataList, Account bAccount,
                                         Transaction transaction, Transaction original, Subscriber subscriber) throws AirException, DedicatedAccountNotFoundException {
        // If there are DA's associated with the transaction reverse the DA amounts
        if (config.isEnableDedicatedAccountReversal() != null && config.isEnableDedicatedAccountReversal()) {

            // We want to find out if there were any bonuses applied to direct accounts
            // Fetch the dedicated account info saved when a sale or transfer takes place
            TransactionExtraData extraDataDedicatedAccountRefillInfo = TransactionExtraData.findByTransactionIdAndKey(em, original.getId(), TransactionExtraData.Key.DEDICATED_ACCOUNT_REFILL_INFO);

            DedicatedAccountReversals postTransactionDAReversals = new DedicatedAccountReversals();

            DedicatedAccountRefillInfoAccounts savedDedicatedAccountRefillInfoAccounts = null;
            if (extraDataDedicatedAccountRefillInfo != null) {
                try {
                    savedDedicatedAccountRefillInfoAccounts = (DedicatedAccountRefillInfoAccounts) extraDataDedicatedAccountRefillInfo.getValueObject();
                } catch (IOException | ClassNotFoundException e) {
                    Reverse.logger.error("Could not deserialize Json for extradata {} for transaction ID: {}", extraDataDedicatedAccountRefillInfo, original.getId());
                }
            }

            if (extraDataDedicatedAccountRefillInfo != null) {
                // list of accounts to to updated and reverse the amount stored/remaining
                AccountUpdate[] dedicatedAccounts = null;

                if (savedDedicatedAccountRefillInfoAccounts != null) {
                    dedicatedAccounts = new AccountUpdate[savedDedicatedAccountRefillInfoAccounts.getDedicatedAccountRefillInfos().size()];
                    int count = 0;
                    for (DedicatedAccountRefillInfo savedDedicatedAccountRefillInfo : savedDedicatedAccountRefillInfoAccounts.getDedicatedAccountRefillInfos()) {
                        DedicatedAccountInformation dedicatedAccountInfo = subscriber.getDedicatedAccount(Integer.valueOf(savedDedicatedAccountRefillInfo.getDedicatedAccountID()));

                        Long daAmountToReverse = null;

                        //check that the DA recorded in the DB exists
                        if (dedicatedAccountInfo == null) {
                            throw new DedicatedAccountNotFoundException(savedDedicatedAccountRefillInfo.getDedicatedAccountID());
                        }
                        // we use dedicatedAccountValue1 and not active value, in case the user has used some of the original amount but has other credit,
                        // then we can reverse the full amount
                        Long currentDedicatedAccountBalance = dedicatedAccountInfo.dedicatedAccountValue1;
                        //check if the balance is the same or more than the amount to reverse
                        if (currentDedicatedAccountBalance.compareTo(savedDedicatedAccountRefillInfo.getRefillAmount1().longValue()) >= 0) {
                            // we have enough balance for the full reversal
                            daAmountToReverse = savedDedicatedAccountRefillInfo.getRefillAmount1().longValue();
                        } else {
                            // We do not have enough balance so reverse whatever is left
                            daAmountToReverse = dedicatedAccountInfo.dedicatedAccountValue1;
                        }

                        dedicatedAccounts[count++] = new AccountUpdate(dedicatedAccountInfo.dedicatedAccountID,
                                                                       dedicatedAccountInfo.dedicatedAccountUnitType,
                                                                       -daAmountToReverse, null, null, null, null, null);

                        // capture the details of the DA accounts that were reversed
                        DedicatedAccountReverseInfo dedicatedAccountReverseInfo = new DedicatedAccountReverseInfo();
                        dedicatedAccountReverseInfo.setDedicatedAccountID(Integer.valueOf(dedicatedAccountInfo.dedicatedAccountID).toString());
                        dedicatedAccountReverseInfo.setDedicatedAccountUnitType(dedicatedAccountInfo.dedicatedAccountUnitType == null ? "" : dedicatedAccountInfo.dedicatedAccountUnitType.toString());
                        dedicatedAccountReverseInfo.setReversalAmount(BigDecimal.valueOf(daAmountToReverse));
                        postTransactionDAReversals.addDedicatedAccountReversal(dedicatedAccountReverseInfo);
                    }

                    subscriber.updateAccounts(0L, externalDataList, dedicatedAccounts);
                    transaction.addExtraDataForKeyType(DEDICATED_ACCOUNT_REVERSE_INFO, postTransactionDAReversals);
                } else {
                    // This else block was missing in the PartialReversal before 2020-Nov-11. If it causes problems the easiest fix is
                    // to duplicate this method and remove the else statement or provide a boolean parameter whether to execute the following
                    // statement or not.
                    // no saved da info or it could not be deserialized
                    subscriber.updateAccounts(0L, externalDataList);
                }
            }
            transaction.persistExtraData(em);
        }
    }

    static UpdateBalanceAndDateResponseMember reverseMainAndDedicatedAccounts(EntityManager em, ReversalsConfig config, List<String> externalDataList,
                                                                              Transaction transaction, Transaction original, Subscriber subscriber,
                                                                              Long maAmountToReverse) throws DedicatedAccountNotFoundException, AirException {
        UpdateBalanceAndDateResponseMember result;
        if (config.isEnableDedicatedAccountReversal() != null && config.isEnableDedicatedAccountReversal()) {

            // We want to find out if there were any bonuses applied to direct accounts
            // Fetch the dedicated account info saved when a sale or transfer takes place
            TransactionExtraData extraDataDedicatedAccountRefillInfo = TransactionExtraData.findByTransactionIdAndKey(em, original.getId(), TransactionExtraData.Key.DEDICATED_ACCOUNT_REFILL_INFO);
            DedicatedAccountRefillInfoAccounts savedDedicatedAccountRefillInfoAccounts = null;

            if (extraDataDedicatedAccountRefillInfo != null) {
                try {
                    savedDedicatedAccountRefillInfoAccounts = (DedicatedAccountRefillInfoAccounts) extraDataDedicatedAccountRefillInfo.getValueObject();
                } catch (IOException | ClassNotFoundException e) {
                    Reverse.logger.error("Could not deserialize Json for extradata {} for transaction ID: {}", extraDataDedicatedAccountRefillInfo, original.getId());
                }
            }

            DedicatedAccountReversals postTransactionDAReversals = new DedicatedAccountReversals();

            if (savedDedicatedAccountRefillInfoAccounts != null) {
                // list of accounts to to updated and reverse the amount stored/remaining
                AccountUpdate[] dedicatedAccounts = new AccountUpdate[savedDedicatedAccountRefillInfoAccounts.getDedicatedAccountRefillInfos().size()];
                int count = 0;
                for (DedicatedAccountRefillInfo savedDedicatedAccountRefillInfo : savedDedicatedAccountRefillInfoAccounts.getDedicatedAccountRefillInfos()) {
                    DedicatedAccountInformation dedicatedAccountInfo = subscriber.getDedicatedAccount(Integer.valueOf(savedDedicatedAccountRefillInfo.getDedicatedAccountID()));

                    Long daAmountToReverse = null;

                    //check that the DA recorded in the DB exists
                    if (dedicatedAccountInfo == null) {
                        throw new DedicatedAccountNotFoundException(savedDedicatedAccountRefillInfo.getDedicatedAccountID());
                    }
                    // we use dedicatedAccountValue1 and not active value, in case the user has used some of the original amount but has other credit,
                    // then we can reverse the full amount
                    Long currentDedicatedAccountBalance = dedicatedAccountInfo.dedicatedAccountValue1;
                    //check if the balance is the same or more than the amount to reverse
                    if (currentDedicatedAccountBalance.compareTo(savedDedicatedAccountRefillInfo.getRefillAmount1().longValue()) >= 0) {
                        // we have enough balance for the full reversal
                        daAmountToReverse = savedDedicatedAccountRefillInfo.getRefillAmount1().longValue();
                    } else {
                        // We do not have enough balance so reverse whatever is left
                        daAmountToReverse = dedicatedAccountInfo.dedicatedAccountValue1;
                    }

                    dedicatedAccounts[count++] = new AccountUpdate(dedicatedAccountInfo.dedicatedAccountID,
                                                                   dedicatedAccountInfo.dedicatedAccountUnitType,
                                                                   -daAmountToReverse, null, null, null, null, null);

                    // capture the details of the DA accounts that were reversed
                    DedicatedAccountReverseInfo dedicatedAccountReverseInfo = new DedicatedAccountReverseInfo();
                    dedicatedAccountReverseInfo.setDedicatedAccountID(Integer.valueOf(dedicatedAccountInfo.dedicatedAccountID).toString());
                    dedicatedAccountReverseInfo.setDedicatedAccountUnitType(dedicatedAccountInfo.dedicatedAccountUnitType == null ? "" : dedicatedAccountInfo.dedicatedAccountUnitType.toString());
                    dedicatedAccountReverseInfo.setReversalAmount(BigDecimal.valueOf(daAmountToReverse));
                    postTransactionDAReversals.addDedicatedAccountReversal(dedicatedAccountReverseInfo);
                }

                result = subscriber.updateAccounts(-maAmountToReverse, externalDataList, dedicatedAccounts);
                transaction.addExtraDataForKeyType(DEDICATED_ACCOUNT_REVERSE_INFO, postTransactionDAReversals);
            } else {
                // no da info associated with transaction
                result = subscriber.updateAccounts(-maAmountToReverse, externalDataList);
            }
            transaction.persistExtraData(em);
        } else {
            // NO Dedicated account to reverse or config to reverse DA's not enabled
            result = subscriber.updateAccounts(-maAmountToReverse, externalDataList);
        }
        return result;
    }
}
