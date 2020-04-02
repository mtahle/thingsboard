package org.thingsboard.server.dao.util;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

@ConditionalOnExpression("'${database.ts.type}'=='sql' || '${database.ts.type}'=='timescale'")
public @interface SqlTsAnyDao {
}
