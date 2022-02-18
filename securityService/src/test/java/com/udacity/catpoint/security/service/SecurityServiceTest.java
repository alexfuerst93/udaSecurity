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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    private SecurityService securityService;

    @Mock
    private FakeImage fakeImage;

    @Mock
    private PretendDatabaseSecurityRepositoryImpl newTestData;

    @BeforeEach
    void init() {
        securityService = new SecurityService(newTestData, fakeImage);
    }

    @Test
    public void changeSensorActivationStatus_alarmArmedAndSensorActivated_returnPendingAlarm() {
    // 1. Requirement: If alarm is armed and a sensor becomes activated, put the system into pending alarm status
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        Boolean sensorState = true;

        when(newTestData.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        // TODO: Add the second option!
        //testData.setArmingStatus(ArmingStatus.ARMED_AWAY);
        when(newTestData.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

        securityService.addSensor(sensor);
        securityService.changeSensorActivationStatus(sensor, sensorState);

        verify(newTestData).setAlarmStatus(AlarmStatus.PENDING_ALARM);
        //assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus()); -> Doesnt work because return type is void
    }

    @Test
    public void what_method_am_I_testing() {
        // 2. Requirement: If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        Boolean sensorState = true;

        when(newTestData.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        // TODO: Add the second option!
        //testData.setArmingStatus(ArmingStatus.ARMED_AWAY);
        when(newTestData.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.addSensor(sensor);
        securityService.changeSensorActivationStatus(sensor, sensorState);
        verify(newTestData).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void something() {
        // 3. Requirement: If pending alarm and all sensors are inactive, return to no alarm state.
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        Boolean sensorState = true;

        when(newTestData.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        // TODO: Add the second option!
        when(newTestData.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.addSensor(sensor);
        securityService.changeSensorActivationStatus(sensor, sensorState);
        securityService.changeSensorActivationStatus(sensor, false);

        verify(newTestData).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    public void something_else() {
        // 4. If alarm is active, change in sensor state should not affect the alarm state.
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        Boolean sensorState = true;

        when(newTestData.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        // TODO: Add the second option!
        when(newTestData.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        securityService.addSensor(sensor);
        securityService.changeSensorActivationStatus(sensor, sensorState);
        securityService.changeSensorActivationStatus(sensor, false);

        // Is my test false or the code?
        // My understanding: Once the Alarm fires, even deactivated sensors won't change the status
        verify(newTestData, never()).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    public void yet_again_something_else() {
        // 5. Requirement: If a sensor is activated while already active and the system is in pending state, change it to alarm state.

    }
}