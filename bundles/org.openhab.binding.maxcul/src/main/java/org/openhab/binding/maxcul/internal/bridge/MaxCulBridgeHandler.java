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

import static org.openhab.binding.maxcul.internal.utils.Checks.isDeviceAddress;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.maxcul.internal.device.MaxDeviceHandler;
import org.openhab.binding.maxcul.internal.message.MoritzMessage;
import org.openhab.binding.maxcul.internal.message.PairPingMoritzMessage;
import org.openhab.binding.maxcul.internal.message.TimeInformationMoritzMessage;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxCulBridgeHandler} class manages interaction between things, physical devices and the CUL.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class MaxCulBridgeHandler extends BaseBridgeHandler {

    private static final Duration MAX_TIME_INFORMATION_DIFFERENCE = Duration.ofSeconds(10);

    private final Logger logger = LoggerFactory.getLogger(MaxCulBridgeHandler.class);

    private final MaxCulTransceiver transceiver;

    private MaxCulBridgeConfiguration configuration = new MaxCulBridgeConfiguration();

    private @Nullable ScheduledFuture<?> connectFuture;

    public MaxCulBridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        transceiver = new MaxCulTransceiver(this, serialPortManager, scheduler);
    }

    public MaxCulTransceiver getTransceiver() {
        return transceiver;
    }

    public MaxCulBridgeConfiguration getConfiguration() {
        return configuration;
    }

    public void sendVirtualThermostat(String roomName, double setTemp, double measuredTemp) {
        logger.debug("Sending desired {} °C, measured {} °C to heating thermostats in room {}", setTemp, measuredTemp,
                roomName);

        maxDeviceHandlers() //
                .filter(handler -> heatingThermostatInRoom(handler, roomName)) //
                .forEach(handler -> transceiver.sendWallThermostatControl(handler.getAddress(), setTemp, measuredTemp));
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MAX!CUL bridge handler for {}", getThing().getUID());

        configuration = getConfigAs(MaxCulBridgeConfiguration.class);

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Trying to connect to MAX!CUL...");

        if (!isDeviceAddress(configuration.address)) {
            logger.error("Address must be 6 hex digits, was '{}'", configuration.address);
            configurationError("Property address must be 6 hexadecimal digits!");
            return;
        }
        if (!configuration.virtualThermostatAddress.isEmpty()
                && !isDeviceAddress(configuration.virtualThermostatAddress)) {
            logger.error("VirtualThermostatAddress must be 6 hex digits or empty, was '{}'",
                    configuration.virtualThermostatAddress);
            configurationError("Property virtualThermostatAddress must be 6 hexadecimal digits or empty!");
            return;
        }
        if (!configuration.virtualShutterContactAddress.isEmpty()
                && !isDeviceAddress(configuration.virtualShutterContactAddress)) {
            logger.error("VirtualShutterContactAddress must be 6 hex digits or empty, was '{}'",
                    configuration.virtualThermostatAddress);
            configurationError("Property virtualThermostatAddress must be 6 hexadecimal digits or empty!");
            return;
        }

        connectFuture = scheduler.scheduleWithFixedDelay(this::connectIfOffline, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing MAX!CUL bridge handler.");

        if (connectFuture != null) {
            connectFuture.cancel(true);
            connectFuture = null;
        }
        transceiver.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands to handle
    }

    void online() {
        updateStatus(ThingStatus.ONLINE);
    }

    void configurationError(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
    }

    void communicationError(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        transceiver.dispose();
    }

    void dispatch(MoritzMessage.Incoming message) {
        if (message instanceof PairPingMoritzMessage) {
            handlePairPing((PairPingMoritzMessage) message);
        } else if (message instanceof TimeInformationMoritzMessage) {
            handleTimeInformation((TimeInformationMoritzMessage) message);
        }

        maxDeviceHandlers().forEach(handler -> handler.dispatch(message));
    }

    private void connectIfOffline() {
        if (thing.getStatus() != ThingStatus.ONLINE) {
            transceiver.connect();
        }
    }

    private void handlePairPing(PairPingMoritzMessage message) {
        if (message.getDestAddress().equals(configuration.address) && knownDevice(message.getSourceAddress())) {
            logger.debug("Handling pair ping for {}", message.getSourceAddress());
            transceiver.sendPairPong(message.getSourceAddress());
        } else if (message.getDestAddress().equals("000000")) {
            logger.error("Received pairing request but pairing mode is not implemented yet");
        }
    }

    private void handleTimeInformation(TimeInformationMoritzMessage message) {
        if (message.getDestAddress().equals(configuration.address) && knownDevice(message.getSourceAddress())) {
            Duration difference = Duration.between(message.getDateTime(), LocalDateTime.now());
            if (difference.compareTo(MAX_TIME_INFORMATION_DIFFERENCE) <= 0) {
                return;
            }

            logger.debug("Time information for {} is {} seconds out of sync, sending correct information",
                    message.getSourceAddress(), difference.getSeconds());

            transceiver.sendTimeInformation(message.getSourceAddress());
        }
    }

    private Stream<MaxDeviceHandler> maxDeviceHandlers() {
        return getThing().getThings().stream() //
                .map(Thing::getHandler) //
                .filter(MaxDeviceHandler.class::isInstance) //
                .map(MaxDeviceHandler.class::cast);
    }

    private boolean knownDevice(String address) {
        return maxDeviceHandlers().anyMatch(handler -> handler.getAddress().equalsIgnoreCase(address));
    }

    private static boolean heatingThermostatInRoom(MaxDeviceHandler handler, String roomName) {
        return handler.getDeviceType().isHeatingThermostat() && roomName.equals(handler.getRoomName());
    }
}
