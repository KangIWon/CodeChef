#!/bin/bash

# Copy Erlang cookie
cp /var/lib/rabbitmq/.erlang.cookie /var/lib/rabbitmq/.erlang.cookie
chown rabbitmq:rabbitmq /var/lib/rabbitmq/.erlang.cookie
chmod 400 /var/lib/rabbitmq/.erlang.cookie

# Enable required RabbitMQ plugins
rabbitmq-plugins enable rabbitmq_management
rabbitmq-plugins enable rabbitmq_web_stomp
rabbitmq-plugins enable rabbitmq_stomp

# Add system user with admin privileges
rabbitmqctl add_user "${RABBITMQ_SYSTEM_USER}" "${RABBITMQ_SYSTEM_PASS}"
rabbitmqctl set_user_tags "${RABBITMQ_SYSTEM_USER}" administrator
rabbitmqctl set_permissions -p / "${RABBITMQ_SYSTEM_USER}" ".*" ".*" ".*"

# Add client user with limited privileges
rabbitmqctl add_user "${RABBITMQ_CLIENT_USER}" "${RABBITMQ_CLIENT_PASS}"
rabbitmqctl set_user_tags "${RABBITMQ_CLIENT_USER}" none
rabbitmqctl set_permissions -p / "${RABBITMQ_CLIENT_USER}" ".*" ".*" ".*"

# Delete guest user
rabbitmqctl delete_user guest