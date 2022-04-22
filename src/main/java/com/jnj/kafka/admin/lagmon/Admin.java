package com.jnj.kafka.admin.lagmon;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class Admin {

    private static final Logger log = LoggerFactory.getLogger(Admin.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final AdminClient adminClient;
    private KafkaConsumer<String, String> consumer;
    private final Set<String> acceptList;
    private final Set<String> denyList;

    public Admin(AdminClient adminClient,
                 KafkaConsumer<String, String> consumer,
                 Set<String> acceptList,
                 Set<String> denyList) {
        this.adminClient = adminClient;
        this.consumer = consumer;
        this.acceptList = acceptList;
        this.denyList = denyList;
    }

    public void start(long timeout, boolean logResults) {

        final List<ConsumerGroupOffset> offsets = new ArrayList<>();

        final ListConsumerGroupsResult listResult = adminClient.listConsumerGroups();
        List<ConsumerGroupListing> consumerGroups = new ArrayList<>();
        try {

            Collection<ConsumerGroupListing> listResults = listResult.all().get(timeout, TimeUnit.SECONDS);
            consumerGroups = listResults.stream()
                    .filter(g -> filter(g.groupId(), acceptList, denyList))
                    .collect(Collectors.toList());
            consumerGroups.sort(Comparator.comparing(ConsumerGroupListing::groupId));

        } catch (Exception e) {
           log.error("Unexpected error getting consumer groups", e);
        }

        // Gather the consumer group offset and log end offset data for all the consumer groups
        String groupId = null;
        List<String> groupIds = new ArrayList<>();
        try {
            for (ConsumerGroupListing group : consumerGroups) {

                groupId = group.groupId();
                groupIds.add(groupId);

                // get the consumer offsets
                Map<TopicPartition, Long> consumerGroupOffsets = getConsumerGroupOffsets(groupId);
                // get the log end offsets
                Map<TopicPartition, Long> logEndOffsets = consumer.endOffsets(consumerGroupOffsets.keySet());
                logEndOffsets.keySet().stream()
                    .forEach(p -> {
                        ConsumerGroupOffset offset = new ConsumerGroupOffset();
                        offset.setGroupId(group.groupId());
                        offset.setTopic(p.topic());
                        offset.setPartition(p.partition());
                        offset.setConsumerGroupOffset(consumerGroupOffsets.get(p));
                        offset.setLogEndOffset(logEndOffsets.get(p));
                        offsets.add(offset);
                    });
            }
        } catch (Exception e) {
            log.error(format("Unexpected error getting consumer group offsets for group %s", groupId), e);
        }

        Map<String, List<ConsumerGroupOffset>> offsetsByGroupId = offsets.stream()
                .collect(Collectors.groupingBy(ConsumerGroupOffset::getGroupId));

        // Collect the consumer data for each consumer group that has active consumers
        DescribeConsumerGroupsResult describeResult = adminClient.describeConsumerGroups(groupIds);
        try {
            Map<String, ConsumerGroupDescription> describeResults = describeResult.all().get(timeout, TimeUnit.SECONDS);
            describeResults.values()
                .forEach(g -> {
                    List<ConsumerGroupOffset> groupIdOffsets = offsetsByGroupId.get(g.groupId());
                    g.members()
                        .forEach(m -> {
                            ConsumerGroupOffset.Consumer consumer = new ConsumerGroupOffset.Consumer();
                            consumer.setHost(m.host());
                            consumer.setClientId(m.clientId());
                            consumer.setState(g.state().name());
                            if (groupIdOffsets != null) {
                                groupIdOffsets.forEach(o -> o.addConsumer(consumer));
                            }
                        });
                });
        } catch (Exception e) {
            log.error("Unexpected error describing consumer groups", e);
        }

        // write the consumer group offset data to the console as JSON
        try {
            for (ConsumerGroupOffset offset : offsets) {
                log.info(mapper.writeValueAsString(offset));
            }
        } catch (Exception e) {
            log.error("Unexpected error converting consumer offset data to JSON", e);
        }
    }

    private boolean filter(String groupId, Set<String> acceptList, Set<String> denyList) {

        if (acceptList.isEmpty() && denyList.isEmpty()) {
            return true;
        }

        // if accept list specified, check that group ID starts with one of the prefixes in the accept list
        if (!acceptList.isEmpty()) {
            for (String accept : acceptList) {
                if (groupId.startsWith(accept)) {
                    return true;
                }
            }
            return false;
        }

        // if deny list specified, check that group ID does not start with one of the prefixes in the deny list
        for (String deny : denyList) {
            if (groupId.startsWith(deny)) {
                return false;
            }
        }
        return true;
    }

    private Map<TopicPartition, Long> getConsumerGroupOffsets(String groupId)
            throws ExecutionException, InterruptedException {

        ListConsumerGroupOffsetsResult info = adminClient.listConsumerGroupOffsets(groupId);
        Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap = info.partitionsToOffsetAndMetadata().get();

        Map<TopicPartition, Long> groupOffset = new HashMap<>();
        for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : topicPartitionOffsetAndMetadataMap.entrySet()) {
            TopicPartition key = entry.getKey();
            OffsetAndMetadata metadata = entry.getValue();
            groupOffset.putIfAbsent(new TopicPartition(key.topic(), key.partition()), metadata.offset());
        }
        return groupOffset;
    }
}
