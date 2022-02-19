package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.service.FakeImage;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    private SecurityService securityService; // All unit tests below test this specific ServiceClass
    private Sensor sensor;
    private StatusListener statusListener;

    @Mock
    private FakeImage fakeImage;

    @Mock
    private PretendDatabaseSecurityRepositoryImpl newTestData;

    @BeforeEach
    void init() {
        securityService = new SecurityService(newTestData, fakeImage);
        sensor = new Sensor("testSensor", SensorType.DOOR);
    }

    private static Stream<Arguments> differentArmingStatus() {
        return Stream.of(
                Arguments.of(ArmingStatus.ARMED_HOME),
                Arguments.of(ArmingStatus.ARMED_AWAY)
        );
    }

    @ParameterizedTest
    @MethodSource("differentArmingStatus")
    public void changeSensorActivationStatus_armedAndActivatedSensor_returnPendingAlarm(ArmingStatus armingStatus) {
    // 1. Requirement: If alarm is armed and a sensor becomes activated, put the system into pending alarm status
        when(newTestData.getArmingStatus()).thenReturn(armingStatus);
        when(newTestData.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

        securityService.addSensor(sensor);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(newTestData).setAlarmStatus(AlarmStatus.PENDING_ALARM);
        //assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus()); -> Doesnt work because returns void
    }

    @ParameterizedTest
    @MethodSource("differentArmingStatus")
    public void changeSensorActivationStatus_armedPendingAndActivatedSensor_returnAlarm(ArmingStatus armingStatus) {
        // 2. Requirement: If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.
        when(newTestData.getArmingStatus()).thenReturn(armingStatus);
        when(newTestData.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.addSensor(sensor);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(newTestData).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void changeSensorActivationStatus_armedPendingAndInactiveSensor_returnNoAlarm() {
        // 3. Requirement: If pending alarm and all sensors are inactive, return to no alarm state.
        when(newTestData.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.addSensor(sensor);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, false);

        verify(newTestData).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    public void changeSensorActivationStatus_alarmActiveAndChangeSensorState_returnNoAlarmChange() {
        // 4. If alarm is active, change in sensor state should not affect the alarm state.
        when(newTestData.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        securityService.addSensor(sensor);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, false);

        // My understanding: Once the Alarm fires, even deactivating sensors won't change the status
        // Therefore, the source code must have a bug!
        verify(newTestData, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test
    public void changeSensorActivationStatus_pendingAndSensorActiveAndActivatedAgain_returnAlarm() {
        // 5. Requirement: If a sensor is activated while already active and the system is in pending state, change it to alarm state.
        when(newTestData.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.addSensor(sensor);
        securityService.removeSensor(sensor); // added for full coverage
        securityService.addSensor(sensor);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, true);

        // Problem: If sensor does not change state, it will not get forwarded to another function, thus never changing AlarmStatus
        // Source code has a bug!
        verify(newTestData).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void changeSensorActivationStatus_deactivatedSensorGetsDeactivated_returnNoAlarmChanges() {
        // 6. Requirement: If a sensor is deactivated while already inactive, make no changes to the alarm state.
        securityService.addSensor(sensor); // newly created sensors are per default deactivated
        securityService.changeSensorActivationStatus(sensor, false);

        // If sensor does not change state, it will not get forwarded to another function, thus never changing AlarmStatus
        verify(newTestData, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test
    public void processImage_armedCatDetected_returnAlarm() {
        // 7. Requirement: If the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status.
        when(newTestData.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        BufferedImage catImage = new BufferedImage(50, 50, 1);
        when(fakeImage.imageContainsCat(eq(catImage), anyFloat())).thenReturn(true);

        securityService.processImage(catImage); // this function calls the image interface, which "mocks" into True, meaning a cat got detected

        verify(newTestData).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void processImage_ActiveSensorsAndNoCatImage_returnAlarm() {
        // 8. Requirement: If the image service identifies an image that does not contain a cat, change the status to no alarm as long as (= meaning 'only then') the sensors are not active.
        Set<Sensor> sensorSet = Set.of(sensor);
        when(newTestData.getSensors()).thenReturn(sensorSet);

        securityService.addSensor(sensor);
        sensor.setActive(true);

        BufferedImage noCatImage = new BufferedImage(50, 50, 1);
        when(fakeImage.imageContainsCat(eq(noCatImage), anyFloat())).thenReturn(false);
        securityService.processImage((noCatImage));

        // Problem: system changes to NO_ALARM even though sensors are active
        // Source code has a bug!
        verify(newTestData, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test
    public void processImage_noActiveSensorsAndNoCatImage_returnNoAlarm() {
        Set<Sensor> sensorSet = Set.of(sensor);
        when(newTestData.getSensors()).thenReturn(sensorSet);

        securityService.addSensor(sensor); // sensor is deactivated per default

        BufferedImage noCatImage = new BufferedImage(50, 50, 1);
        when(fakeImage.imageContainsCat(eq(noCatImage), anyFloat())).thenReturn(false);
        securityService.processImage((noCatImage));

        verify(newTestData).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    public void setArmingStatus_disarmSystem_returnAlarm() {
        // 9. Requirement: If the system is disarmed, set the status to no alarm.
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(newTestData).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest
    @MethodSource("differentArmingStatus")
    public void setArmingStatus_armedSystem_deactivateAllSensors(ArmingStatus armingStatus) {
        // 10. Requirement: If the system is armed, reset all sensors to inactive.
        Set<Sensor> sensorSet = Set.of(sensor);
        when(newTestData.getSensors()).thenReturn(sensorSet);

        securityService.addSensor(sensor);
        sensor.setActive(true);

        securityService.setArmingStatus(armingStatus);
        // The function does not change any sensor activity
        // Source code has a bug!
        assertFalse(sensor.getActive());
    }

    @Test
    public void processImage_armedHomeAndCatImage_returnAlarm() {
        // 11. Requirement: If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
        when(newTestData.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        BufferedImage catImage = new BufferedImage(50, 50, 1);
        when(fakeImage.imageContainsCat(eq(catImage), anyFloat())).thenReturn(true);

        securityService.processImage(catImage);

        verify(newTestData).setAlarmStatus(AlarmStatus.ALARM);
    }
    
    @Test
    public void test_statusListener() {
        // achieve full coverage
        securityService.addStatusListener(statusListener);
        securityService.removeStatusListener(statusListener);
    }
}