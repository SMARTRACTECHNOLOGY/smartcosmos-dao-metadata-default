package net.smartcosmos.dao.metadata.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
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
import static net.smartcosmos.dao.metadata.domain.MetadataEntity.OWNER_ID_FIELD_NAME;
import static net.smartcosmos.dao.metadata.domain.MetadataEntity.OWNER_TYPE_FIELD_NAME;
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

//        CriteriaQuery<MetadataOwnerEntity> resultQuery = getMetadataOwnerCriteriaQueryJoin(tenantId, keyValuePairs, pageable);
        Query resultQuery = getSqlQuery(tenantId, keyValuePairs, pageable);
//        List<MetadataOwnerEntity> result = getMetadataOwners(pageable, resultQuery);


        List<MetadataOwnerEntity> result = resultQuery.getResultList();



        if (result.size() > 0 && result.size() < pageable.getPageSize()) {
            pageable = new PageRequest(pageable.getPageNumber(), result.size(), pageable.getSort());
        }

        // TODO: Improve Counting
        // This is not really nice, because it gets all results just to count them
//        long totalElements = (long) entityManager.createQuery(resultQuery).getResultList().size();

        return new PageImpl<>(result, pageable, 5);
//        return new PageImpl<>(result, pageable, totalElements);
    }

    private Query getSqlQuery(UUID tenantId, Map<String, Object> keyValuePairs, Pageable pageable) {

        String key = keyValuePairs.keySet().iterator().next();
        Object value = keyValuePairs.remove(key);

        String selectClause = "SELECT o.tenantId, o.type, o.internalId FROM metadata m0 INNER JOIN metadataOwner o ON m0.owner_internalId = o" +
                              ".internalId";
        String whereClause = String.format("WHERE m0.keyName = '%s' AND m0.value = '%s' AND m0.dataType = %d",
            key,
            MetadataValueParser.getValue(value),
            MetadataValueParser.getDataType(value).getId());

        String qlString = new StringBuilder(selectClause).append("\n")
            .append(getInnerJoin(0, keyValuePairs))
            .append(whereClause)
            .toString();

        System.out.println(qlString);

        return entityManager.createNativeQuery(qlString);
    }

    private String getInnerJoin(Integer root, Map<String, Object> keyValuePairs) {

        if (MapUtils.isNotEmpty(keyValuePairs)) {
            Integer newRoot = root + 1;

            String key = keyValuePairs.keySet().iterator().next();
            Object value = keyValuePairs.remove(key);

            String joinClause = String.format("INNER JOIN metadata m%d ON m%d.owner_internalId = m" + newRoot + ".owner_internalId",
                newRoot, root );
            String andClause = String.format("AND m" + newRoot + ".keyName = '%s' AND m" + newRoot + ".value = '%s' AND m" + newRoot + ".dataType = %d",
                key,
                MetadataValueParser.getValue(value),
                MetadataValueParser.getDataType(value).getId());

            return new StringBuilder(joinClause)
                .append("\n")
                .append(andClause)
                .append("\n")
                .append(getInnerJoin(newRoot, keyValuePairs))
                .toString();
        } else {
            return "";
        }
    }

    private List<MetadataOwnerEntity> getMetadataOwners(Pageable pageable, CriteriaQuery<MetadataOwnerEntity> resultQuery) {

        TypedQuery<MetadataOwnerEntity> q = entityManager.createQuery(resultQuery);

        q.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        q.setMaxResults(pageable.getPageSize());

        return q.getResultList();
    }

    private CriteriaQuery<MetadataOwnerEntity> getMetadataOwnerCriteriaQueryJoin(UUID tenantId, Map<String, Object> keyValuePairs, Pageable
        pageable) {

        // region SQL Statement
        /*
            select generatedAlias0
            from net.smartcosmos.dao.metadata.domain.MetadataEntity as generatedAlias1
            inner join generatedAlias1.owner as generatedAlias0 with ( generatedAlias1.owner=generatedAlias1.keyName ) and ( ( generatedAlias1.keyName=:param0 ) and ( generatedAlias1.dataType=:param1 ) and ( generatedAlias1.value=12 ) ) where ( generatedAlias1.keyName=:param2 ) and ( generatedAlias1.dataType=:param3 ) and ( generatedAlias1.value=:param4 ) order by generatedAlias1.created asc]; nested exception is java.lang.IllegalArgumentException: org.hibernate.hql.internal.ast.InvalidWithClauseException: with clause can only reference columns in the driving table [select generatedAlias0 from net.smartcosmos.dao.metadata.domain.MetadataEntity as generatedAlias1 inner join generatedAlias1.owner as generatedAlias0 with ( generatedAlias1.owner=generatedAlias1.keyName ) and ( ( generatedAlias1.keyName=:param0 ) and ( generatedAlias1.dataType=:param1 ) and ( generatedAlias1.value=12 ) ) where ( generatedAlias1.keyName=:param2 ) and ( generatedAlias1.dataType=:param3 ) and ( generatedAlias1.value=:param4 ) order by generatedAlias1.created asc
        */
        // endregion

        CriteriaQuery<MetadataOwnerEntity> query = builder.createQuery(MetadataOwnerEntity.class);
        Root<MetadataEntity> metadata0 = query.from(MetadataEntity.class);


        Join<MetadataEntity, MetadataEntity> join = metadata0.join("owner", JoinType.INNER);

        Join<MetadataEntity, MetadataOwnerEntity> ownerJoin = join.join("internalId", JoinType.INNER);

        String firstKey;
        Object firstValue;
        if (MapUtils.isNotEmpty(keyValuePairs)) {
            firstKey = keyValuePairs.keySet().iterator().next();
            firstValue = keyValuePairs.remove(firstKey);

//            Predicate tenantPredicate = builder.equal(root.get(TENANT_ID_FIELD_NAME), tenantId);
            Predicate keyValuePredicate = getKeyValuePredicate(firstKey, firstValue, metadata0);

            ownerJoin = ownerJoin.on(getJoinPredicates(join, metadata0.get("keyName"), keyValuePairs));

            query.select(ownerJoin)
                .where(builder.and(keyValuePredicate))
                .orderBy(getOrder(metadata0, pageable.getSort()));

            return query;
        }

//        query.select(root.get(OWNER_FIELD_NAME))
//            .distinct(true)
//            .where(builder.and(getPredicatesJoin(root, query, tenantId, keyValuePairs)))
//            .orderBy(getOrder(root, pageable.getSort()));

        return query;
    }

    private Predicate getJoinPredicates(From<MetadataEntity, MetadataEntity> join, Path<Object> owner, Map<String, Object> keyValuePairs) {

        Predicate ownerPredicate = builder.equal(join.get("owner"), owner);

        String key = keyValuePairs.keySet().iterator().next();
        Object value = keyValuePairs.remove(key);

        Predicate keyValuePredicate = getKeyValuePredicate(key, value, join);

        if (MapUtils.isNotEmpty(keyValuePairs)) {
            Join<MetadataEntity, MetadataEntity> subJoin = join.join("owner", JoinType.INNER);
            return builder.and(ownerPredicate, keyValuePredicate, getJoinPredicates(subJoin, join.get("owner"), keyValuePairs));
        }

        return builder.and(ownerPredicate, keyValuePredicate);
    }

    private Predicate[] getPredicatesJoin(
        Root<MetadataEntity> root,
        CriteriaQuery<MetadataOwnerEntity> resultQuery,
        UUID tenantId,
        Map<String, Object> keyValuePairs) {

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(builder.equal(root.get(TENANT_ID_FIELD_NAME), tenantId));


        return predicates.toArray(new Predicate[predicates.size()]);
    }

    private CriteriaQuery<MetadataOwnerEntity> getMetadataOwnerCriteriaQuery(UUID tenantId, Map<String, Object> keyValuePairs, Pageable pageable) {

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

        CriteriaQuery<MetadataOwnerEntity> resultQuery = builder.createQuery(MetadataOwnerEntity.class);
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

    private Predicate getKeyValuePredicate(String key, Object value, From root) {

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
