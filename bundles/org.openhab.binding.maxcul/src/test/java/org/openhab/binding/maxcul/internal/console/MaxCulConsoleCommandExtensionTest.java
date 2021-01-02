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
package org.openhab.binding.maxcul.internal.console;

/**
 * @author Sascha Volkenandt - Initial contribution
 *
 * @RunWith(MockitoJUnitRunner.class)
 *                                    public class MaxCulConsoleCommandExtensionTest {
 * 
 *                                    private static final String BRIDGE_UID = "maxcul:bridge:theBridge";
 *                                    private static final String DEVICE1_UID =
 *                                    "maxcul:heatingThermostat:theFirstHeatingThermostat";
 *                                    private static final String DEVICE2_UID =
 *                                    "maxcul:heatingThermostat:theSecondHeatingThermostat";
 * 
 * @Mock
 *       private ThingRegistry thingRegistryMock;
 * 
 * @Mock
 *       private MaxCulBridgeHandler bridgeHandlerMock;
 * 
 * @Mock
 *       private HeatingThermostatDeviceHandler firstDeviceHandlerMock;
 * @Mock
 *       private HeatingThermostatDeviceHandler secondDeviceHandlerMock;
 * 
 * @Mock
 *       private Console consoleMock;
 * 
 * @InjectMocks
 *              private MaxCulConsoleCommandExtension maxCulConsoleCommandExtensionUnderTest;
 * 
 * @Test
 *       public void nothing_emptyCommand() {
 *       maxCulConsoleCommandExtensionUnderTest.execute(new String[0], consoleMock);
 * 
 *       verifyNoInteractions(thingRegistryMock, consoleMock);
 *       }
 * 
 * @Test
 *       public void usage_unknownCommand() {
 *       maxCulConsoleCommandExtensionUnderTest.execute(new String[]{"nonsense"}, consoleMock);
 * 
 *       verify(consoleMock).println("Unknown command 'nonsense'");
 *       verify(consoleMock).printUsage("maxcul link <bridge> <thing> <thing> - Associates two MAX! devices.");
 *       verify(consoleMock).printUsage("maxcul unlink <bridge> <thing> <thing> - Disassociates two MAX! devices.");
 *       verifyNoMoreInteractions(thingRegistryMock, bridgeHandlerMock, consoleMock);
 *       }
 * 
 * @Test
 *       public void link_ok() {
 *       setUpThings();
 * 
 *       maxCulConsoleCommandExtensionUnderTest.execute(new String[]{"link", BRIDGE_UID, DEVICE1_UID, DEVICE2_UID},
 *       consoleMock);
 * 
 *       verify(thingRegistryMock).get(new ThingUID(BRIDGE_UID));
 *       verify(thingRegistryMock).get(new ThingUID(DEVICE1_UID));
 *       verify(thingRegistryMock).get(new ThingUID(DEVICE2_UID));
 *       verify(bridgeHandlerMock).sendLinkage(eq(true), same(firstDeviceHandlerMock), same(secondDeviceHandlerMock));
 *       verifyNoMoreInteractions(thingRegistryMock, bridgeHandlerMock, consoleMock);
 *       }
 * 
 * @Test
 *       public void unlink_ok() {
 *       setUpThings();
 * 
 *       maxCulConsoleCommandExtensionUnderTest.execute(new String[]{"unlink", BRIDGE_UID, DEVICE1_UID, DEVICE2_UID},
 *       consoleMock);
 * 
 *       verify(thingRegistryMock).get(new ThingUID(BRIDGE_UID));
 *       verify(thingRegistryMock).get(new ThingUID(DEVICE1_UID));
 *       verify(thingRegistryMock).get(new ThingUID(DEVICE2_UID));
 *       verify(bridgeHandlerMock).sendLinkage(eq(false), same(firstDeviceHandlerMock), same(secondDeviceHandlerMock));
 *       verifyNoMoreInteractions(thingRegistryMock, bridgeHandlerMock, consoleMock);
 *       }
 * 
 * @Test
 *       public void link_unknownBridge() {
 *       command_unknownBridge("link");
 *       }
 * 
 * @Test
 *       public void unlink_unknownBridge() {
 *       command_unknownBridge("unlink");
 *       }
 * 
 *       private void command_unknownBridge(String command) {
 *       doReturn(null).when(thingRegistryMock).get(any());
 * 
 *       maxCulConsoleCommandExtensionUnderTest.execute(new String[]{command, BRIDGE_UID, "ababab", "cdcdcd"},
 *       consoleMock);
 * 
 *       verify(thingRegistryMock).get(new ThingUID(BRIDGE_UID));
 *       verify(consoleMock).println("Unknown bridge '" + BRIDGE_UID + "'");
 *       verifyNoMoreInteractions(thingRegistryMock, bridgeHandlerMock, consoleMock);
 *       }
 * 
 *       private void setUpThings() {
 *       setUpThing(THING_TYPE_BRIDGE, "theBridge", bridgeHandlerMock);
 *       setUpThing(THING_TYPE_HEATING_THERMOSTAT, "theFirstHeatingThermostat", firstDeviceHandlerMock);
 *       setUpThing(THING_TYPE_HEATING_THERMOSTAT, "theSecondHeatingThermostat", secondDeviceHandlerMock);
 *       }
 * 
 *       private void setUpThing(ThingTypeUID thingTypeUID, String thingId, ThingHandler thingHandler) {
 *       Thing thing = new ThingImpl(thingTypeUID, thingId);
 *       thing.setHandler(thingHandler);
 *       doReturn(thing).when(thingRegistryMock).get(thing.getUID());
 *       }
 *       }
 */
