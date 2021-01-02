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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MaxCulBridgeConfiguration} class contains fields mapping the bridge's configuration parameters.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class MaxCulBridgeConfiguration {

    /**
     * The serial port of the MAX!CUL gateway.
     */
    public String port = "";

    /**
     * The baudrate of the serial port.
     */
    public int baudRate = 0;

    /**
     * The RF address of the MAX!CUL (must not be in use by any other MAX! device).
     */
    public String address = "";

    /**
     * The RF address of the Virtual Thermostat (clear to disable).
     */
    public String virtualThermostatAddress = "";

    /**
     * The RF address of the Virtual Shutter Contact (clear to disable).
     */
    public String virtualShutterContactAddress = "";
}
