package com.service.releasenote.domain.Alarm.api;

import com.service.releasenote.domain.Alarm.application.AlarmService;
import io.swagger.annotations.*;
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
    @ApiOperation("api for get alarm")
    @ApiImplicitParam(name = "onlyNew", value = "읽지 않은 알람만 표시", required = true,
            dataType = "Boolean", paramType = "query", defaultValue = "true")
    @ApiResponses({
            @ApiResponse(code=200, message="요청 성공"),
            @ApiResponse(code=404, message="존재하지 않는 memberProject")
    })
    public AlarmInfoDto alarmDetails(
            @PathVariable(name = "projectId") Long projectId,
            @RequestParam(value = "onlyNew", defaultValue = "true") Boolean onlyNew) {
        if(onlyNew) {
            return alarmService.getAlarmDetailWithNotReadByProjectId(projectId);
        }
        return alarmService.getAlarmDetailByProjectId(projectId);
    }

    @GetMapping("{projectId}/alarms")
    @ApiOperation("api for read alarm")
    @ApiResponses({
            @ApiResponse(code=200, message="요청 성공"),
            @ApiResponse(code=404, message="존재하지 않는 memberProject")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("{projectId}/alarms")
    public void alarmRead(@PathVariable(name = "projectId") Long projectId) {
        alarmService.readAlarm(projectId);
    }

}
