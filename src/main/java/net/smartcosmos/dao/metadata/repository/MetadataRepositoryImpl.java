package net.smartcosmos.dao.metadata.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang.StringUtils;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwner;
import net.smartcosmos.dao.metadata.util.MetadataValueParser;

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

        CriteriaQuery<MetadataOwner> resultQuery = builder.createQuery(MetadataOwner.class);

        Root<MetadataEntity> root = resultQuery.from(MetadataEntity.class);

        resultQuery.multiselect(root.get("ownerType"), root.get("ownerId"), root.get("tenantId"));
        resultQuery.distinct(true);
        resultQuery.orderBy(new OrderImpl(root.get("tenantId"), true), new OrderImpl(root.get("ownerId"), true));
        List<Predicate> predicates = getPredicateList(root, resultQuery, tenantId, keyValuePairs);
        resultQuery.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));

        TypedQuery<MetadataOwner> q = entityManager.createQuery(resultQuery);
        q.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        q.setMaxResults(pageable.getPageSize());

        List<MetadataOwner> result = q.getResultList();

        if (result.size() < pageable.getPageSize()) {
            pageable = new PageRequest(pageable.getPageNumber(), result.size(), pageable.getSort());
        }

        // TODO: Add Sorting


        // This is not really nice, because it gets all results just to count them

        // TODO: Improve Counting

        CriteriaQuery<MetadataOwner> countQuery = builder.createQuery(MetadataOwner.class);
        Root<MetadataEntity> root2 = countQuery.from(MetadataEntity.class);
        countQuery.multiselect(root2.get("ownerType"), root2.get("ownerId"), root2.get("tenantId"));
        countQuery.distinct(true);
        countQuery.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));
        long totalElements = (long) entityManager.createQuery(countQuery).getResultList().size();

        return new PageImpl<>(result, pageable, totalElements);
    }

    private List<Predicate> getPredicateList(Root<MetadataEntity> root, CriteriaQuery<?> ownerQuery, UUID tenantId, Map<String, Object> keyValuePairs) {

        // TODO: Refactor

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(root.get("tenantId"), tenantId));

        for (String key : keyValuePairs.keySet()) {

            Subquery<MetadataEntity> ownerIdQuery = ownerQuery.subquery(MetadataEntity.class);
            Root<MetadataEntity> idRoot = ownerIdQuery.from(MetadataEntity.class);

            Object value = keyValuePairs.get(key);

            Predicate subKeyPredicate = builder.like(builder.upper(idRoot.get("keyName")), StringUtils.upperCase(key));
            Predicate subValuePredicate = builder.like(builder.upper(idRoot.get("value")), StringUtils.upperCase(MetadataValueParser.getValue(value)));
            Predicate subDataTypePredicate = builder.equal(idRoot.get("dataType"), MetadataValueParser.getDataType(value));

            Subquery<MetadataEntity> ownerTypeQuery = ownerIdQuery.subquery(MetadataEntity.class);
            ownerTypeQuery.select(root.get("ownerType"));
            ownerTypeQuery.distinct(true);
            ownerTypeQuery.from(MetadataEntity.class);
            ownerTypeQuery.where(builder.and(subKeyPredicate, subDataTypePredicate, subValuePredicate));

            Predicate subQueryPredicate = builder.in(root.get("ownerType")).value(ownerTypeQuery);


            Predicate subEntryPredicate = builder.and(subKeyPredicate, subDataTypePredicate, subValuePredicate, subQueryPredicate);

            ownerIdQuery.select(idRoot.get("ownerId"));
            ownerIdQuery.from(MetadataEntity.class);
            ownerIdQuery.where(subEntryPredicate);
            ownerIdQuery.distinct(true);

            predicates.add(builder.in(root.get("ownerId")).value(ownerIdQuery));
        }

        return predicates;
    }
}
