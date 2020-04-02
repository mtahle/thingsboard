package org.thingsboard.server.kafka;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.header.Header;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by ashvayka on 24.09.18.
 */
@Slf4j
public class TBKafkaProducerTemplate<T> {

    private final KafkaProducer<String, byte[]> producer;
    private final TbKafkaEncoder<T> encoder;

    private final TbKafkaPartitioner<T> partitioner;
    private ConcurrentMap<String, List<PartitionInfo>> partitionInfoMap;
    @Getter
    private final String defaultTopic;

    @Getter
    private final TbKafkaSettings settings;

    @Builder
    private TBKafkaProducerTemplate(TbKafkaSettings settings, TbKafkaEncoder<T> encoder,
                                    TbKafkaPartitioner<T> partitioner, String defaultTopic, String clientId) {
        Properties props = settings.toProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        if (!StringUtils.isEmpty(clientId)) {
            props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        }
        this.settings = settings;
        this.producer = new KafkaProducer<>(props);
        this.encoder = encoder;
        this.partitioner = partitioner;
        this.defaultTopic = defaultTopic;
    }

    public void init() {
        this.partitionInfoMap = new ConcurrentHashMap<>();
        if (!StringUtils.isEmpty(defaultTopic)) {
            try {
                TBKafkaAdmin admin = new TBKafkaAdmin(this.settings);
                admin.waitForTopic(defaultTopic, 30, TimeUnit.SECONDS);
                log.info("[{}] Topic exists.", defaultTopic);
            } catch (Exception e) {
                log.info("[{}] Failed to wait for topic: {}", defaultTopic, e.getMessage(), e);
                throw new RuntimeException(e);
            }
            //Maybe this should not be cached, but we don't plan to change size of partitions
            this.partitionInfoMap.putIfAbsent(defaultTopic, producer.partitionsFor(defaultTopic));
        }
    }

    public Future<RecordMetadata> send(String key, T value, Callback callback) {
        return send(key, value, null, callback);
    }

    public Future<RecordMetadata> send(String key, T value, Iterable<Header> headers, Callback callback) {
        return send(key, value, null, headers, callback);
    }

    public Future<RecordMetadata> send(String key, T value, Long timestamp, Iterable<Header> headers, Callback callback) {
        if (!StringUtils.isEmpty(this.defaultTopic)) {
            return send(this.defaultTopic, key, value, timestamp, headers, callback);
        } else {
            throw new RuntimeException("Failed to send message! Default topic is not specified!");
        }
    }

    public Future<RecordMetadata> send(String topic, String key, T value, Iterable<Header> headers, Callback callback) {
        return send(topic, key, value, null, headers, callback);
    }

    public Future<RecordMetadata> send(String topic, String key, T value, Callback callback) {
        return send(topic, key, value, null, null, callback);
    }

    public Future<RecordMetadata> send(String topic, String key, T value, Long timestamp, Iterable<Header> headers, Callback callback) {
        byte[] data = encoder.encode(value);
        ProducerRecord<String, byte[]> record;
        Integer partition = getPartition(topic, key, value, data);
        record = new ProducerRecord<>(topic, partition, timestamp, key, data, headers);
        return producer.send(record, callback);
    }

    private Integer getPartition(String topic, String key, T value, byte[] data) {
        if (partitioner == null) {
            return null;
        } else {
            return partitioner.partition(topic, key, value, data, partitionInfoMap.computeIfAbsent(topic, producer::partitionsFor));
        }
    }
}
