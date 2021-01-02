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
package org.openhab.binding.maxcul.internal.utils;

import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.TEMPERATURE_OFF;
import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.TEMPERATURE_ON;
import static org.openhab.binding.maxcul.internal.utils.Checks.requireNonNull;

import java.util.Optional;
import java.util.OptionalDouble;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.maxcul.internal.device.ThermostatMode;

/**
 * The {@link Types} class provides common {@link org.eclipse.smarthome.core.types.Type} conversions.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class Types {

    public static OptionalDouble toTemperature(Command command) {
        if (command instanceof QuantityType) {
            // noinspection NullableProblems
            QuantityType<?> quantityType = (QuantityType<?>) command;
            return OptionalDouble.of(requireNonNull(quantityType.toUnit(SIUnits.CELSIUS)).doubleValue());
        }
        if (command instanceof OnOffType) {
            OnOffType onOffType = (OnOffType) command;
            return OptionalDouble.of(onOffType == OnOffType.ON ? TEMPERATURE_ON : TEMPERATURE_OFF);
        }
        return OptionalDouble.empty();
    }

    public static Optional<ThermostatMode> toThermostatMode(Command command) {
        if (command instanceof StringType) {
            return Optional.of(ThermostatMode.valueOf(command.toString()));
        }
        return Optional.empty();
    }

    private Types() {
    }
}
