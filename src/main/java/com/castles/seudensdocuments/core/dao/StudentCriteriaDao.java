package com.castles.seudensdocuments.core.dao;


import com.castles.seudensdocuments.core.model.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.antlr.v4.runtime.misc.Utils.count;

@Repository
@RequiredArgsConstructor
public class StudentCriteriaDao {

    private static final Set<String> SORTABLE = Set.of(
            "id",
            "firstName",
            "lastName",
            "email",
            "enrollmentDate"
    );

    private final EntityManager entityManager;

    public Page<Student> search(String name,
                                String email,
                                LocalDate from,
                                LocalDate to,
                                Pageable pageable){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Student> query = cb.createQuery(Student.class);
        Root<Student> root = query.from(Student.class);
        Predicate[] predicates = buildPredicates(cb, root, name, email, from, to);

        query.select(root).where(predicates).orderBy(toOrders(cb, root, pageable.getSort()));

        TypedQuery<Student> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<Student> content = typedQuery.getResultList();

        long total = count(cb, name, email, from, to);
        return new PageImpl<>(content, pageable, total);

    }

    private long count(CriteriaBuilder cb,
                       String name,
                       String email,
                       LocalDate from,
                       LocalDate to){
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Student> root = countQuery.from(Student.class);
        countQuery.select(cb.count(root)).
                where(buildPredicates(cb, root, name, email, from, to));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb,
                                        Root<Student> root,
                                        String name,
                                        String email,
                                        LocalDate from,
                                        LocalDate to){

        List<Predicate> predicates = new ArrayList<>();

        if(name != null && !name.isBlank()){
            String pattern = "%" + name.trim().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern)
            ));
        }
        if(email != null && !email.isBlank()){
            predicates.add(cb.equal(cb.lower(root.get("email")), email.trim().toLowerCase()));
        }

        if(from != null){
            predicates.add(cb.greaterThanOrEqualTo(root.get("enrollmentDate"), from));
        }
        if(to != null){
            predicates.add(cb.lessThanOrEqualTo(root.get("enrollmentDate"), to));
        }

        return predicates.toArray(Predicate[]::new);
    }

    private List<Order> toOrders(CriteriaBuilder cb,
                                 Root<Student> root,
                                 Sort sort){
        List<Order> orders = new ArrayList<>();


        if(sort == null || sort.isUnsorted()){
            orders.add(cb.asc(root.get("id")));
            return orders;
        }

        for (Sort.Order order : sort){

            //Подменяем name на firstName при сортировке
            String property = "name".equals(order.getProperty()) ? "firstName" : order.getProperty();

            //Проверяем наличие поля в списке подлежащих сортировке
            if (!SORTABLE.contains(property)){
                continue;
            }
            orders.add(order.isAscending() ? cb.asc(root.get(property)) : cb.desc(root.get(property)));
        }

        if(orders.isEmpty()){
            orders.add(cb.asc(root.get("id")));
        }

        return orders;

    }



}
