package com.service.releasenote.global.alarm.application;

import com.service.releasenote.global.alarm.dto.RabbitmqDto;
import com.service.releasenote.global.alarm.exception.QueueNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.service.releasenote.global.alarm.dto.RabbitmqDto.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitmqService {

    private final AmqpAdmin amqpAdmin;

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    public String saveQueue(SaveQueueRequest saveQueueRequest) {
        Queue queue = new Queue(saveQueueRequest.getQueueName(), true, false, false);
        Binding binding = new Binding(saveQueueRequest.getQueueName(), Binding.DestinationType.QUEUE,
                exchangeName, saveQueueRequest.getBindingKey(), null);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(binding);
        return saveQueueRequest.getQueueName() + " created";
    }

    public QueueInformation getQueueInfo(String queueName) {
        QueueInformation queueInfo = amqpAdmin.getQueueInfo(queueName);
        if(queueInfo == null) {
            throw new QueueNotFoundException();
        }
        return queueInfo;
    }

    public String deleteQueue(String queueName) {
        amqpAdmin.deleteQueue(queueName);
        return queueName + " deleted";
    }

}
