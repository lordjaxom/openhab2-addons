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
package org.openhab.binding.maxcul.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MaxCulBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class MaxCulBindingConstants {

    public static final String BINDING_ID = "maxcul";
    public static final String BINDING_PACKAGE = "org.openhab.binding.maxcul";

    // List of all Thing Type UIDs
    // @formatter:off
    public static final ThingTypeUID THING_TYPE_BRIDGE             = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_HEATING_THERMOSTAT = new ThingTypeUID(BINDING_ID, "heatingThermostat");
    public static final ThingTypeUID THING_TYPE_VIRTUAL_THERMOSTAT = new ThingTypeUID(BINDING_ID, "virtualThermostat");
    // @formatter:on

    // List of all Channel ids
    // @formatter:off
    public static final String CHANNEL_ID_VALVE         = "valve";
    public static final String CHANNEL_ID_BATTERY_LOW   = "batteryLow";
    public static final String CHANNEL_ID_MODE          = "mode";
    public static final String CHANNEL_ID_ACTUAL_TEMP   = "actualTemp";
    public static final String CHANNEL_ID_SET_TEMP      = "setTemp";
    public static final String CHANNEL_ID_LOCKED        = "locked";
    public static final String CHANNEL_ID_RF_ERROR      = "rfError";
    public static final String CHANNEL_ID_MEASURED_TEMP = "measuredTemp";
    // @formatter:on

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(THING_TYPE_BRIDGE, THING_TYPE_HEATING_THERMOSTAT, THING_TYPE_VIRTUAL_THERMOSTAT)));

    // Temperature Constants
    // @formatter:off
    public static final double TEMPERATURE_OFF =  4.5;
    public static final double TEMPERATURE_ON  = 30.5;
    // @formatter:on

    private MaxCulBindingConstants() {
    }
}
