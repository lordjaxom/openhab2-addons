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
 *                                    public class MaxCulPhysicalDeviceHandlerTest {
 * 
 *                                    private final ThingImpl thing = new ThingImpl(THING_TYPE_HEATING_THERMOSTAT,
 *                                    "theThing");
 * 
 *                                    private HeatingThermostatDeviceHandler deviceHandlerUnderTest;
 * 
 * @Before
 *         public void setUp() {
 *         Configuration configuration = new Configuration();
 *         configuration.put("rfAddress", "ababab");
 *         thing.setConfiguration(configuration);
 * 
 *         deviceHandlerUnderTest = new HeatingThermostatDeviceHandler(thing);
 *         }
 * 
 * @Test
 *       public void dispatch_ignoresCase() {
 *       MoritzMessage.Incoming message = mock(ThermostatStateMoritzMessage.class);
 *       doReturn("ABABAB").when(message).getSourceAddress();
 * 
 *       deviceHandlerUnderTest.dispatch(message);
 * 
 *       verify(message).dispatch(same(deviceHandlerUnderTest));
 *       }
 *       }
 */
