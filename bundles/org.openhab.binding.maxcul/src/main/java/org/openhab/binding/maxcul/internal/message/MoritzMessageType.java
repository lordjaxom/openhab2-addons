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
package org.openhab.binding.maxcul.internal.message;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MoritzMessageType} enumeration represents the type of a MORITZ message.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public enum MoritzMessageType {

    PAIR_PING(0x00, PairPingMoritzMessage::parse),
    PAIR_PONG(0x01),
    ACK(0x02, AckMoritzMessage::parse),
    TIME_INFORMATION(0x03, TimeInformationMoritzMessage::parse),

    ConfigWeekProfile(0x10),
    ConfigTemperatures(0x11), // like eco/comfort etc
    ConfigValve(0x12),

    ADD_LINK_PARTNER(0x20),
    REMOVE_LINK_PARTNER(0x21),
    SetGroupId(0x22),
    RemoveGroupId(0x23),

    ShutterContactState(0x30),

    SET_TEMPERATURE(0x40, SetTemperatureMoritzMessage::parse), // to thermostat
    WALL_THERMOSTAT_CONTROL(0x42), // by WallMountedThermostat
    // Sending this without payload to thermostat sets desiredTemperature to the comfort/eco temperature
    // We don't use it, we just do SetTemperature
    SetComfortTemperature(0x43),
    SetEcoTemperature(0x44),

    PushButtonState(0x50),

    THERMOSTAT_STATE(0x60, ThermostatStateMoritzMessage::parse), // by HeatingThermostat

    WallThermostatState(0x70),

    SetDisplayActualTemperature(0x82),

    WakeUp(0xF1),
    Reset(0xF0);

    public static MoritzMessageType findById(int id) {
        return Arrays.stream(values()) //
                .filter(type -> type.id == id) //
                .findFirst() //
                .orElseThrow(() -> new IllegalArgumentException(String.valueOf(id)));
    }

    private final int id;
    private final @Nullable Parser parser;

    MoritzMessageType(int id, @Nullable Parser parser) {
        this.id = id;
        this.parser = parser;
    }

    MoritzMessageType(int id) {
        this(id, null);
    }

    public int getId() {
        return id;
    }

    public Optional<MoritzMessage.Incoming> parse(int messageId, String sourceAddress, String destAddress,
            String payload) {
        return parser != null //
                ? Optional.of(parser.parse(messageId, sourceAddress, destAddress, payload)) //
                : Optional.empty();
    }

    @FunctionalInterface
    private interface Parser {

        MoritzMessage.Incoming parse(int messageId, String sourceAddress, String destAddress, String payload);
    }
}
