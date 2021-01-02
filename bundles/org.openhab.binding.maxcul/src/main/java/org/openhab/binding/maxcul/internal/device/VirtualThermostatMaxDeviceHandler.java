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
import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.CHANNEL_ID_MEASURED_TEMP;
import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.CHANNEL_ID_SET_TEMP;
import static org.openhab.binding.maxcul.internal.utils.Checks.requireNonNull;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.maxcul.internal.bridge.MaxCulBridgeHandler;
import org.openhab.binding.maxcul.internal.message.MoritzMessage;
import org.openhab.binding.maxcul.internal.utils.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VirtualThermostatMaxDeviceHandler} class handles a faked wall thermostat.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class VirtualThermostatMaxDeviceHandler extends BaseThingHandler implements MaxDeviceHandler {

    private static final Duration TIMEOUT_UPDATE = Duration.ofMinutes(10);

    private final Logger logger = LoggerFactory.getLogger(VirtualThermostatMaxDeviceHandler.class);

    private final Storage<Object> storage;

    private MaxDeviceConfiguration configuration = new MaxDeviceConfiguration();

    private @Nullable MaxCulBridgeHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> updateFuture;

    private State state = new State();

    public VirtualThermostatMaxDeviceHandler(Thing thing, Storage<Object> storage) {
        super(thing);
        this.storage = storage;
    }

    @Override
    public MaxDeviceType getDeviceType() {
        return MaxDeviceType.WALL_MOUNTED_THERMOSTAT;
    }

    @Override
    public String getAddress() {
        return requireNonNull(bridgeHandler).getConfiguration().virtualThermostatAddress;
    }

    @Override
    public String getRoomName() {
        return configuration.roomName;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MAX!CUL virtual thermostat handler");

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING);

        if (getBridge() == null) {
            logger.error("Bridge for MAX! virtual thermostat is missing");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge for MAX! virtual thermostat is missing!");
            return;
        }

        configuration = getConfigAs(MaxDeviceConfiguration.class);
        bridgeHandler = requireNonNull((MaxCulBridgeHandler) requireNonNull(getBridge()).getHandler());

        if (bridgeHandler.getConfiguration().virtualThermostatAddress.isEmpty()) {
            logger.error("VirtualWallThermostatAddress must be set in bridge configuration");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Property virtualThermostatAddress must be set in " + "bridge configuration!");
            return;
        }

        updateStatus(ThingStatus.ONLINE);

        loadFromStorage();
        scheduleIfBridgeOnline();
    }

    @Override
    public void dispose() {
        unschedule();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        scheduleIfBridgeOnline();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();
        if (command instanceof RefreshType) {
            if (CHANNEL_ID_MEASURED_TEMP.equals(id)) {
                updateState(CHANNEL_ID_MEASURED_TEMP, new QuantityType<>(state.measuredTemp, CELSIUS));
            } else if (CHANNEL_ID_SET_TEMP.equals(id)) {
                updateState(CHANNEL_ID_SET_TEMP, new QuantityType<>(state.setTemp, CELSIUS));
            }
            return;
        }

        if (CHANNEL_ID_MEASURED_TEMP.equals(id)) {
            handleMeasuredTempCommand(command);
        } else if (CHANNEL_ID_SET_TEMP.equals(id)) {
            handleSetTemperature(command);
        }
    }

    private synchronized void scheduleIfBridgeOnline() {
        MaxCulBridgeHandler safeBridgeHandler = requireNonNull(bridgeHandler);
        if (updateFuture == null && safeBridgeHandler.getThing().getStatus() == ThingStatus.ONLINE) {
            updateFuture = scheduler.schedule(this::update, 0, TimeUnit.MILLISECONDS);
        } else if (safeBridgeHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            unschedule();
        }
    }

    private synchronized void unschedule() {
        if (updateFuture != null) {
            updateFuture.cancel(true);
            updateFuture = null;
        }
    }

    private synchronized void update() {
        logger.debug("Sending update from virtual thermostat to heating thermostats");

        unschedule();

        requireNonNull(bridgeHandler).sendVirtualThermostat(getRoomName(), state.setTemp, state.measuredTemp);

        state.lastSentTemp = state.measuredTemp;
        updateFuture = scheduler.schedule(this::update, TIMEOUT_UPDATE.toMillis(), TimeUnit.MILLISECONDS);
    }

    private synchronized void handleMeasuredTempCommand(Command command) {
        if (command instanceof RefreshType) {
            updateState(CHANNEL_ID_MEASURED_TEMP, new QuantityType<>(state.measuredTemp, CELSIUS));
            return;
        }

        Types.toTemperature(command).ifPresent(newMeasuredTemp -> {
            if (state.measuredTemp != newMeasuredTemp) {
                state.measuredTemp = newMeasuredTemp;
                saveToStorage();

                if (Math.abs(state.lastSentTemp - state.measuredTemp) > 1.0) {
                    update();
                }
            }
        });
    }

    private void handleSetTemperature(Command command) {
        if (command instanceof RefreshType) {
            updateState(CHANNEL_ID_SET_TEMP, new QuantityType<>(state.setTemp, CELSIUS));
            return;
        }

        Types.toTemperature(command).ifPresent(newSetTemp -> {
            if (state.setTemp != newSetTemp) {
                state.setTemp = newSetTemp;
                saveToStorage();

                update();
            }
        });
    }

    private void loadFromStorage() {
        Optional.ofNullable((State) storage.get(getThing().getUID().toString()))
                .ifPresent(newState -> state = newState);
    }

    private void saveToStorage() {
        storage.put(getThing().getUID().toString(), state);
    }

    @Override
    public void dispatch(MoritzMessage.Incoming moritzMessage) {
        // ignore
    }

    public static class State {

        public double measuredTemp = 20.0;
        public double lastSentTemp = 20.0;
        public double setTemp = 20.0;
    }
}
