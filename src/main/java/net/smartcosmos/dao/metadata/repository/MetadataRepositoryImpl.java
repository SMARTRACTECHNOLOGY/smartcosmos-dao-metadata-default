package net.smartcosmos.dao.metadata.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.collections4.IteratorUtils;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwner;
import net.smartcosmos.dao.metadata.util.MetadataValueParser;

import static net.smartcosmos.dao.metadata.domain.MetadataEntity.*;

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
    public Page<MetadataOwner> findProjectedByTenantIdAndKeyValuePairs(UUID tenantId, Map<String, Object> keyValuePairs, Pageable pageable) {

        CriteriaQuery<MetadataOwner> resultQuery = getMetadataOwnerCriteriaQuery(tenantId, keyValuePairs, pageable);
        List<MetadataOwner> result = getMetadataOwners(pageable, resultQuery);

        if (result.size() > 0 && result.size() < pageable.getPageSize()) {
            pageable = new PageRequest(pageable.getPageNumber(), result.size(), pageable.getSort());
        }

        // TODO: Improve Counting
        // This is not really nice, because it gets all results just to count them
        long totalElements = (long) entityManager.createQuery(resultQuery).getResultList().size();

        return new PageImpl<>(result, pageable, totalElements);
    }

    private List<MetadataOwner> getMetadataOwners(Pageable pageable, CriteriaQuery<MetadataOwner> resultQuery) {

        TypedQuery<MetadataOwner> q = entityManager.createQuery(resultQuery);

        q.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        q.setMaxResults(pageable.getPageSize());

        return q.getResultList();
    }

    private CriteriaQuery<MetadataOwner> getMetadataOwnerCriteriaQuery(UUID tenantId, Map<String, Object> keyValuePairs, Pageable pageable) {

        // region SQL Statement
        /*
            The generated SQL:

            select distinct metadataen0_.owner_type as col_0_0_, metadataen0_.owner_id as col_1_0_, metadataen0_.tenant_id as col_2_0_
            from metadata metadataen0_
            where metadataen0_.tenant_id=?
                and (metadataen0_.owner_id in (
                    select distinct metadataen1_.owner_id
                    from metadata metadataen1_ cross join metadata metadataen2_
                    where (upper(metadataen1_.key_name) like ?)
                        and metadataen1_.data_type=?
                        and (upper(metadataen1_.value) like ?)
                        and (metadataen0_.owner_type in (
                            select distinct metadataen0_.owner_type
                            from metadata metadataen3_
                            where (upper(metadataen1_.key_name) like ?)
                                and metadataen1_.data_type=?
                                and (upper(metadataen1_.value) like ?)))
                            )
                        )
                        and (metadataen0_.owner_id in (
                            select distinct metadataen4_.owner_id
                            from metadata metadataen4_ cross join metadata metadataen5_
                            where (upper(metadataen4_.key_name) like ?)
                                and metadataen4_.data_type=?
                                and (upper(metadataen4_.value) like ?)
                                and (metadataen0_.owner_type in (
                                    select distinct metadataen0_.owner_type
                                    from metadata metadataen6_
                                    where (upper(metadataen4_.key_name) like ?)
                                        and metadataen4_.data_type=?
                                        and (upper(metadataen4_.value) like ?))
                                )
                        )
                )
         */
        // endregion

        CriteriaQuery<MetadataOwner> resultQuery = builder.createQuery(MetadataOwner.class);
        Root<MetadataEntity> root = resultQuery.from(MetadataEntity.class);

        resultQuery.multiselect(root.get(OWNER_TYPE_FIELD_NAME), root.get(OWNER_ID_FIELD_NAME), root.get(TENANT_ID_FIELD_NAME))
            .distinct(true)
            .where(builder.and(getPredicates(root, resultQuery, tenantId, keyValuePairs)))
            .orderBy(getOrder(root, pageable.getSort()));

        return resultQuery;
    }

    private List<Order> getOrder(Root<MetadataEntity> root, Sort sort) {
        List<Order> orderList = new ArrayList<>();

        if (sort != null && !IteratorUtils.isEmpty(sort.iterator())) {
            Sort.Order order = sort.iterator().next();
            orderList.add(new OrderImpl(root.get(order.getProperty()), true));
        } else {
            orderList.add(new OrderImpl(root.get(TENANT_ID_FIELD_NAME), true));
            orderList.add(new OrderImpl(root.get(OWNER_ID_FIELD_NAME), true));
        }

        return orderList;
    }

    private Predicate[] getPredicates(Root<MetadataEntity> root, CriteriaQuery<?> ownerQuery, UUID tenantId, Map<String, Object> keyValuePairs) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(builder.equal(root.get(TENANT_ID_FIELD_NAME), tenantId));

        for (String key : keyValuePairs.keySet()) {

            Subquery<MetadataEntity> ownerIdQuery = ownerQuery.subquery(MetadataEntity.class);
            Root<MetadataEntity> idRoot = ownerIdQuery.from(MetadataEntity.class);

            Predicate keyValuePredicate = getKeyValuePredicate(key, keyValuePairs.get(key), idRoot);
            Predicate typeQueryPredicate = getTypeQueryPredicate(ownerIdQuery, root, keyValuePredicate);
            Predicate ownerIdPredicate = getOwnerIdQueryPredicate(ownerIdQuery, root, idRoot, builder.and(keyValuePredicate, typeQueryPredicate));

            predicates.add(ownerIdPredicate);
        }

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    private Predicate getKeyValuePredicate(String key, Object value, Root<MetadataEntity> root) {

        Predicate keyNamePredicate = builder.equal(root.get(KEY_NAME_FIELD_NAME), key);
        Predicate valuePredicate = builder.equal(root.get(VALUE_FIELD_NAME), value);
        Predicate dataTypePredicate = builder.equal(root.get(DATA_TYPE_FIELD_NAME), MetadataValueParser.getDataType(value));

        return builder.and(keyNamePredicate, dataTypePredicate, valuePredicate);
    }

    private Predicate getTypeQueryPredicate(Subquery<MetadataEntity> query, Root<MetadataEntity> root, Predicate keyValuePredicate) {

        Subquery<MetadataEntity> ownerTypeQuery = query.subquery(MetadataEntity.class);

        ownerTypeQuery.select(root.get(OWNER_TYPE_FIELD_NAME))
            .distinct(true)
            .where(keyValuePredicate)
            .from(MetadataEntity.class);

        return builder.in(root.get(OWNER_TYPE_FIELD_NAME)).value(ownerTypeQuery);
    }

    private Predicate getOwnerIdQueryPredicate(Subquery<MetadataEntity> query, Root<MetadataEntity> root, Root<MetadataEntity> subRoot,
                                               Predicate predicate) {

        query.select(subRoot.get(OWNER_ID_FIELD_NAME))
            .distinct(true)
            .where(predicate)
            .from(MetadataEntity.class);

        return builder.in(root.get(OWNER_ID_FIELD_NAME)).value(query);
    }
}
