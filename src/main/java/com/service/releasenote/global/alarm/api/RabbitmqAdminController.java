package com.service.releasenote.global.alarm.api;

import com.service.releasenote.global.alarm.application.RabbitmqAdminService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.web.bind.annotation.*;

import static com.service.releasenote.global.alarm.dto.RabbitmqAdminDto.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Api(tags = {"amqp-rabbitmq"})
public class RabbitmqAdminController {

    private final RabbitmqAdminService rabbitmqAdminService;

    @PostMapping("/exchange")
    public String exchangeCreate() {
        return rabbitmqAdminService.saveExchange();
    }

    @PostMapping("/queue")
    public String queueCreate(@RequestBody SaveQueueRequest saveQueueRequest) {
        return rabbitmqAdminService.saveQueue(saveQueueRequest);
    }

    @GetMapping
    public QueueInformation queueInfoGet(@RequestParam(value="queuename") String queueName) {
        return rabbitmqAdminService.getQueueInfo(queueName);
    }

    @DeleteMapping
    public String queueDelete(@RequestParam(value = "queuename") String queueName) {
        return rabbitmqAdminService.deleteQueue(queueName);
    }
}
