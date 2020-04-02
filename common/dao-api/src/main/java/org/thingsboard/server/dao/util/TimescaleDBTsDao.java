package org.thingsboard.server.dao.util;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(prefix = "database.ts", value = "type", havingValue = "timescale")
public @interface TimescaleDBTsDao {
}
