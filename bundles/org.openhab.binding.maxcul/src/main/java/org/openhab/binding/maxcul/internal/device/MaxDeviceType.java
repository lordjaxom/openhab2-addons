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

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MaxDeviceType} enumeration represents the type of a physical MAX! device.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public enum MaxDeviceType {

    CUBE(0, false),
    HEATING_THERMOSTAT(1, true),
    HEATING_THERMOSTAT_PLUS(2, true),
    WALL_MOUNTED_THERMOSTAT(3, false),
    SHUTTER_CONTACT(4, false),
    PUSH_BUTTON(5, false);

    public static MaxDeviceType findById(int id) {
        return Arrays.stream(values()) //
                .filter(value -> value.id == id) //
                .findFirst() //
                .orElseThrow(() -> new IllegalArgumentException(String.valueOf(id)));
    }

    private final int id;
    private final boolean heatingThermostat;

    MaxDeviceType(int id, boolean heatingThermostat) {
        this.id = id;
        this.heatingThermostat = heatingThermostat;
    }

    public int getId() {
        return id;
    }

    public boolean isHeatingThermostat() {
        return heatingThermostat;
    }
}
