package org.thingsboard.server.kafka;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaFuture;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by ashvayka on 24.09.18.
 */
public class TBKafkaAdmin {

    AdminClient client;

    public TBKafkaAdmin(TbKafkaSettings settings) {
        client = AdminClient.create(settings.toProps());
    }

    public void waitForTopic(String topic, long timeout, TimeUnit timeoutUnit) throws InterruptedException, TimeoutException {
        synchronized (this) {
            long timeoutExpiredMs = System.currentTimeMillis() + timeoutUnit.toMillis(timeout);
            while (!topicExists(topic)) {
                long waitMs = timeoutExpiredMs - System.currentTimeMillis();
                if (waitMs <= 0) {
                    throw new TimeoutException("Timeout occurred while waiting for topic [" + topic + "] to be available!");
                } else {
                    wait(1000);
                }
            }
        }
    }

    public CreateTopicsResult createTopic(NewTopic topic){
        return client.createTopics(Collections.singletonList(topic));
    }

    private boolean topicExists(String topic) throws InterruptedException {
        KafkaFuture<TopicDescription> topicDescriptionFuture = client.describeTopics(Collections.singleton(topic)).values().get(topic);
        try {
            topicDescriptionFuture.get();
            return true;
        } catch (ExecutionException e) {
            return false;
        }
    }

}
