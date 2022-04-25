package com.jnj.kafka.admin.lagmon;

import java.util.ArrayList;
import java.util.List;

public class ConsumerGroupOffset {

    private String groupId;
    private String topic;
    private int partition;
    private long consumerGroupOffset;
    private long logEndOffset;
    private final List<Consumer> consumers = new ArrayList<>();

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public long getConsumerGroupOffset() {
        return consumerGroupOffset;
    }

    public void setConsumerGroupOffset(long consumerGroupOffset) {
        this.consumerGroupOffset = consumerGroupOffset;
    }

    public long getLogEndOffset() {
        return logEndOffset;
    }

    public void setLogEndOffset(long logEndOffset) {
        this.logEndOffset = logEndOffset;
    }

    public List<Consumer> getConsumers() {
        return consumers;
    }

    public void addConsumer(Consumer consumer) {
        this.consumers.add(consumer);
    }

    public long getLag() {
        return logEndOffset - consumerGroupOffset;
    }

    @Override
    public String toString() {
        return "ConsumerGroupOffset{" +
                "groupId='" + groupId + '\'' +
                ", topic='" + topic + '\'' +
                ", partition=" + partition +
                ", consumerGroupOffset=" + consumerGroupOffset +
                ", logEndOffset=" + logEndOffset +
                ", lag=" + getLag() +
                ", consumers=" + consumers +
                '}';
    }

    public static class Consumer {

        private String clientId;
        private String host;
        private String state;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return "Consumer{" +
                    "clientId='" + clientId + '\'' +
                    ", host='" + host + '\'' +
                    ", state='" + state + '\'' +
                    '}';
        }
    }
}
