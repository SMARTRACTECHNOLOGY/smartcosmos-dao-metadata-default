package net.smartcosmos.dao.metadata.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.MapUtils;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwnerEntity;
import net.smartcosmos.dao.metadata.util.MetadataValueParser;

import static net.smartcosmos.dao.metadata.domain.MetadataEntity.DATA_TYPE_FIELD_NAME;
import static net.smartcosmos.dao.metadata.domain.MetadataEntity.KEY_NAME_FIELD_NAME;
import static net.smartcosmos.dao.metadata.domain.MetadataEntity.OWNER_FIELD_NAME;
import static net.smartcosmos.dao.metadata.domain.MetadataEntity.OWNER_ID_FIELD_NAME;
import static net.smartcosmos.dao.metadata.domain.MetadataEntity.TENANT_ID_FIELD_NAME;
import static net.smartcosmos.dao.metadata.domain.MetadataEntity.VALUE_FIELD_NAME;

@Component
public class MetadataRepositoryImpl implements MetadataRepositoryCustom {

    private final EntityManager entityManager;
    private final CriteriaBuilder builder;

    @Autowired
    public MetadataRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;

        builder = entityManager.getCriteriaBuilder();
    }

    @Override
    public Page<MetadataOwnerEntity> findProjectedByTenantIdAndKeyValuePairs(UUID tenantId, Map<String, Object> keyValuePairs, Pageable pageable) {

        CriteriaQuery<MetadataOwnerEntity> resultQuery = getMetadataOwnerCriteriaQuery(tenantId, keyValuePairs, pageable);
        List<MetadataOwnerEntity> result = getMetadataOwners(pageable, resultQuery);

        if (result.size() > 0 && result.size() < pageable.getPageSize()) {
            pageable = new PageRequest(pageable.getPageNumber(), result.size(), pageable.getSort());
        }

        // TODO: Improve Counting
        // This is not really nice, because it gets all results just to count them
        long totalElements = (long) entityManager.createQuery(resultQuery).getResultList().size();

        return new PageImpl<>(result, pageable, totalElements);
    }

    private List<MetadataOwnerEntity> getMetadataOwners(Pageable pageable, CriteriaQuery<MetadataOwnerEntity> resultQuery) {

        TypedQuery<MetadataOwnerEntity> q = entityManager.createQuery(resultQuery);

        q.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        q.setMaxResults(pageable.getPageSize());

        return q.getResultList();
    }

    private CriteriaQuery<MetadataOwnerEntity> getMetadataOwnerCriteriaQuery(UUID tenantId, Map<String, Object> keyValuePairs, Pageable pageable) {

        // region SQL Statement
        /*
            The generated SQL:

            select distinct generatedAlias0.owner
            from net.smartcosmos.dao.metadata.domain.MetadataEntity as generatedAlias0
            where (( generatedAlias0.owner.tenantId=:param0 )
            and ( generatedAlias0.keyName in (:param1, :param2, :param3, :param4) ) )
            and ( generatedAlias0.owner in (
                select distinct generatedAlias1.owner
                from net.smartcosmos.dao.metadata.domain.MetadataEntity as generatedAlias1
                where (( generatedAlias1.keyName=:param5 )
                and ( generatedAlias1.dataType=:param6 )
                and ( generatedAlias1.value=:param7 ) )
                and ( generatedAlias0.owner in (
                    select distinct generatedAlias2.owner
                    from net.smartcosmos.dao.metadata.domain.MetadataEntity as generatedAlias2
                    where (( generatedAlias2.keyName=:param8 )
                    and ( generatedAlias2.dataType=:param9 )
                    and ( generatedAlias2.value=:param10 ) )
                    and ( generatedAlias1.owner in (
                        select distinct generatedAlias3.owner
                        from net.smartcosmos.dao.metadata.domain.MetadataEntity as generatedAlias3
                        where (( generatedAlias3.keyName=:param11 )
                        and ( generatedAlias3.dataType=:param12 )
                        and ( generatedAlias3.value=:param13 ) )
                        and ( generatedAlias2.owner in (
                            select distinct generatedAlias4.owner
                            from net.smartcosmos.dao.metadata.domain.MetadataEntity as generatedAlias4
                            where ( generatedAlias4.keyName=:param14 )
                            and ( generatedAlias4.dataType=:param15 )
                            and ( generatedAlias4.value=:param16 )
                        ) )) )
                ) )) ) order by generatedAlias0.owner.id asc
         */
        // endregion

        CriteriaQuery<MetadataOwnerEntity> criteriaQuery = builder.createQuery(MetadataOwnerEntity.class);
        Root<MetadataEntity> root = criteriaQuery.from(MetadataEntity.class);
        Path<MetadataEntity> tenantIdPath = root.get(OWNER_FIELD_NAME).get(TENANT_ID_FIELD_NAME);
        Path<MetadataEntity> keyNamePath = root.get(KEY_NAME_FIELD_NAME);

        Predicate tenantPredicate = builder.equal(tenantIdPath, tenantId);
        Predicate keyPredicate = keyNamePath.in(keyValuePairs.keySet());

        Predicate rootPredicate = builder.and(tenantPredicate, keyPredicate);

        Subquery<MetadataEntity> keyValueQuery = getSubQuery(criteriaQuery.subquery(MetadataEntity.class), root, keyValuePairs);
        Predicate keyValueSubQueryPredicate = builder.in(root.get(OWNER_FIELD_NAME)).value(keyValueQuery);

        criteriaQuery.select(root.get(OWNER_FIELD_NAME))
            .distinct(true)
            .where(builder.and(rootPredicate, keyValueSubQueryPredicate))
            .orderBy(getOrder(root, pageable.getSort()));

        return criteriaQuery;
    }

    private Subquery<MetadataEntity> getSubQuery(Subquery<MetadataEntity> query, Root<MetadataEntity> root, Map<String, Object> keyValuePairs) {

        Root<MetadataEntity> subRoot = query.from(MetadataEntity.class);

        String key = keyValuePairs.keySet().iterator().next();
        Object value = keyValuePairs.remove(key);

        Predicate keyValuePredicate = getKeyValuePredicate(key, value, subRoot);

        Predicate predicate;
        if (MapUtils.isNotEmpty(keyValuePairs)) {
            Predicate subQueryPredicate = builder.in(root.get(OWNER_FIELD_NAME)).value(getSubQuery(query.subquery(MetadataEntity.class), subRoot, keyValuePairs));
            predicate = builder.and(keyValuePredicate, subQueryPredicate);
        } else {
            predicate = keyValuePredicate;
        }

        return query.select(subRoot.get(OWNER_FIELD_NAME))
            .where(predicate)
            .distinct(true);
    }

    private List<Order> getOrder(Root<MetadataEntity> root, Sort sort) {
        List<Order> orderList = new ArrayList<>();

        if (sort != null && !IteratorUtils.isEmpty(sort.iterator())) {
            Sort.Order order = sort.iterator().next();

            String[] sortFields = order.getProperty().split("\\.");
            Path<MetadataEntity> orderPath = root;
            for (String sortField : sortFields) {
                orderPath = orderPath.get(sortField);
            }
            orderList.add(new OrderImpl(orderPath, order.isAscending()));
        } else {
            orderList.add(new OrderImpl(root.get(TENANT_ID_FIELD_NAME), true));
            orderList.add(new OrderImpl(root.get(OWNER_ID_FIELD_NAME), true));
        }

        return orderList;
    }

    private Predicate getKeyValuePredicate(String key, Object value, From root) {

        Predicate keyNamePredicate = builder.equal(root.get(KEY_NAME_FIELD_NAME), key);
        Predicate valuePredicate = builder.equal(root.get(VALUE_FIELD_NAME), MetadataValueParser.getValue(value));
        Predicate dataTypePredicate = builder.equal(root.get(DATA_TYPE_FIELD_NAME), MetadataValueParser.getDataType(value));

        return builder.and(keyNamePredicate, dataTypePredicate, valuePredicate);
    }
}
