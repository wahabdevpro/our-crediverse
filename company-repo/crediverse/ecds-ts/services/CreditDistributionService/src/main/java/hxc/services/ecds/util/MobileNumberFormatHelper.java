package hxc.services.ecds.util;

import static hxc.ecds.protocol.rest.MobileNumberFormatConfig.Phase.AFTER;
import static hxc.ecds.protocol.rest.MobileNumberFormatConfig.Phase.BEFORE;
import static hxc.ecds.protocol.rest.MobileNumberFormatConfig.Phase.DUAL_PHASE;
import static hxc.ecds.protocol.rest.MobileNumberFormatConfig.Phase.MAINTENANCE_WINDOW;
import static hxc.ecds.protocol.rest.config.IConfirmationMenuConfig.RECIPIENT_MSISDN_CONFIRMED;
import static hxc.ecds.protocol.rest.config.SalesConfig.RECIPIENT_MSISDN;
import static hxc.ecds.protocol.rest.config.TransactionsConfig.ERR_WRONG_B_NUMBER_FORMAT;
import static hxc.services.ecds.util.MobileNumberDbTransformer.TRANSFORMER;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.MobileNumberTransformationProgress;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.MobileNumberFormatConfig;
import hxc.services.ecds.model.MobileNumberFormatMapping;
import hxc.services.ecds.rest.ICreditDistribution;

public enum MobileNumberFormatHelper {
    MOBILE_NUMBER_FORMAT_HELPER;

    final static Logger logger = LoggerFactory.getLogger(MobileNumberFormatHelper.class);

    private Map<String, String> oldCodeNewPrefixMapping;
    private hxc.ecds.protocol.rest.MobileNumberFormatConfig config;

    public Map<String, String> getCachedOldCodeNewPrefixMapping(EntityManager em) {
        if (oldCodeNewPrefixMapping == null) {
            loadMapping(em);
        }
        return oldCodeNewPrefixMapping;
    }

    public hxc.ecds.protocol.rest.MobileNumberFormatConfig getCachedConfig(EntityManager em) {
        if (config == null) {
            config = loadConfig(em);
        }
        return config;
    }

    private void cacheMapping(List<MobileNumberFormatMapping> dbMapping) {
        oldCodeNewPrefixMapping = new HashMap<>();
        for (MobileNumberFormatMapping dbEntry : dbMapping) {
            oldCodeNewPrefixMapping.put(dbEntry.getOldCode(), dbEntry.getNewPrefix());
        }
    }

    private void setConfig(hxc.ecds.protocol.rest.MobileNumberFormatConfig config) {
        this.config = config;
    }

    public static hxc.ecds.protocol.rest.MobileNumberFormatMapping loadMapping(EntityManager em) {
        TypedQuery<MobileNumberFormatMapping> query = em.createNamedQuery("MobileNumberFormatMapping.findAll", MobileNumberFormatMapping.class);
        try (RequiresTransaction trans = new RequiresTransaction(em)) {
            List<MobileNumberFormatMapping> dbMapping = query.getResultList();
            trans.commit();
            hxc.ecds.protocol.rest.MobileNumberFormatMapping mapping = new hxc.ecds.protocol.rest.MobileNumberFormatMapping();
            for (MobileNumberFormatMapping dbEntry : dbMapping) {
                if (!mapping.getMapping().containsKey(dbEntry.getNewPrefix())) {
                    mapping.getMapping().put(dbEntry.getNewPrefix(), new HashSet<>());
                }
                mapping.getMapping().get(dbEntry.getNewPrefix()).add(dbEntry.getOldCode());
            }
            MOBILE_NUMBER_FORMAT_HELPER.cacheMapping(dbMapping);
            return mapping;
        }
    }

    public static hxc.ecds.protocol.rest.MobileNumberFormatMapping saveMapping(EntityManager em,
                                                                               hxc.ecds.protocol.rest.MobileNumberFormatMapping mapping) {
        try (RequiresTransaction trans = new RequiresTransaction(em)) {
            em.createNamedQuery("MobileNumberFormatMapping.deleteAll").executeUpdate();
            for (Map.Entry<String, Set<String>> entry : mapping.getMapping().entrySet()) {
                for (String oldCode : entry.getValue()) {
                    em.merge(new MobileNumberFormatMapping(oldCode, entry.getKey()));
                }
            }
            trans.commit();
        }
        return loadMapping(em);
    }

    public static hxc.ecds.protocol.rest.MobileNumberFormatConfig loadConfig(EntityManager em) {
        try (RequiresTransaction trans = new RequiresTransaction(em)) {
            MobileNumberFormatConfig configInDatabase = getConfigFromDatabase(em);
            trans.commit();
            MOBILE_NUMBER_FORMAT_HELPER.setConfig(configInDatabase);
            return configInDatabase;
        }
    }

    private static MobileNumberFormatConfig getConfigFromDatabase(EntityManager em) {
        if (!em.isJoinedToTransaction()) {
            throw new RuntimeException("Not joined to transaction! Please use 'loadConfig' instead of 'getConfigFromDatabase'.");
        }
    
        MobileNumberFormatConfig config;
        TypedQuery<MobileNumberFormatConfig> query = em.createNamedQuery("MobileNumberFormatConfig.find", MobileNumberFormatConfig.class);
        try {
            config = query.getSingleResult();
        } catch (NoResultException ex) {
            config = new MobileNumberFormatConfig();
            em.persist(config);
        }
        return config;
    }

    public static hxc.ecds.protocol.rest.MobileNumberFormatConfig saveConfig(EntityManager em,
                                                                             hxc.ecds.protocol.rest.MobileNumberFormatConfig config) {
        try (RequiresTransaction trans = new RequiresTransaction(em)) {
            MobileNumberFormatConfig configInDatabase = getConfigFromDatabase(em);
            configInDatabase.setOldNumberLength(config.getOldNumberLength());
            configInDatabase.setWrongBNumberMessageEn(config.getWrongBNumberMessageEn());
            configInDatabase.setWrongBNumberMessageFr(config.getWrongBNumberMessageFr());
            em.persist(configInDatabase);
            trans.commit();
            MOBILE_NUMBER_FORMAT_HELPER.setConfig(configInDatabase);
            return configInDatabase;
        }
    }

    public static MobileNumberTransformationProgress getProgress() {
        MobileNumberTransformationProgress progress = new MobileNumberTransformationProgress();
        progress.setError(TRANSFORMER.isError());
        progress.setMessage(TRANSFORMER.getMessage());
        progress.setLastUpdated(TRANSFORMER.getLastUpdated());
        progress.setSuccessfullyFinished(TRANSFORMER.isSuccessfullyFinished());
        progress.setProcessed(TRANSFORMER.getProcessed());
        progress.setGoodNumbers(TRANSFORMER.getGoodNumbers());
        progress.setWrongNumbers(TRANSFORMER.getWrongNumbers());
        progress.setRunning(TRANSFORMER.isRunning());
        progress.setManuallyStopped(TRANSFORMER.isManuallyStopped());
        return progress;
    }

    public static void stopTransformationInDatabase() {
        TRANSFORMER.stop();
    }

    public static void transformNumbersInDatabase(ICreditDistribution context) throws MobileNumberFormatException {
        try (EntityManagerEx em = context.getEntityManager()) {
            MobileNumberTransformationProgress progress = getProgress();
            if (progress.getProcessed() > 0) {
                logger.warn("Transformation has been run before! Current values are: " + progress);
            }

            hxc.ecds.protocol.rest.MobileNumberFormatConfig config = loadConfig(em);
            checkPreconditions(em, config);

            if (config.getPhase() != MAINTENANCE_WINDOW) {
                config.setPhase(MAINTENANCE_WINDOW);
            }

            TRANSFORMER.start(em, context, config.getOldNumberLength());
        }
    }

    private static void checkPreconditions(EntityManager em,
                                           hxc.ecds.protocol.rest.MobileNumberFormatConfig config) throws MobileNumberFormatException {
        if (config.getOldNumberLength() == null || config.getOldNumberLength() < 1) {
            throw new MobileNumberFormatException("Old number length is missing in the DB. Please save the configuration containing this value.");
        }

        if (config.getPhase() != BEFORE && config.getPhase() != MAINTENANCE_WINDOW) {
            throw new MobileNumberFormatException(
                    "To start the database transformation the current phase must be BEFORE or MAINTENANCE_WINDOW but it is: "
                            + config.getPhase().name());
        }

        hxc.ecds.protocol.rest.MobileNumberFormatMapping mapping = loadMapping(em);
        if (mapping.getMapping() == null || mapping.getMapping().isEmpty()) {
            throw new MobileNumberFormatException(
                    "Old codes to new prefix mapping is missing in the DB. Please save the configuration containing this value.");
        }
    }

    public static void enableDualPhase(EntityManager em, boolean force) throws MobileNumberFormatException {
        if (TRANSFORMER.isRunning()) {
            throw new MobileNumberFormatException("The mobile numbers transformation is still running.");
        }

        hxc.ecds.protocol.rest.MobileNumberFormatConfig config = loadConfig(em);
        if (config.getPhase() == BEFORE) {
            throw new MobileNumberFormatException("To enable Dual phase you have to run the mobile numbers transformation first.");
        }

        if (config.getPhase() == DUAL_PHASE) {
            throw new MobileNumberFormatException("Dual phase is already enabled.");
        }

        if (config.getPhase() == AFTER) {
            throw new MobileNumberFormatException("Dual phase is already disabled.");
        }

        if (!force) {
            checkNumbersInDatabase(em, config);
        }

        config.setPhase(DUAL_PHASE);
        try (RequiresTransaction trans = new RequiresTransaction(em)) {
            em.persist(config);
            trans.commit();
        }
    }

    public static void disableDualPhase(EntityManager em, boolean force) throws MobileNumberFormatException {
        if (TRANSFORMER.isRunning()) {
            throw new MobileNumberFormatException("The mobile numbers transformation is still running.");
        }

        hxc.ecds.protocol.rest.MobileNumberFormatConfig config = loadConfig(em);
        if (config.getPhase() == BEFORE || config.getPhase() == MAINTENANCE_WINDOW) {
            throw new MobileNumberFormatException("To disable Dual phase you have to enable it first.");
        }

        if (config.getPhase() == AFTER) {
            throw new MobileNumberFormatException("Dual phase is already disabled.");
        }

        if (!force) {
            checkNumbersInDatabase(em, config);
        }

        config.setPhase(AFTER);
        try (RequiresTransaction trans = new RequiresTransaction(em)) {
            em.persist(config);
            trans.commit();
        }
    }

    private static void checkNumbersInDatabase(EntityManager em, hxc.ecds.protocol.rest.MobileNumberFormatConfig config) throws MobileNumberFormatException {
        try (RequiresTransaction trans = new RequiresTransaction(em)) {
            Query query = em.createNamedQuery("Agent.getNLengthCount");
            query.setParameter("len", config.getOldNumberLength());
            long notTransformedNumbers = ((BigInteger) query.getSingleResult()).longValue();
            trans.commit();
            if (notTransformedNumbers != 0) {
                throw new MobileNumberFormatException(
                        "There are still mobile numbers with length " + config.getOldNumberLength() + " in the database.");
            } else {
                query = em.createNamedQuery("Agent.getWrongPrefixCount");
                long numbersWithWrongPrefix = ((BigInteger) query.getSingleResult()).longValue();
                if (numbersWithWrongPrefix != 0) {
                    throw new MobileNumberFormatException("There are still mobile numbers which starts with wrong prefix.");
                }
            }
        }
    }

    public static String convertBNumber(Map<String, String> values, EntityManager em) {
        String error = convertNumber(values, em, RECIPIENT_MSISDN);
        if (error != null) {
            return error;
        }
        
        error = convertNumber(values, em, RECIPIENT_MSISDN_CONFIRMED);
        
        return error;
    }
    
    private static String convertNumber(Map<String, String> values, EntityManager em, String propertyName) {
        if (values.containsKey(propertyName)) {
            String number = values.get(propertyName);
            hxc.ecds.protocol.rest.MobileNumberFormatConfig config = MOBILE_NUMBER_FORMAT_HELPER.getCachedConfig(em);

            if (number != null && number.length() == config.getOldNumberLength()) {
                if (config.getPhase() == DUAL_PHASE) {
                    String oldCode = number.substring(0, 2);
                    Map<String, String> mapping = MOBILE_NUMBER_FORMAT_HELPER.getCachedOldCodeNewPrefixMapping(em);
                    if (mapping.containsKey(oldCode)) {
                        values.put(propertyName, mapping.get(oldCode) + number);
                    }
                } else if (config.getPhase() == AFTER) {
                    return ERR_WRONG_B_NUMBER_FORMAT;
                }
            }
        }
        return null;
    }

    public void initErrorMessages(Session session, EntityManager em) {
        TransactionsConfig transactionsConfig = session.getCompanyInfo().getConfiguration(em, TransactionsConfig.class);
        hxc.ecds.protocol.rest.MobileNumberFormatConfig config = getCachedConfig(em);

        Phrase phrase = Phrase.en(config.getWrongBNumberMessageEn()).fre(config.getWrongBNumberMessageFr());
        transactionsConfig.getErrorMessages().put(ERR_WRONG_B_NUMBER_FORMAT, phrase);
    }
}
