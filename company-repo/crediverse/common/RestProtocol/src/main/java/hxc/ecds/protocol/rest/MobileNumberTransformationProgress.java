package hxc.ecds.protocol.rest;

import static java.lang.Math.round;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MobileNumberTransformationProgress extends TransactionServerResponse {
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

    protected long goodNumbers;
    protected long wrongNumbers;
    protected long processed;
    protected boolean successfullyFinished;
    protected boolean running;
    protected boolean manuallyStopped;
    protected Date lastUpdated;

    public long getGoodNumbers() {
        return goodNumbers;
    }

    public void setGoodNumbers(long goodNumbers) {
        this.goodNumbers = goodNumbers;
    }

    public long getWrongNumbers() {
        return wrongNumbers;
    }

    public void setWrongNumbers(long wrongNumbers) {
        this.wrongNumbers = wrongNumbers;
    }

    public long getProcessed() {
        return processed;
    }

    public void setProcessed(long processed) {
        this.processed = processed;
    }

    public boolean isSuccessfullyFinished() {
        return successfullyFinished;
    }

    public void setSuccessfullyFinished(boolean successfullyFinished) {
        this.successfullyFinished = successfullyFinished;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isManuallyStopped() {
        return manuallyStopped;
    }

    public void setManuallyStopped(boolean manuallyStopped) {
        this.manuallyStopped = manuallyStopped;
    }

    @Override
    public String toString() {
        String lastUpdatedString = lastUpdated != null ? dateFormat.format(lastUpdated) + ": " : "";
        String errorString = isError() ? ". ERROR: " + getMessage() : "";
        lastUpdatedString += isError() ? " ### ERROR ### " : "";
        String percentage = "";
        if (goodNumbers + wrongNumbers > 0) {
            percentage = (round(processed / (float)goodNumbers * 100)) + "% ";
        }
        return lastUpdatedString +
                percentage +
                "processed: " + processed +
                ", good numbers: " + goodNumbers +
                ", wrong numbers: " + wrongNumbers +
                ", running: " + running +
                ", successfully finished: " + successfullyFinished +
                ", manually stopped: " + manuallyStopped +
                errorString;
    }
}
