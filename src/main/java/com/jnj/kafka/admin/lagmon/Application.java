package com.jnj.kafka.admin.lagmon;

import org.apache.commons.cli.*;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final String OPTION_ACCEPT_LIST = "accept";
    private static final String OPTION_DENY_LIST = "deny";
    private static final String OPTION_LOG_RESULTS = "log";
    private static final String OPTION_COMMAND_TIMEOUT = "timeout";
    // default admin client command timeout is five seconds
    private static final long DEFAULT_COMMAND_TIMEOUT = 5;

    @Autowired
    private AdminClient adminClient;
    @Autowired
    private KafkaConsumer<String, String> consumerClient;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args).close();
    }

    @Override
    public void run(String... args) throws Exception {

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(getOptions(), args);

        Set<String> acceptList = getSet(cmd.getOptionValue(OPTION_ACCEPT_LIST));
        Set<String> denyList = getSet(cmd.getOptionValue(OPTION_DENY_LIST));
        boolean logResults = Boolean.parseBoolean(cmd.getOptionValue(OPTION_LOG_RESULTS));
        long timeout = cmd.hasOption(OPTION_COMMAND_TIMEOUT) ? Long.parseLong(cmd.getOptionValue(OPTION_COMMAND_TIMEOUT)) :
                DEFAULT_COMMAND_TIMEOUT;
        ConsumerLagMonitor consumerLagMonitor = new ConsumerLagMonitor(adminClient, consumerClient, acceptList, denyList);
        consumerLagMonitor.collectStats(timeout, logResults);
    }

    private Options getOptions() {
        Options options = new Options();
        options.addOption(Option.builder("a").longOpt(OPTION_ACCEPT_LIST).optionalArg(true)
                .hasArg(true).desc("Comma-delimited list of consumer group prefixes to allow").type(String.class).build());
        options.addOption(Option.builder("d").longOpt(OPTION_DENY_LIST).optionalArg(true)
                .hasArg(true).desc("Comma-delimited list of consumer group prefixes to deny").type(String.class).build());
        options.addOption(Option.builder("l").longOpt(OPTION_LOG_RESULTS).optionalArg(true)
                .hasArg(true).desc("Log results to console").type(Boolean.class).build());
        options.addOption(Option.builder("t").longOpt(OPTION_COMMAND_TIMEOUT).optionalArg(true)
                .hasArg(true).desc("Maximum seconds to wait for admin client requests").type(Long.class).build());
        return options;
    }

    private Set<String> getSet(String str) {
        Set<String> set = new HashSet<>();
        if (str != null) {
            String[] strs = str.split(",");
            set.addAll(Arrays.asList(strs));
        }
        return set;
    }
}
