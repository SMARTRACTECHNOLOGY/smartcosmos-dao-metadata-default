package net.smartcosmos.dao.metadata.repository;

import net.smartcosmos.dao.metadata.domain.MetadataEntity;
import net.smartcosmos.dao.metadata.domain.MetadataOwner;
import net.smartcosmos.dao.metadata.util.MetadataValueParser;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        CriteriaQuery<MetadataOwner> query = builder.createQuery(MetadataOwner.class);

        Root<MetadataEntity> root = query.from(MetadataEntity.class);

        query.multiselect(root.get("ownerType"), root.get("ownerId"), root.get("tenantId"));
        query.distinct(true);
        List<Predicate> predicates = getPredicateList(root, query, tenantId, keyValuePairs);
        query.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));

        List<MetadataOwner> result = entityManager.createQuery(query).getResultList();

        return new PageImpl<>(result, pageable, 2);
    }

    @SuppressWarnings("Duplicates")
    private List<Predicate> getPredicateList(Root<MetadataEntity> root, CriteriaQuery<?> ownerQuery, UUID tenantId, Map<String, Object> keyValuePairs) {

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
