package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.service.FakeImage;
import com.udacity.catpoint.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    SecurityService securityService;
    SecurityRepository testData;

    @Mock
    FakeImage fakeImage;

    @BeforeEach
    void init() {
        // ensures system is always: 'AlarmStatus.NO_ALARM & ArmingStatus.DISARMED' when initiated
        // furthermore, all sensors initialized are true/active
        testData = new PretendDatabaseSecurityRepositoryImpl();
        testData.setAlarmStatus(AlarmStatus.NO_ALARM);
        testData.setArmingStatus(ArmingStatus.DISARMED);
    }

    @Test
    void internal_confirmsCleanSystemStart_SystemIsNoAlarmAndDisarmed() {
        assertAll(
                () -> assertEquals(AlarmStatus.NO_ALARM, testData.getAlarmStatus()),
                () -> assertEquals(ArmingStatus.DISARMED, testData.getArmingStatus())
        );
    }

    @Test
    public void changeSensorActivationStatus_alarmArmedAndSensorActivated_returnPendingAlarm() {
    // 1. Requirement: If alarm is armed and a sensor becomes activated, put the system into pending alarm status
        testData.setArmingStatus(ArmingStatus.ARMED_HOME);
        // TODO: Add the second option!
        //testData.setArmingStatus(ArmingStatus.ARMED_AWAY);
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        Boolean sensorState = true;

        securityService = new SecurityService(testData, fakeImage);
        securityService.changeSensorActivationStatus(sensor, sensorState);
        assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus());
    }

    @Test
    public void what_method_am_I_testing() {
        // 2. Requirement: If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.
        testData.setArmingStatus(ArmingStatus.ARMED_HOME);
        // TODO: Add the second option!
        //testData.setArmingStatus(ArmingStatus.ARMED_AWAY);
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        Boolean sensorState = true;
        testData.setAlarmStatus(AlarmStatus.PENDING_ALARM);

        securityService = new SecurityService(testData, fakeImage);
        securityService.changeSensorActivationStatus(sensor, sensorState);
        assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
    }

    @Test
    public void something() {
        //If pending alarm and all sensors are inactive, return to no alarm state.
        testData.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        for (Sensor sensor : testData.getSensors()) {
            System.out.println(sensor.getActive());
        }

        assertEquals(AlarmStatus.NO_ALARM, securityService.getAlarmStatus());
    }
}