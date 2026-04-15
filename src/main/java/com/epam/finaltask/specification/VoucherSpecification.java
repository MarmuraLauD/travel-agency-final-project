package com.epam.finaltask.specification;

import com.epam.finaltask.model.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class VoucherSpecification {

    public static Specification<Voucher> filterVouchers(
            String title,
            String tourType,
            String transferType,
            String hotelType,
            Boolean hot,
            String status) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), VoucherStatus.valueOf(status.toUpperCase())));
            }

            if (title != null && !title.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%"
                ));
            }

            if (tourType != null && !tourType.isEmpty() && !tourType.equals("ALL")) {
                predicates.add(criteriaBuilder.equal(root.get("tourType"), TourType.valueOf(tourType)));
            }

            if (transferType != null && !transferType.isEmpty() && !transferType.equals("ALL")) {
                predicates.add(criteriaBuilder.equal(root.get("transferType"), TransferType.valueOf(transferType)));
            }

            if (hotelType != null && !hotelType.isEmpty() && !hotelType.equals("ALL")) {
                predicates.add(criteriaBuilder.equal(root.get("hotelType"), HotelType.valueOf(hotelType)));
            }

            if (hot != null && hot) {
                predicates.add(criteriaBuilder.equal(root.get("hot"), true));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
