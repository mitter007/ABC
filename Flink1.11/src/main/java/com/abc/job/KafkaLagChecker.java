package com.abc.job;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class KafkaLagChecker {
    public static void main(String[] args) throws Exception {
        String bootstrapServers = "localhost:9092";
        String groupId = "my-group-id";
        String topicName = "my-topic";

        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try (AdminClient adminClient = AdminClient.create(props)) {
            // 1. 获取该 Group 在该 Topic 上的已提交偏移量 (Current Offset)
            ListConsumerGroupOffsetsOptions options = new ListConsumerGroupOffsetsOptions()
                    .topicPartitions(getTopicPartitions(adminClient, topicName));
            
            Map<TopicPartition, OffsetAndMetadata> committedOffsets = adminClient
                    .listConsumerGroupOffsets(groupId, options)
                    .partitionsToOffsetAndMetadata()
                    .get();

            // 2. 获取该 Topic 各分区的最新偏移量 (Log End Offset)
            Map<TopicPartition, OffsetSpec> offsetSpecs = committedOffsets.keySet().stream()
                    .collect(Collectors.toMap(tp -> tp, tp -> OffsetSpec.latest()));
            
            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> endOffsets = adminClient
                    .listOffsets(offsetSpecs)
                    .all()
                    .get();

            // 3. 计算并打印 Lag
            System.out.println("Topic: " + topicName + " | Group: " + groupId);
            long totalLag = 0;
            for (TopicPartition tp : committedOffsets.keySet()) {
                long currentOffset = committedOffsets.get(tp).offset();
                long endOffset = endOffsets.get(tp).offset();
                long lag = endOffset - currentOffset;
                totalLag += lag;
                System.out.printf("  Partition %d: LEO=%d, Current=%d, Lag=%d%n", 
                        tp.partition(), endOffset, currentOffset, lag);
            }
            System.out.println("Total Lag: " + totalLag);
        }
    }

    // 辅助方法：获取 Topic 的所有分区
    private static List<TopicPartition> getTopicPartitions(AdminClient adminClient, String topic) 
            throws ExecutionException, InterruptedException {
        return adminClient.describeTopics(Collections.singletonList(topic))
                .allTopicNames()
                .get()
                .get(topic)
                .partitions()
                .stream()
                .map(p -> new TopicPartition(topic, p.partition()))
                .collect(Collectors.toList());
    }
}