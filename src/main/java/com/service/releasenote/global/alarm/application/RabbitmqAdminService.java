package com.service.releasenote.global.alarm.application;

import com.service.releasenote.global.alarm.exception.QueueNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.service.releasenote.global.alarm.dto.RabbitmqAdminDto.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitmqAdminService {

    private final AmqpAdmin amqpAdmin;

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    // queueName 컨벤션 : project-{projectName}-queue
    // bindingKey 컨벤션 : {projectName} (다이렉트 전략이라 queue이름과 일치시킴)
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
