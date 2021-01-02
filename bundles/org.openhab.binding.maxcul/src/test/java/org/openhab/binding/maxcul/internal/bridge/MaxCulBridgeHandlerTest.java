/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.maxcul.internal.bridge;

/**
 * @author Sascha Volkenandt - Initial contribution
 *
 * @RunWith(MockitoJUnitRunner.class)
 *                                    public class MaxCulBridgeHandlerTest {
 * 
 *                                    private final BridgeImpl bridge = new BridgeImpl(THING_TYPE_BRIDGE, "theBridge");
 * 
 * @Mock
 *       private MaxCulTransceiver transceiverMock;
 * 
 * @Mock
 *       private ThingHandlerCallback callbackMock;
 * 
 *       private MaxCulBridgeHandler bridgeHandlerUnderTest;
 * 
 * @Before
 *         public void setUp() {
 *         Configuration configuration = new Configuration();
 *         configuration.put("rfAddress", "123456");
 *         configuration.put("fakeWTAddress", "111111");
 *         bridge.setConfiguration(configuration);
 * 
 *         bridgeHandlerUnderTest = new MaxCulBridgeHandler(bridge, transceiverMock);
 *         bridgeHandlerUnderTest.setCallback(callbackMock);
 *         }
 * 
 * @Test
 *       public void initialize_ok() {
 *       doReturn(true).when(transceiverMock).initialize(any(), any());
 * 
 *       bridgeHandlerUnderTest.initialize();
 *       verify(transceiverMock, timeout(500)).connect();
 * 
 *       InOrder inOrder = inOrder(transceiverMock, callbackMock);
 *       inOrder.verify(callbackMock).statusUpdated(any(), matchesStatus(ThingStatus.OFFLINE,
 *       ThingStatusDetail.CONFIGURATION_PENDING));
 *       inOrder.verify(transceiverMock).initialize(same(bridgeHandlerUnderTest), any());
 *       inOrder.verify(transceiverMock).connect();
 *       verifyNoMoreInteractions(transceiverMock, callbackMock);
 *       }
 * 
 * @Test
 *       public void initialize_transceiverInitializeError() {
 *       doReturn(false).when(transceiverMock).initialize(any(), any());
 * 
 *       bridgeHandlerUnderTest.initialize();
 * 
 *       InOrder inOrder = inOrder(transceiverMock, callbackMock);
 *       inOrder.verify(callbackMock).statusUpdated(any(), matchesStatus(ThingStatus.OFFLINE,
 *       ThingStatusDetail.CONFIGURATION_PENDING));
 *       inOrder.verify(transceiverMock).initialize(same(bridgeHandlerUnderTest), any());
 *       verifyNoMoreInteractions(transceiverMock, callbackMock);
 *       }
 * 
 * @Test
 *       public void receiveVersion_ok() {
 *       bridgeHandlerUnderTest.receiveVersion(167);
 * 
 *       InOrder inOrder = inOrder(transceiverMock, callbackMock);
 *       inOrder.verify(callbackMock).statusUpdated(any(), matchesStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE));
 *       inOrder.verify(transceiverMock).send("X21");
 *       inOrder.verify(transceiverMock).send("Zr");
 *       inOrder.verify(transceiverMock).send("Za123456");
 *       inOrder.verify(transceiverMock).send("Zw111111");
 *       verifyNoMoreInteractions(transceiverMock, callbackMock);
 *       }
 * 
 * @Test
 *       public void receiveVersion_tooOld() {
 *       bridgeHandlerUnderTest.receiveVersion(152);
 * 
 *       InOrder inOrder = inOrder(transceiverMock, callbackMock);
 *       inOrder.verify(callbackMock).statusUpdated(any(), matchesStatus(ThingStatus.OFFLINE,
 *       ThingStatusDetail.COMMUNICATION_ERROR));
 *       inOrder.verify(transceiverMock).disconnect();
 *       verifyNoMoreInteractions(transceiverMock, callbackMock);
 *       }
 * 
 * @Test
 *       public void dispatch_callsDevices() {
 *       MaxDeviceHandler firstDeviceHandlerMock = mockDeviceHandler("firstThing", null);
 *       MaxDeviceHandler secondDeviceHandlerMock = mockDeviceHandler("secondThing", null);
 *       ThermostatStateMoritzMessage messageMock = mock(ThermostatStateMoritzMessage.class);
 * 
 *       bridgeHandlerUnderTest.dispatch(messageMock);
 * 
 *       verify(firstDeviceHandlerMock).dispatch(same(messageMock));
 *       verify(secondDeviceHandlerMock).dispatch(same(messageMock));
 *       verifyNoMoreInteractions(firstDeviceHandlerMock, secondDeviceHandlerMock, messageMock);
 *       }
 * 
 * @Test
 *       public void sendFakeWallThermostat_singleDeviceInRoom() {
 *       mockDeviceHandler("firstThing", "firstRoom");
 *       mockDeviceHandler("secondThing", "secondRoom");
 * 
 *       bridgeHandlerUnderTest.sendVirtualThermostat("firstRoom", 21.5, 23.8);
 * 
 *       verify(transceiverMock, times(1)).queueCommand(argThat(MoritzCulCommand.class::isInstance));
 *       verifyNoMoreInteractions(transceiverMock, callbackMock);
 *       }
 * 
 *       private MaxDeviceHandler mockDeviceHandler(String thingId, String roomName) {
 *       HeatingThermostatDeviceHandler deviceHandler = mock(HeatingThermostatDeviceHandler.class);
 *       //noinspection ResultOfMethodCallIgnored
 *       doReturn(MaxDeviceType.HEATING_THERMOSTAT).when(deviceHandler).getDeviceType();
 *       doReturn(roomName).when(deviceHandler).getRoomName();
 * 
 *       ThingImpl thing = new ThingImpl(THING_TYPE_HEATING_THERMOSTAT, thingId);
 *       thing.setHandler(deviceHandler);
 *       bridge.addThing(thing);
 * 
 *       return deviceHandler;
 *       }
 * 
 *       private static ThingStatusInfo matchesStatus(ThingStatus status, ThingStatusDetail statusDetail) {
 *       return argThat(statusInfo -> statusInfo.getStatus() == status && statusInfo.getStatusDetail() == statusDetail);
 *       }
 *       }
 */
