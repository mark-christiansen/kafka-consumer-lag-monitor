#!/bin/bash

program_name="kafka-consumer-lag-monitor"
version="0.0.1"

display_help() {
    echo "Usage: $0 [option...]" >&2
    echo ""
    echo "   -h, --help                 Displays script usage info"
    echo "   -a, --accept               Comma-delimited list of consumer group prefixes to include (if not specified all will be included)"
    echo "   -d, --deny                 Comma-delimited list of consumer group prefixes to exclude (if not specified all will be included)"
    echo "   -t, --timeout              The timeout (in seconds) for admin client requests"
    echo "   -e, --env                  The Kafka environment to connect to (application-<env>.yaml)"
    echo ""
    exit 1
}

accept=""
deny=""
timeout=""
env=""

# find argument names and values passed into the program
while [[ "$#" -gt 0 ]]; do
    case $1 in
        -h|--help) display_help; shift ;;
        -a|--accept) accept="$2"; shift ;;
        -d|--deny) deny="$2"; shift ;;
        -t|--timeout) timeout="$2"; shift ;;
        -e|--env) env="$2"; shift ;;
        *) echo "Unknown parameter passed: $1" ;;
    esac
    shift
done

# verify required arguments were passed
[[ $env == "" ]] && { echo "The option --env (-e) is required but not set, please specify the environment name"  && display_help && exit 1; }
if [[ $accept != "" ]]; then
  accept="--accept $accept"
fi
if [[ $deny != "" ]]; then
  deny="--deny $deny"
fi
if [[ $timeout != "" ]]; then
  timeout="--timeout $timeout"
fi

# execute program
java -cp "$program_name-$version.jar" -Dloader.main=com.jnj.kafka.admin.lagmon.Application -Dspring.profiles.active=$env -Dspring.config.location=conf/ org.springframework.boot.loader.PropertiesLauncher $accept $deny $timeout