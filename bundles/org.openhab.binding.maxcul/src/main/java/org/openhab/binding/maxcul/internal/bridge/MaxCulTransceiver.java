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

import static org.openhab.binding.maxcul.internal.utils.Checks.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.maxcul.internal.command.CulCommand;
import org.openhab.binding.maxcul.internal.command.CulCommandQueue;
import org.openhab.binding.maxcul.internal.command.MoritzCulCommand;
import org.openhab.binding.maxcul.internal.command.VersionCulCommand;
import org.openhab.binding.maxcul.internal.device.HeatingThermostatDeviceHandler;
import org.openhab.binding.maxcul.internal.device.MaxDeviceHandler;
import org.openhab.binding.maxcul.internal.device.ThermostatMode;
import org.openhab.binding.maxcul.internal.message.*;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxCulTransceiver} class handles communication between the MAX! devices and the CUL.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class MaxCulTransceiver {

    private final Logger logger = LoggerFactory.getLogger(MaxCulTransceiver.class);

    private final MaxCulBridgeHandler bridgeHandler;
    private final SerialPortManager serialPortManager;
    private final ScheduledExecutorService scheduler;
    private final CulCommandQueue commandQueue = new CulCommandQueue();

    private @Nullable SerialPort serialPort;
    private @Nullable SerialPortReader serialPortReader;

    MaxCulTransceiver(MaxCulBridgeHandler bridgeHandler, SerialPortManager serialPortManager,
            ScheduledExecutorService scheduler) {
        this.bridgeHandler = bridgeHandler;
        this.serialPortManager = serialPortManager;
        this.scheduler = scheduler;
    }

    /* Public interface methods */

    public void sendTemperatureAndMode(String address, ThermostatMode mode, double temp) {
        logger.debug("Setting temperature for {} to {} °C", address, temp);

        commandQueue.enqueue(new MoritzCulCommand(this, scheduler, //
                new SetTemperatureMoritzMessage(bridgeHandler.getConfiguration().address, address, mode, temp)));
    }

    public void sendTimeInformation(String address) {
        logger.debug("Sending time information to {}", address);

        commandQueue.enqueue(new MoritzCulCommand(this, scheduler, //
                new TimeInformationMoritzMessage(bridgeHandler.getConfiguration().address, address)));
    }

    public void sendPairPong(String address) {
        logger.debug("Sending pair pong to {}", address);

        commandQueue.enqueue(new MoritzCulCommand(this, scheduler, //
                new PairPongMoritzMessage(bridgeHandler.getConfiguration().address, address)));
    }

    public void sendWallThermostatControl(String address, double setTemp, double measuredTemp) {
        commandQueue.enqueue(new MoritzCulCommand(this, scheduler, //
                new WallThermostatControlMoritzMessage(bridgeHandler.getConfiguration().virtualThermostatAddress,
                        address, setTemp, measuredTemp)));
    }

    public void sendLinkage(boolean link, MaxDeviceHandler firstDeviceHandler, MaxDeviceHandler secondDeviceHandler) {
        if (firstDeviceHandler instanceof HeatingThermostatDeviceHandler) {
            queueLinkageCommand(link, (HeatingThermostatDeviceHandler) firstDeviceHandler, secondDeviceHandler);
        }
        if (secondDeviceHandler instanceof HeatingThermostatDeviceHandler) {
            queueLinkageCommand(link, (HeatingThermostatDeviceHandler) secondDeviceHandler, firstDeviceHandler);
        }
    }

    private void queueLinkageCommand(boolean link, HeatingThermostatDeviceHandler deviceHandler,
            MaxDeviceHandler linkDeviceHandler) {
        commandQueue.enqueue(new MoritzCulCommand(this, scheduler, //
                new LinkPartnerMoritzMessage(link, bridgeHandler.getConfiguration().address, deviceHandler.getAddress(),
                        linkDeviceHandler.getAddress(), linkDeviceHandler.getDeviceType())));
    }

    /* Methods used by MaxCulBridgeHandler */

    void connect() {
        MaxCulBridgeConfiguration configuration = bridgeHandler.getConfiguration();

        logger.debug("Connecting to MAX!CUL @ {}...", configuration.port);

        @Nullable
        SerialPortIdentifier serialPortIdentifier = serialPortManager.getIdentifier(configuration.port);
        if (serialPortIdentifier == null) {
            logger.error("Serial port {} is unknown", configuration.port);
            bridgeHandler.configurationError("Serial port " + configuration.port + " is unknown!");
            return;
        }

        try {
            serialPort = serialPortIdentifier.open(bridgeHandler.getThing().getUID().toString(), 2000);
            serialPort.setSerialPortParams(configuration.baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.addEventListener(this::serialEvent);
            serialPort.notifyOnDataAvailable(true);

            serialPortReader = new SerialPortReader(requireNonNull(serialPort.getInputStream()));

            commandQueue.enqueue(new VersionCulCommand(this, scheduler));
        } catch (PortInUseException e) {
            logger.error("Serial port {} is already in use", configuration.port, e);
            bridgeHandler.communicationError("Serial port is already in use!");
        } catch (UnsupportedCommOperationException e) {
            logger.error("Unsupported operation on serial port {}", configuration.port, e);
            bridgeHandler.communicationError("Unsupported operation on serial port!");
        } catch (TooManyListenersException e) {
            logger.error("Cannot attach listener to serial port {}", configuration.port, e);
            bridgeHandler.communicationError("Cannot attach listener to serial port!");
        } catch (IOException e) {
            logger.error("I/O error on serial port {}", configuration.port, e);
            bridgeHandler.communicationError("I/O error on serial port!");
        }
    }

    void dispose() {
        logger.debug("Disconnecting from MAX!CUL");

        commandQueue.clear();

        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
            serialPort = null;
        }
    }

    /* Methods used by CulCommands */

    public void send(String data) {
        requireNonNull(serialPort);

        logger.trace("<<< [RAW] {}", data);

        try {
            byte[] bytes = (data + "\015\012").getBytes(StandardCharsets.US_ASCII);
            OutputStream outputStream = requireNonNull(serialPort.getOutputStream());
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            logger.error("I/O error on serial port {}", bridgeHandler.getConfiguration().port, e);
            bridgeHandler.communicationError("I/O error on serial port!");
        }
    }

    public void error(String message) {
        bridgeHandler.communicationError(message);
    }

    public void version(int version) {
        if (version < 153) {
            logger.error("Unsupported version {} from CUL on serial port {}", version,
                    bridgeHandler.getConfiguration().port);
            error("Unsupported CUL firmware version " + version + "!");
            return;
        }

        logger.debug("Received supported version {} from CUL", version);

        bridgeHandler.online();

        send("X21"); // RSSI
        send("Zr"); // rf mode
        send("Za" + bridgeHandler.getConfiguration().address);
        if (!bridgeHandler.getConfiguration().virtualThermostatAddress.isEmpty()) {
            send("Zw" + bridgeHandler.getConfiguration().virtualThermostatAddress);
        }
    }

    public void advance() {
        commandQueue.advance(true);
    }

    public void postpone(CulCommand command) {
        commandQueue.postpone(command);
    }

    /* Internal methods */

    private void receive(String data) {
        logger.trace(">>> [RAW] {}", data);

        if (commandQueue.receive(data)) {
            return;
        }

        Optional<MoritzMessage.Incoming> optionalMessage = MoritzMessageParser.parse(data);
        if (!optionalMessage.isPresent()) {
            logger.debug("Unhandled (unknown or spurious) message '{}'", data);
            logger.trace("Command queue {}", commandQueue.tracingInfo());
            return;
        }
        MoritzMessage.Incoming message = optionalMessage.get();

        logger.trace(">>> [CKD] {}", message);

        bridgeHandler.dispatch(message);
    }

    private void serialEvent(SerialPortEvent event) {
        requireNonNull(serialPortReader);

        if (event.getEventType() != SerialPortEvent.DATA_AVAILABLE) {
            return;
        }

        try {
            @Nullable
            String line;
            while ((line = serialPortReader.readLine()) != null) {
                receive(line);
            }
        } catch (IOException e) {
            logger.error("I/O error on serial port {}", bridgeHandler.getConfiguration().port, e);
            bridgeHandler.communicationError("I/O error on serial port!");
        }
    }
}
