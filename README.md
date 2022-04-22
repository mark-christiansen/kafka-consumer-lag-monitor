# kafka-admin-lag-monitor
To run the script make sure that you have the compiled jar file "kafka-consumer-lag-monitor-0.0.1.jar" in the same 
directory as the shell script "kafka-consumer-lag-monitor.sh". In that same directory create a "conf" directory and add 
an "application-<env>.yaml" to that directory, where <env> is an arbitrary name you want to give the Kafka environment 
you are retrieving consumer group lag data from. See the example YAML file, "application-dev.yaml" in this project's 
"conf" folder for an example of how to setup the file.
In that all properties under "admin" are Kafka admin client properties as defined
[here](https://docs.confluent.io/platform/current/installation/configuration/admin-configs.html).
In that YAML, all properties under "consumer" are Kafka consumer properties as defined
[here](https://docs.confluent.io/platform/current/installation/configuration/consumer-configs.html).

When running the script you specify the options like show below:

```
kafka-consumer-lag-monitor.sh --timeout 5 --env dev

    timeout: the number of seconds to wait for admin client requests (default is 5 seconds)
    
    env: the name of the environment (conf/application-<env>.yaml) to load
```

You can retrieve consumer group offsets for just the consumer groups that are prefixed with one of the values specified 
by the `accept` argument as shown below.

```
kafka-consumer-lag-monitor.sh --accept _confluent,app1-group,app2-group --env dev
```

This will return the consumer group offset data for any consumer group prefixed with `_confluent`,`app1-group`, or 
`app2-group`.

You can also retrieve consumer group offsets for the consumer groups that are NOT prefixed with one of the values 
specified by the `deny` argument as shown below.

```
kafka-consumer-lag-monitor.sh --deny _confluent-controlcenter,test --env dev
```

This will return the consumer group offset data for any consumer group not prefixed with `_confluent-controlcenter` or
`test`.