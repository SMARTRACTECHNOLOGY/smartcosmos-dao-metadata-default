package net.smartcosmos.dao.metadata.util;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.UUID;

public class SearchSpecifications<T>
{
    //
    // The public methods, which return specifications
    //
    public Specification<T> matchUuid(UUID uuid, String fieldName) {
        return (root, criteriaQuery, criteriaBuilder) ->
            uuidMatches(root, criteriaQuery, criteriaBuilder, uuid, fieldName);
    }

    public Specification<T> stringMatchesExactly(String matchesExactly, String queryParameter) {
        return (root, criteriaQuery, criteriaBuilder) ->
            stringMatchesExactly(root, criteriaQuery, criteriaBuilder, matchesExactly, queryParameter);
    }

    //
    // The private methods, which return Predicates
    //
    private Predicate uuidMatches(Root<T> root, CriteriaQuery<?> query,
                                  CriteriaBuilder builder, UUID matches, String queryParameter) {
        return builder.equal(root.get(queryParameter), matches);
    }

    private Predicate stringMatchesExactly(Root<T> root, CriteriaQuery<?> query,
                                           CriteriaBuilder builder, String matchesExactly, String queryParameter) {
        return builder.equal(root.get(queryParameter), matchesExactly);
    }
}
