package com.service.releasenote.global.alarm.api;

import com.service.releasenote.global.alarm.application.RabbitmqService;
import com.service.releasenote.global.alarm.dto.RabbitmqDto;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import static com.service.releasenote.global.alarm.dto.RabbitmqDto.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/queue")
@Api(tags = {"amqp-rabbitmq"})
public class RabbitmqController {

    private final RabbitmqService rabbitmqService;

    @PostMapping
    public String queueCreate(@RequestBody SaveQueueRequest saveQueueRequest) {
        return rabbitmqService.saveQueue(saveQueueRequest);
    }

    @GetMapping
    public QueueInformation queueInfoGet(@RequestParam(value="queuename") String queueName) {
        return rabbitmqService.getQueueInfo(queueName);
    }

    @DeleteMapping
    public String queueDelete(@RequestParam(value = "queuename") String queueName) {
        return rabbitmqService.deleteQueue(queueName);
    }
}
