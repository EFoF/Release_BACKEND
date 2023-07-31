package com.service.releasenote.domain.Alarm.api;

import com.service.releasenote.domain.Alarm.application.AlarmService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.service.releasenote.domain.Alarm.dto.AlarmDto.*;

@Slf4j
@RestController
@Api(tags = {"alarm"})
@RequiredArgsConstructor
@RequestMapping("/companies/projects")
public class AlarmController {

    private final AlarmService alarmService;
    @GetMapping("{projectId}/alarms")
    public AlarmInfoDto alarmDetails(
            @PathVariable(name = "projectId") Long projectId,
            @RequestParam(value = "onlyNew", defaultValue = "true") Boolean onlyNew) {
        if(onlyNew) {
            return alarmService.getAlarmDetailWithNotReadByProjectId(projectId);
        }
        return alarmService.getAlarmDetailByProjectId(projectId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("{projectId}/alarms")
    public void alarmRead(@PathVariable(name = "projectId") Long projectId) {
        alarmService.readAlarm(projectId);
    }

}
