package org.thingsboard.server.common.msg.tools;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.local.LocalBucket;
import io.github.bucket4j.local.LocalBucketBuilder;

import java.time.Duration;

/**
 * Created by ashvayka on 22.10.18.
 */
public class TbRateLimits {
    private final LocalBucket bucket;

    public TbRateLimits(String limitsConfiguration) {
        LocalBucketBuilder builder = Bucket4j.builder();
        boolean initialized = false;
        for (String limitSrc : limitsConfiguration.split(",")) {
            long capacity = Long.parseLong(limitSrc.split(":")[0]);
            long duration = Long.parseLong(limitSrc.split(":")[1]);
            builder.addLimit(Bandwidth.simple(capacity, Duration.ofSeconds(duration)));
            initialized = true;
        }
        if (initialized) {
            bucket = builder.build();
        } else {
            throw new IllegalArgumentException("Failed to parse rate limits configuration: " + limitsConfiguration);
        }


    }

    public boolean tryConsume() {
        return bucket.tryConsume(1);
    }

}
