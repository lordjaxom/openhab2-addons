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
 * The {@link ThermostatMode} enumeration represents the mode of a thermostat.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public enum ThermostatMode {

    AUTOMATIC(0),
    MANUAL(1),
    BOOST(2),
    VACATION(3);

    public static ThermostatMode findById(int id) {
        return Arrays.stream(values()).filter(type -> type.id == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.valueOf(id)));
    }

    private final int id;

    ThermostatMode(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
