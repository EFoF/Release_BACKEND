package com.service.releasenote.domain.alarm.api;

import com.service.releasenote.domain.alarm.application.AlarmService;
import com.service.releasenote.global.util.SecurityUtil;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

import static com.service.releasenote.domain.alarm.dto.AlarmDto.AlarmInfoDto;
import static com.service.releasenote.domain.alarm.dto.AlarmDto.AlarmInfoDtoEach;

@Slf4j
@RestController
@Api(tags = {"alarm"})
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;
    @ApiOperation("api for get alarm")
    @ApiImplicitParam(name = "onlyNew", value = "읽지 않은 알람만 표시", required = true, dataType = "Boolean", paramType = "query", defaultValue = "true")
    @ApiResponses({ @ApiResponse(code=200, message="요청 성공"), @ApiResponse(code=404, message="존재하지 않는 memberProject")})
    @GetMapping("/api/companies/projects/{projectId}/alarms")
    public AlarmInfoDto alarmDetails(
            @PathVariable(name = "projectId") Long projectId,
            @RequestParam(value = "onlyNew", defaultValue = "true") Boolean onlyNew) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if(onlyNew) {
            return alarmService.getAlarmDetailWithNotReadByProjectId(projectId, currentMemberId);
        }
        return alarmService.getAlarmDetailByProjectId(projectId, currentMemberId);
    }

    @ApiOperation("api for get my alarm")
    @GetMapping("/api/alarms")
    public Slice<AlarmInfoDtoEach> myAlarmDetails(Pageable pageable) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return alarmService.getMyAlarmDetail(pageable, currentMemberId);
    }

    @ApiOperation("api for read my alarm")
    @PostMapping("/api/alarms")
    public void myAlarmRead() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

    }

    @ApiOperation("api for read alarm")
    @ApiResponses({ @ApiResponse(code=204, message="요청 성공"), @ApiResponse(code=404, message="존재하지 않는 memberProject")})
    @PostMapping("/api/companies/projects/{projectId}/alarms")
    public void alarmRead(@PathVariable(name = "projectId") Long projectId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        alarmService.readAlarm(projectId, currentMemberId);
    }

    @ApiOperation("api for delete alarm")
    @ApiResponses({ @ApiResponse(code=204, message="요청 성공"), @ApiResponse(code=404, message="알람 자체가 존재하지 않거나 사용자에게 알람이 존재하지 않음")})
    @DeleteMapping("/api/companies/projects/{projectId}/alarms/{alarmId}")
    public void alarmDelete(
            @PathVariable(name = "projectId") Long projectId,
            @PathVariable(name = "alarmId") Long alarmId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        alarmService.deleteAlarm(projectId, alarmId, currentMemberId);
    }



}
