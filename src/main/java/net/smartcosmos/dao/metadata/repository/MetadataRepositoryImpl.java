package net.smartcosmos.dao.metadata.repository;

import java.util.ArrayList;
import java.util.HashMap;
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

        CriteriaQuery<MetadataOwnerEntity> query = getMetadataOwnerCriteriaQuery(tenantId, keyValuePairs, pageable);
        List<MetadataOwnerEntity> result = getResults(pageable, query);

        if (result.size() > 0 && result.size() < pageable.getPageSize()) {
            pageable = new PageRequest(pageable.getPageNumber(), result.size(), pageable.getSort());
        }

        long totalElements = getResultCount(tenantId, keyValuePairs);

        return new PageImpl<>(result, pageable, totalElements);
    }

    private Long getResultCount(UUID tenantId, Map<String, Object> keyValuePairs) {

        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<MetadataEntity> entityRoot = countQuery.from(MetadataEntity.class);

        countQuery.select(builder.countDistinct(entityRoot.get(OWNER_FIELD_NAME)))
            .where(getKeyValuePredicates(countQuery, entityRoot, tenantId, keyValuePairs));

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<MetadataOwnerEntity> getResults(Pageable pageable, CriteriaQuery<MetadataOwnerEntity> resultQuery) {

        TypedQuery<MetadataOwnerEntity> q = entityManager.createQuery(resultQuery);

        q.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        q.setMaxResults(pageable.getPageSize());

        return q.getResultList();
    }

    private CriteriaQuery<MetadataOwnerEntity> getMetadataOwnerCriteriaQuery(UUID tenantId, Map<String, Object> keyValuePairs, Pageable pageable) {

        // region SQL Statement
        /*
            The generated SQL (actually HQL):

            select distinct generatedAlias0.owner
            from net.smartcosmos.dao.metadata.domain.MetadataEntity as generatedAlias0
            where generatedAlias0.owner in (
                select distinct generatedAlias1.owner
                from net.smartcosmos.dao.metadata.domain.MetadataEntity as generatedAlias1
                where ( ( generatedAlias1.keyName=:param0 )
                and ( generatedAlias1.dataType=:param1 )
                and ( generatedAlias1.value=:param2 ) )
                and ( generatedAlias0.owner in (
                    select distinct generatedAlias2.owner
                    from net.smartcosmos.dao.metadata.domain.MetadataEntity as generatedAlias2
                    where ( ( generatedAlias2.keyName=:param3 )
                    and ( generatedAlias2.dataType=:param4 )
                    and ( generatedAlias2.value=:param5 ) )
                    and ( generatedAlias1.owner in (
                        select distinct generatedAlias3.owner
                        from net.smartcosmos.dao.metadata.domain.MetadataEntity as generatedAlias3
                        where ( ( generatedAlias3.keyName=:param6 )
                        and ( generatedAlias3.dataType=:param7 )
                        and ( generatedAlias3.value=:param8 ) )
                        and ( generatedAlias2.owner in (
                            select distinct generatedAlias4.owner
                            from net.smartcosmos.dao.metadata.domain.MetadataEntity as generatedAlias4
                            where ( ( generatedAlias4.keyName=:param9 )
                            and ( generatedAlias4.dataType=:param10 )
                            and ( generatedAlias4.value=:param11 ) )
                            and ( generatedAlias3.owner in (
                                select distinct generatedAlias5.owner
                                from net.smartcosmos.dao.metadata.domain.MetadataEntity as generatedAlias5
                                where ( generatedAlias0.owner.tenantId=:param12 )
                                and ( generatedAlias0.keyName in (:param13, :param14, :param15, :param16)
                            ))
                        ))
                    ))
                ))
            ))
            order by generatedAlias0.owner.id asc

         */
        // endregion

        CriteriaQuery<MetadataOwnerEntity> criteriaQuery = builder.createQuery(MetadataOwnerEntity.class);
        Root<MetadataEntity> root = criteriaQuery.from(MetadataEntity.class);

        criteriaQuery.select(root.get(OWNER_FIELD_NAME))
            .distinct(true)
            .where(getKeyValuePredicates(criteriaQuery, root, tenantId, keyValuePairs))
            .orderBy(getOrder(root, pageable.getSort()));

        return criteriaQuery;
    }

    private Predicate getKeyValuePredicates(CriteriaQuery<?> criteriaQuery, Root<MetadataEntity> root, UUID tenantId, Map<String,
        Object> keyValuePairs) {

        Path<MetadataEntity> tenantIdPath = root.get(OWNER_FIELD_NAME).get(TENANT_ID_FIELD_NAME);
        Path<MetadataEntity> keyNamePath = root.get(KEY_NAME_FIELD_NAME);

        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.putAll(keyValuePairs);

        Predicate tenantPredicate = builder.equal(tenantIdPath, tenantId);
        Predicate keyPredicate = keyNamePath.in(keyValuePairs.keySet());
        Predicate rootPredicate = builder.and(tenantPredicate, keyPredicate);

        Subquery<MetadataEntity> keyValueQuery = getRecursiveSubQueries(criteriaQuery.subquery(MetadataEntity.class), root, metadataMap, rootPredicate);

        return builder.in(root.get(OWNER_FIELD_NAME)).value(keyValueQuery);
    }

    private Subquery<MetadataEntity> getRecursiveSubQueries(Subquery<MetadataEntity> query, Root<MetadataEntity> root, Map<String, Object> keyValuePairs,
                                                            Predicate rootPredicate) {

        Root<MetadataEntity> subRoot = query.from(MetadataEntity.class);

        Predicate predicate;
        if (MapUtils.isNotEmpty(keyValuePairs)) {

            String key = keyValuePairs.keySet().iterator().next();
            Object value = keyValuePairs.remove(key);

            Predicate keyValuePredicate = getKeyValuePredicate(key, value, subRoot);
            Predicate subQueryPredicate = builder.in(root.get(OWNER_FIELD_NAME)).value(getRecursiveSubQueries(query.subquery(MetadataEntity.class), subRoot,
                keyValuePairs, rootPredicate));

            predicate = builder.and(keyValuePredicate, subQueryPredicate);
        } else {
            predicate = rootPredicate;
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
