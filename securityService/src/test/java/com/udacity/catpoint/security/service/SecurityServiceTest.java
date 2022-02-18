package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.service.FakeImage;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    private SecurityService securityService;

    @Mock
    private FakeImage fakeImage;

    @Mock
    private PretendDatabaseSecurityRepositoryImpl newTestData;

    @Mock
    private StatusListener statusListener;

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
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        sensor.setActive(true); // sensor is already active

        when(newTestData.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        // TODO: Add the second option!
        when(newTestData.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.addSensor(sensor);
        securityService.changeSensorActivationStatus(sensor, true);

        // Problem: If sensor does not change state, it will not get forwarded to another function, thus never changing AlarmStatus
        verify(newTestData).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void foo() {
        // 6. Requirement: If a sensor is deactivated while already inactive, make no changes to the alarm state.
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR); // newly created sensors are per default deactivated

        securityService.addSensor(sensor);
        securityService.changeSensorActivationStatus(sensor, false);

        // Problem: If sensor does not change state, it will not get forwarded to another function, thus never changing AlarmStatus
        // feels like a bug, but works!
        // TODO: Add the other 2 AlarmStatus stati
        verify(newTestData, never()).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    public void bar() {
        // 7. Requirement: If the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status.
        when(newTestData.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        BufferedImage catImage = new BufferedImage(50, 50, 1);
        when(fakeImage.imageContainsCat(eq(catImage), anyFloat())).thenReturn(true);

        securityService.processImage(catImage); // this function calls the image interface, which "mocks" into True, meaning a cat got detected

        verify(newTestData).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void bazz() {
        // 8. Requirement: If the image service identifies an image that does not contain a cat, change the status to no alarm as long as (= meaning 'only then') the sensors are not active.
        when(newTestData.getAlarmStatus()).thenReturn(AlarmStatus.ALARM); // currently, the system is in alarm-state, because the sensor is active

        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        sensor.setActive(true);
        securityService.addSensor(sensor);

        BufferedImage noCatImage = new BufferedImage(50, 50, 1);
        when(fakeImage.imageContainsCat(eq(noCatImage), anyFloat())).thenReturn(false);
        securityService.processImage((noCatImage));

        // Problem: system changes to NO_ALARM even though sensors are active
        verify(newTestData).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void fizz() {
        // 9. Requirement: If the system is disarmed, set the status to no alarm.
        when(newTestData.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);

        securityService.setArmingStatus(newTestData.getArmingStatus());

        verify(newTestData).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    public void second_last() {
        // 10. If the system is armed, reset all sensors to inactive.
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        sensor.setActive(true);
        securityService.addSensor(sensor);

        // TODO: add the 2nd option for armed_away
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        assertFalse(sensor.getActive());
    }

    @Test
    public void last() {
        // 11. If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
        when(newTestData.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        BufferedImage catImage = new BufferedImage(50, 50, 1);
        when(fakeImage.imageContainsCat(eq(catImage), anyFloat())).thenReturn(true);

        securityService.processImage(catImage);

        verify(newTestData).setAlarmStatus(AlarmStatus.ALARM);
    }
}