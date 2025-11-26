package hxc.services.ecds.model.non_airtime;

import hxc.services.ecds.model.Transaction;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

@Table(name = "non_airtime_transaction_details")
@NamedQueries({
        @NamedQuery(name = "NonAirtimeTransactionDetails.findById", query = "SELECT n FROM NonAirtimeTransactionDetails n where id = :id"),
        
        @NamedQuery(name = "NonAirtimeTransactionDetails.findByClientTransactionId", query = "SELECT n FROM NonAirtimeTransactionDetails n " +
                " where serviceUserId = :serviceUserId AND clientTransactionId = :clientTransactionId"),
                
        @NamedQuery(name = "Transaction.findByClientTransactionId", query = "SELECT t FROM Transaction t " +
                " WHERE t.id = (SELECT n.id FROM NonAirtimeTransactionDetails n " +
                "                WHERE n.serviceUserId = :serviceUserId AND n.clientTransactionId = :clientTransactionId)"),
})
@Entity
public class NonAirtimeTransactionDetails {
    public static final int MAX_ITEM_DESCRIPTION_LENGTH = 512;
    public static final int MAX_CONSUMER_MSISDN_LENGTH = 30;

    private long id;
    private String clientTransactionId;
    private String itemDescription;
    private int serviceUserId;

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "client_transaction_id", nullable = false)
    public String getClientTransactionId() {
        return clientTransactionId;
    }

    public void setClientTransactionId(String clientTransactionId) {
        this.clientTransactionId = clientTransactionId;
    }

    @Column(name = "item_description")
    public String getItemDescription() {
        return itemDescription;
    }

    @Column(name = "service_user_id")
    public int getServiceUserId() {
        return serviceUserId;
    }

    public void setServiceUserId(int serviceUserId) {
        this.serviceUserId = serviceUserId;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public static NonAirtimeTransactionDetails findById(EntityManager em, long id) {
        TypedQuery<NonAirtimeTransactionDetails> query = em.createNamedQuery("NonAirtimeTransactionDetails.findById",
                                                                             NonAirtimeTransactionDetails.class);
        query.setParameter("id", id);
        query.setMaxResults(1);
        return query.getSingleResult();
    }

    public static List<NonAirtimeTransactionDetails> findByUserAndClientTransactionId(EntityManager em, int serviceUserId, String clientTransactionId) {
        TypedQuery<NonAirtimeTransactionDetails> query = em.createNamedQuery("NonAirtimeTransactionDetails.findByClientTransactionId",
                                                                             NonAirtimeTransactionDetails.class);
        query.setParameter("serviceUserId", serviceUserId);
        query.setParameter("clientTransactionId", clientTransactionId);
        return query.getResultList();
    }

    public static Transaction findTransactionByClientTransactionId(EntityManager em, int serviceUserId, String clientTransactionId) {
        TypedQuery<Transaction> query = em.createNamedQuery("Transaction.findByClientTransactionId", Transaction.class);
        query.setParameter("serviceUserId", serviceUserId);
        query.setParameter("clientTransactionId", clientTransactionId);
        List<Transaction> transactions = query.getResultList();
        return transactions.isEmpty() ? null : transactions.get(0);
    }
}
