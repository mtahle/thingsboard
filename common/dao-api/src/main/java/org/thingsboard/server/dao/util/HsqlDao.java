package org.thingsboard.server.dao.util;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(prefix = "spring.jpa", value = "database-platform", havingValue = "org.hibernate.dialect.HSQLDialect")
public @interface HsqlDao {
}