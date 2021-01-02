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
package org.openhab.binding.maxcul.internal.device;

/**
 * @author Sascha Volkenandt - Initial contribution
 *
 * @RunWith(MockitoJUnitRunner.class)
 *                                    public class VirtualThermostatMaxDeviceHandlerTest {
 * 
 *                                    private static final String BRIDGE_ID = "theBridge";
 *                                    private static final String THING_ID = "theVirtualThermostat";
 *                                    private static final String ADDRESS = "ababab";
 *                                    private static final String ROOM_NAME = "theRoom";
 * 
 * @Mock
 *       private MaxCulBridgeHandler bridgeHandlerMock;
 * 
 * @Mock
 *       private StorageService storageServiceMock;
 * 
 * @Mock
 *       private ThingHandlerCallback callbackMock;
 * 
 *       private VirtualThermostatMaxDeviceHandler deviceHandlerUnderTest;
 * 
 * @Before
 *         public void setUp() {
 *         BridgeImpl bridge = new BridgeImpl(THING_TYPE_BRIDGE, BRIDGE_ID);
 *         bridge.setHandler(bridgeHandlerMock);
 *         doReturn(bridge).when(callbackMock).getBridge(bridge.getUID());
 * 
 *         Configuration configuration = new Configuration();
 *         configuration.put("roomName", ROOM_NAME);
 * 
 *         ThingImpl thing = new ThingImpl(THING_TYPE_VIRTUAL_THERMOSTAT, THING_ID);
 *         thing.setConfiguration(configuration);
 *         thing.setBridgeUID(bridge.getUID());
 * 
 *         deviceHandlerUnderTest = new VirtualThermostatMaxDeviceHandler(thing, storageServiceMock);
 *         deviceHandlerUnderTest.setCallback(callbackMock);
 *         }
 * 
 * @Test
 *       public void initialize_ok() {
 *       doReturn(ADDRESS).when(bridgeHandlerMock).getFakeWTAddress();
 * 
 *       deviceHandlerUnderTest.initialize();
 * 
 *       assertThat(deviceHandlerUnderTest.getDeviceType()).isEqualTo(MaxDeviceType.WALL_MOUNTED_THERMOSTAT);
 *       assertThat(deviceHandlerUnderTest.getAddress()).isEqualTo(ADDRESS);
 *       assertThat(deviceHandlerUnderTest.getRoomName()).isEqualTo(ROOM_NAME);
 * 
 *       InOrder inOrder = inOrder(callbackMock);
 *       inOrder.verify(callbackMock).statusUpdated(any(), argThat(statusInfo -> statusInfo.getStatus() ==
 *       ThingStatus.OFFLINE));
 *       inOrder.verify(callbackMock).getBridge(any());
 *       inOrder.verify(callbackMock).statusUpdated(any(), argThat(statusInfo -> statusInfo.getStatus() ==
 *       ThingStatus.ONLINE));
 *       inOrder.verify(callbackMock, times(2)).getBridge(any()); // TODO
 *       verifyNoMoreInteractions(callbackMock);
 *       }
 *       }
 */
