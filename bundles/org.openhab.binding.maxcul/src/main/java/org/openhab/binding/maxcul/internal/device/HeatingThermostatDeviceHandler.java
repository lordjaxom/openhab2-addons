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

import static org.eclipse.smarthome.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.CHANNEL_ID_ACTUAL_TEMP;
import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.CHANNEL_ID_BATTERY_LOW;
import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.CHANNEL_ID_LOCKED;
import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.CHANNEL_ID_MODE;
import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.CHANNEL_ID_RF_ERROR;
import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.CHANNEL_ID_SET_TEMP;
import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.CHANNEL_ID_VALVE;
import static org.openhab.binding.maxcul.internal.utils.Checks.isDeviceAddress;
import static org.openhab.binding.maxcul.internal.utils.Checks.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.maxcul.internal.bridge.MaxCulBridgeHandler;
import org.openhab.binding.maxcul.internal.message.AckMoritzMessage;
import org.openhab.binding.maxcul.internal.message.MoritzMessage;
import org.openhab.binding.maxcul.internal.message.SetTemperatureMoritzMessage;
import org.openhab.binding.maxcul.internal.message.ThermostatSettings;
import org.openhab.binding.maxcul.internal.message.ThermostatStateMoritzMessage;
import org.openhab.binding.maxcul.internal.utils.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeatingThermostatDeviceHandler} class represents a specific MAX! device.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class HeatingThermostatDeviceHandler extends BaseThingHandler implements MaxDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(HeatingThermostatDeviceHandler.class);

    private final Storage<Object> storage;

    private MaxDeviceConfiguration configuration = new MaxDeviceConfiguration();

    private @Nullable MaxCulBridgeHandler bridgeHandler;

    private State state = new State();

    public HeatingThermostatDeviceHandler(Thing thing, Storage<Object> storage) {
        super(thing);
        this.storage = storage;
    }

    @Override
    public MaxDeviceType getDeviceType() {
        return MaxDeviceType.HEATING_THERMOSTAT;
    }

    @Override
    public String getAddress() {
        return configuration.address;
    }

    @Override
    public String getRoomName() {
        return configuration.roomName;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MAX!CUL heating thermostat handler");

        configuration = getConfigAs(MaxDeviceConfiguration.class);
        bridgeHandler = requireNonNull((MaxCulBridgeHandler) requireNonNull(getBridge()).getHandler());

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING);

        if (getBridge() == null) {
            logger.error("Bridge for MAX! heating thermostat is missing");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge for MAX! heating thermostat is missing!");
            return;
        }

        if (!isDeviceAddress(configuration.address)) {
            logger.error("Address must be 6 hex digits, was '{}'", configuration.address);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Property address must be 6 hexadecimal digits!");
            return;
        }

        updateStatus(ThingStatus.ONLINE);

        loadFromStorage();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();
        if (command instanceof RefreshType) {
            // noinspection IfCanBeSwitch
            if (CHANNEL_ID_MODE.equals(id)) {
                updateState(CHANNEL_ID_MODE, new StringType(state.mode.name()));
            } else if (CHANNEL_ID_SET_TEMP.equals(id)) {
                updateState(CHANNEL_ID_SET_TEMP, new QuantityType<>(state.setTemp, CELSIUS));
            } else if (CHANNEL_ID_ACTUAL_TEMP.equals(id)) {
                updateState(CHANNEL_ID_ACTUAL_TEMP, new QuantityType<>(state.actualTemp, CELSIUS));
            } else if (CHANNEL_ID_VALVE.equals(id)) {
                updateState(CHANNEL_ID_VALVE, new DecimalType(state.valve));
            } else if (CHANNEL_ID_BATTERY_LOW.equals(id)) {
                updateState(CHANNEL_ID_BATTERY_LOW, OnOffType.from(state.batteryLow));
            } else if (CHANNEL_ID_RF_ERROR.equals(id)) {
                updateState(CHANNEL_ID_RF_ERROR, OnOffType.from(state.rfError));
            } else if (CHANNEL_ID_LOCKED.equals(id)) {
                updateState(CHANNEL_ID_LOCKED, state.locked ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            }
            return;
        }

        if (CHANNEL_ID_MODE.equals(id)) {
            handleModeCommand(command);
        } else if (CHANNEL_ID_SET_TEMP.equals(id)) {
            handleSetTempCommand(command);
        }
    }

    @Override
    public void dispatch(MoritzMessage.Incoming message) {
        if (!message.getSourceAddress().equalsIgnoreCase(configuration.address)) {
            return;
        }

        if (message instanceof AckMoritzMessage) {
            AckMoritzMessage ackMessage = (AckMoritzMessage) message;
            updateSettings(ackMessage.getSettings());
        } else if (message instanceof ThermostatStateMoritzMessage) {
            ThermostatStateMoritzMessage thermostatStateMessage = (ThermostatStateMoritzMessage) message;
            updateSettings(thermostatStateMessage.getSettings());
            updateActualTemp(thermostatStateMessage.getMeasuredTemperature());
        } else if (message instanceof SetTemperatureMoritzMessage) {
            SetTemperatureMoritzMessage setTemperatureMessage = (SetTemperatureMoritzMessage) message;
            logger.debug("Received SetTemperature for {} with mode {}, desiredTemp {}", configuration.address,
                    setTemperatureMessage.getMode(), setTemperatureMessage.getDesiredTemperature());
        }
    }

    private void handleModeCommand(Command command) {
        Types.toThermostatMode(command).ifPresent(newMode -> {
            if (newMode != state.mode) {
                state.mode = newMode;
                saveToStorage();

                requireNonNull(bridgeHandler).getTransceiver().sendTemperatureAndMode(configuration.address, state.mode,
                        state.setTemp);
            }
        });
    }

    private void handleSetTempCommand(Command command) {
        Types.toTemperature(command).ifPresent(newSetTemp -> {
            if (state.setTemp != newSetTemp) {
                state.setTemp = newSetTemp;
                saveToStorage();

                requireNonNull(bridgeHandler).getTransceiver().sendTemperatureAndMode(configuration.address, state.mode,
                        state.setTemp);
            }
        });
    }

    private void updateSettings(ThermostatSettings settings) {
        logger.debug("Updating settings for {} with {}", configuration.address, settings);

        state.mode = settings.getMode();
        state.setTemp = settings.getDesiredTemperature();
        state.valve = settings.getValve();
        state.batteryLow = settings.isBatteryLow();
        state.rfError = settings.isRfError();
        state.locked = settings.isLocked();
        saveToStorage();

        updateState(CHANNEL_ID_VALVE, new DecimalType(state.valve));
        updateState(CHANNEL_ID_BATTERY_LOW, OnOffType.from(state.batteryLow));
        updateState(CHANNEL_ID_MODE, new StringType(state.mode.name()));
        updateState(CHANNEL_ID_SET_TEMP, new QuantityType<>(state.setTemp, CELSIUS));
        updateState(CHANNEL_ID_RF_ERROR, OnOffType.from(state.rfError));
        updateState(CHANNEL_ID_LOCKED, state.locked ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
    }

    private void updateActualTemp(double actualTemp) {
        logger.debug("Updating actual temperature {} for {}", actualTemp, configuration.address);

        state.actualTemp = actualTemp;
        saveToStorage();

        updateState(CHANNEL_ID_ACTUAL_TEMP, new QuantityType<>(state.actualTemp, CELSIUS));
    }

    private void loadFromStorage() {
        @Nullable
        State loadedState = (State) storage.get(getThing().getUID().toString());
        if (loadedState != null) {
            state = loadedState;
        }
    }

    private void saveToStorage() {
        storage.put(getThing().getUID().toString(), state);
    }

    public static class State {

        public ThermostatMode mode = ThermostatMode.MANUAL;
        public double setTemp = 20.0;
        public double actualTemp = 20.0;
        public int valve = 0;
        public boolean batteryLow = false;
        public boolean rfError = false;
        public boolean locked = false;
    }
}
