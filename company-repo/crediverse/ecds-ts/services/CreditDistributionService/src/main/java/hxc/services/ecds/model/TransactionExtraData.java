package hxc.services.ecds.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hxc.services.ecds.model.extra.DedicatedAccountRefillInfoAccounts;
import hxc.services.ecds.model.extra.DedicatedAccountReversals;


/**
 * An extension of the transaction table to hold addtitonal data related to the transaction
 */
@Table(name = "ec_transact_ex")
@Entity
@NamedQueries({ //

        @NamedQuery(name = "TransactionExtraData.findByTransIdAndKey", query = "SELECT ted FROM TransactionExtraData ted where ted.transactionExtraDataId.transactionId = :transactionId and ted.transactionExtraDataId.key = :key"), //
})
public class TransactionExtraData implements Serializable {

    final static Logger logger = LoggerFactory.getLogger(TransactionExtraData.class);

    //Mapping of the keys to class type
    public enum Key {
        DEDICATED_ACCOUNT_REFILL_INFO(DedicatedAccountRefillInfoAccounts.class.getName()),
        DEDICATED_ACCOUNT_REVERSE_INFO(DedicatedAccountReversals.class.getName());

        Key(String keyClass) {

            this.keyClass = keyClass;
        }


        private String keyClass;

    }

    private static ObjectMapper mapper = new ObjectMapper();

    private TransactionExtraDataId transactionExtraDataId;

    //should be stored as
    private String value;

    // no arg constructor for hibernate
    public TransactionExtraData() {
    }

    public TransactionExtraData(TransactionExtraDataId transactionExtraDataId, String value) {
        this.transactionExtraDataId = transactionExtraDataId;
        this.value = value;
    }


    @EmbeddedId
    public TransactionExtraDataId getTransactionExtraDataId() {
        return transactionExtraDataId;
    }

    public void setTransactionExtraDataId(TransactionExtraDataId transactionExtraDataId) {
        this.transactionExtraDataId = transactionExtraDataId;
    }

    @Column(name = "value", columnDefinition = "longtext", nullable = false)
    public String getValue() {
        return value;
    }


    public void setValue(String value) {
        this.value = value;
    }

    @Transient
    public Object getValueObject() throws IOException, ClassNotFoundException {
        Key key = Key.valueOf(transactionExtraDataId.getKey());

        Object valueOblect = mapper.readValue(value, Class.forName(key.keyClass));

        return valueOblect;
    }

    public static TransactionExtraData findByTransactionIdAndKey(EntityManager em, Long transactionId, Key key) {
        TypedQuery<TransactionExtraData> query = em.createNamedQuery("TransactionExtraData.findByTransIdAndKey", TransactionExtraData.class);
        query.setParameter("transactionId", transactionId);
        query.setParameter("key", key.name());
        List<TransactionExtraData> resultList = query.getResultList();

        // we only ever get a single result but want to return null if no result is found
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Serialize and Deserialize dedicatedAccountRefillInformation
    //
    // /////////////////////////////////


    @Embeddable
    public static class TransactionExtraDataId implements Serializable {
        protected long transactionId;
        protected String key;

        @Column(name = "transaction_id", nullable = false)
        public long getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(long transactionId) {
            this.transactionId = transactionId;
        }

        @Column(name = "key_type", length = 50, nullable = false )
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    public static String toJson(Object object) throws JsonProcessingException {

        // Java object to JSON string
        return mapper.writeValueAsString(object);
    }
}
