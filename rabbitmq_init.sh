#!/bin/bash

# rabbitmq.conf 파일 경로 설정
RABBITMQ_CONF_FILE="/etc/rabbitmq/rabbitmq.conf"

# AMQP 포트 설정 (기본값 5672)
echo "listeners.tcp.default = 5672" >> $RABBITMQ_CONF_FILE
# RabbitMQ 관리 UI 포트 설정 (기본값 15672)
echo "management.listener.port = 15672" >> $RABBITMQ_CONF_FILE
# STOMP 포트 설정 (기본값 61613)
echo "stomp.listeners.tcp.default = 61613" >> $RABBITMQ_CONF_FILE

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