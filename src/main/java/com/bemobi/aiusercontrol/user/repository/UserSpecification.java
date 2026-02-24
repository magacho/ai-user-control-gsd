package com.bemobi.aiusercontrol.user.repository;

import com.bemobi.aiusercontrol.enums.UserStatus;
import com.bemobi.aiusercontrol.model.entity.User;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> withFilters(String name, String email, String department, String status) {
        return Specification
                .where(nameContains(name))
                .and(emailContains(email))
                .and(departmentEquals(department))
                .and(statusEquals(status));
    }

    private static Specification<User> nameContains(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private static Specification<User> emailContains(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }

    private static Specification<User> departmentEquals(String department) {
        if (department == null || department.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("department"), department);
    }

    private static Specification<User> statusEquals(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), UserStatus.valueOf(status));
    }
}
