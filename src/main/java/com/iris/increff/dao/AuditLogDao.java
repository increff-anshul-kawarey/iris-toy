package com.iris.increff.dao;

import com.iris.increff.model.AuditLog;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class AuditLogDao {

    @PersistenceContext
    private EntityManager em;

    public void insert(AuditLog log) {
        em.persist(log);
    }

    public List<AuditLog> selectAll() {
        String jpql = "select p from AuditLog p";
        TypedQuery<AuditLog> query = em.createQuery(jpql, AuditLog.class);
        return query.getResultList();
    }
}
