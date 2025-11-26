package hxc.services.ecds.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of the transaction table to hold location information (GPS coordinates) related to the transaction
 */
@Table(name = "ec_transact_location")
@Entity
@NamedQueries({ //
    @NamedQuery(name = "TransactionLocation.findGPSByTransactionID", query = "SELECT tl FROM TransactionLocation tl where tl.transactionId = :transactionId"), //
})
public class TransactionLocation implements Serializable {

    final static Logger logger = LoggerFactory.getLogger(TransactionLocation.class);

    protected long transactionId;
    protected Double latitude;
    protected Double longitude;

    // no arg constructor for hibernate
    public TransactionLocation() {
    }

    public TransactionLocation(long transactionId, Double latitude, Double longitude) {
        this.transactionId = transactionId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Id
    @Column(name = "transaction_id", nullable = false, unique = true)
    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    @Column(name = "longitude", nullable = false)
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Column(name = "latitude", nullable = false)
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public static TransactionLocation findGPSByTransactionId(EntityManager em, Long transactionId) {
        TypedQuery<TransactionLocation> query = em.createNamedQuery("TransactionLocation.findGPSByTransactionID", TransactionLocation.class);
        query.setParameter("transactionId", transactionId);
        List<TransactionLocation> resultList = query.getResultList();

        // we only ever get a single result but want to return null if no result is found
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    public static String formatLocation(TransactionLocation location) {
        if (location == null) {
            return "";
        }

        return String.format("%8f|%8f", location.getLatitude(), location.getLongitude());
    }
}


