package hxc.services.ecds.util;

import java.math.BigInteger;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.services.ecds.rest.ICreditDistribution;

public enum MobileNumberDbTransformer {
    TRANSFORMER;

    final static Logger logger = LoggerFactory.getLogger(MobileNumberDbTransformer.class);
    public static final int STEP = 1000;

    private boolean error;
    private boolean running;
    private long wrongNumbers;
    private long processed;
    private long goodNumbers;
    private boolean successfullyFinished;
    private Date lastUpdated;
    private String message;
    private boolean toStop;
    private boolean manuallyStopped;

    public synchronized boolean isRunning() {
        return running;
    }

    /* We don't care about the correctness of the other fields because they are only for roughly showing the progress.
       But the running is important because we should not be able to start the transformation twice.
       Thus its getter is synchronized and its setter is private and synchronized. */
    private synchronized void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Start the transformation in the database in a separate thread.
     *
     * @param em              Entity Manager
     * @param context         ICreditDistribution context
     * @param oldNumberLength old number length
     */
    public void start(EntityManager em, ICreditDistribution context, int oldNumberLength) throws MobileNumberFormatException {
        error = false;
        goodNumbers = 0;
        wrongNumbers = 0;
        processed = 0;
        successfullyFinished = false;
        message = "";
        toStop = false;
        manuallyStopped = false;

        if (isRunning()) {
            error = true;
            message = "Already running. Use 'progress' endpoint to see the progress.";
            throw new MobileNumberFormatException(message);
        }

        try (RequiresTransaction trans = new RequiresTransaction(em)) {
            Query query = em.createNamedQuery("Agent.getGoodCount");
            query.setParameter("len", oldNumberLength);
            goodNumbers = ((BigInteger) query.getSingleResult()).longValue();
            if (goodNumbers == 0) {
                message = "Cannot find any agent in the database! Please check the old code - new prefix mapping. Expected number length: "
                        + oldNumberLength;
                error = true;
                setRunning(false);
                lastUpdated = new Date();
                throw new MobileNumberFormatException(message);
            }

            query = em.createNamedQuery("Agent.getWrongNumbersCount");
            query.setParameter("len", oldNumberLength);
            wrongNumbers = ((BigInteger) query.getSingleResult()).longValue();
            trans.commit();
        }

        lastUpdated = new Date();
        setRunning(true);
        (new Thread(new Transformation(context.getEntityManager(), oldNumberLength))).start();
    }

    private class Transformation implements Runnable {
        private final EntityManager em;
        private final int oldNumberLength;

        public Transformation(EntityManager em, int oldNumberLength) {
            this.em = em;
            this.oldNumberLength = oldNumberLength;
        }

        @Override
        public void run() {
            try {
                List<Integer> ids;
                try (RequiresTransaction trans = new RequiresTransaction(em)) {
                    Query query = em.createNamedQuery("Agent.getAllGoodIds");
                    query.setParameter("len", oldNumberLength);
                    ids = query.getResultList();
                    trans.commit();
                }

                for (int i = 0; i < ids.size(); i += STEP) {
                    if (toStop) {
                        manuallyStopped = true;
                        break;
                    }
                    int toIndex = i + STEP - 1 < ids.size() ? i + STEP - 1 : ids.size() - 1;
                    try (RequiresTransaction trans = new RequiresTransaction(em)) {
                        Query query = em.createNamedQuery("Agent.transformNumbers");
                        query.setParameter("len", oldNumberLength);
                        query.setParameter("id_start", ids.get(i));
                        query.setParameter("id_end", ids.get(toIndex));
                        query.executeUpdate();
                        trans.commit();
                    } catch (PersistenceException ex) {
                        if (ex.getCause() instanceof ConstraintViolationException
                                && ex.getCause().getCause() instanceof SQLIntegrityConstraintViolationException) {
                            logger.warn("TRANSFORMER: " + ex.getCause().getCause().getMessage());
                        } else {
                            error = true;
                            message = ex.getMessage();
                            logger.error("TRANSFORMER: Error during updating numbers", ex);
                        }
                    }
                    processed += toIndex - i + 1;
                    lastUpdated = new Date();
                }

                lastUpdated = new Date();
                if (!manuallyStopped) {
                    successfullyFinished = true;
                }
            } finally {
                setRunning(false);
                if (em != null) {
                    em.close();
                }
            }
        }
    }

    public void stop() {
        toStop = true;
    }

    public boolean isError() {
        return error;
    }

    public long getGoodNumbers() {
        return goodNumbers;
    }

    public long getWrongNumbers() {
        return wrongNumbers;
    }

    public long getProcessed() {
        return processed;
    }

    public boolean isSuccessfullyFinished() {
        return successfullyFinished;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public String getMessage() {
        return message;
    }

    public boolean isManuallyStopped() {
        return manuallyStopped;
    }
}
