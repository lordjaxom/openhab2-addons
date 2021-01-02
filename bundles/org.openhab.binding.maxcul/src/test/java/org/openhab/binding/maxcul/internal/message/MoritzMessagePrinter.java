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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.openhab.binding.maxcul.internal.device.ThermostatMode;

/**
 * @author Sascha Volkenandt - Initial contribution
 */
public class MoritzMessagePrinter {

    private static int lineNo = 1;
    private static int parsed = 0;
    private static String timestamp = "";

    public static void main(String[] args) throws IOException {
        try (BufferedReader input = Files.newBufferedReader(Paths.get(args[0]))) {
            String line;
            while ((line = input.readLine()) != null) {
                parse(line);
                ++lineNo;
            }
        }
    }

    private static void parse(String line) {
        if (line.length() < 20) {
            return;
        }
        timestamp = line.substring(0, 19);

        String[] parts = line.split("\\s+");
        if (parts.length == 7 && parts[3].equals("CUL_Parse:")) {
            parseIncoming(parts[5]);
        } else if (parts.length == 6 && parts[4].equals("sending")) {
            parseOutgoing(parts[5]);
        } else {
            return;
        }
        if (++parsed == 1000) {
            System.out.println("--MARK--");
        }
    }

    private static void parseOutgoing(String message) {
        if (!message.startsWith("Zs")) {
            System.out.println("sent unknown: " + message);
            return;
        }
        message = message.substring(2).toUpperCase();
        int id = Integer.parseInt(message.substring(2, 4), 16);
        int flags = Integer.parseInt(message.substring(4, 6), 16);
        MoritzMessageType type = MoritzMessageType.findById(Integer.parseInt(message.substring(6, 8), 16));
        long src = Long.parseLong(message.substring(8, 14), 16);
        long dst = Long.parseLong(message.substring(14, 20), 16);
        String payload = message.substring(22);
        System.out.printf("%s sent id: %03d flags: %02X type: [%25s] src: %06X dst: %06X %s%n", timestamp, id, flags,
                type, src, dst, parsePayload(type, payload));
    }

    private static void parseIncoming(String message) {
        if (!message.startsWith("Z") || message.length() < 23) {
            System.out.println("recv unknown: " + message + " in line " + lineNo);
            return;
        }
        message = message.substring(1, message.length() - 2);
        int id = Integer.parseInt(message.substring(2, 4), 16);
        int flags = Integer.parseInt(message.substring(4, 6), 16);
        MoritzMessageType type = MoritzMessageType.findById(Integer.parseInt(message.substring(6, 8), 16));
        long src = Long.parseLong(message.substring(8, 14), 16);
        long dst = Long.parseLong(message.substring(14, 20), 16);
        String payload = message.length() > 22 ? message.substring(22) : "";
        System.out.printf("%s recv id: %03d flags: %02X type: [%25s] src: %06X dst: %06X %s%n", timestamp, id, flags,
                type, src, dst, parsePayload(type, payload));
    }

    private static String parsePayload(MoritzMessageType type, String payload) {
        if (type == MoritzMessageType.WALL_THERMOSTAT_CONTROL) {
            if (payload.length() < 4) {
                return "payload invalid";
            }
            int data = Integer.parseInt(payload, 16);
            double desired = ((data >> 8) & 0x7f) / 2.0;
            double measured = (((data >> 7) & 0x100) | (data & 0xff)) / 10.0;
            return String.format("desired %.1f measured %.1f", desired, measured);
        }
        if (type == MoritzMessageType.ACK) {
            if (payload.length() < 2) {
                return "payload invalid";
            }
            boolean ok = (Integer.parseInt(payload.substring(0, 2), 16) & 0x80) == 0;
            if (payload.length() == 2) {
                return String.format("%s", ok ? "ok" : "nok");
            }
            ThermostatSettings settings = ThermostatSettings.parse(payload.substring(2));
            return String.format("%s mode %s desired %.1f%s", ok ? "ok" : "nok", settings.getMode(),
                    settings.getDesiredTemperature(), settings.isRfError() ? "rferror" : "");
        }
        if (type == MoritzMessageType.THERMOSTAT_STATE) {
            if (payload.length() < 10) {
                return "payload invalid";
            }
            ThermostatSettings settings = ThermostatSettings.parse(payload);
            double measured = (Integer.parseInt(payload.substring(6, 10), 16) & 0x1ff) / 10.0;
            return String.format("mode %s desired %.1f measured %.1f%s", settings.getMode(),
                    settings.getDesiredTemperature(), measured, settings.isRfError() ? "rferror" : "");
        }
        if (type == MoritzMessageType.SET_TEMPERATURE) {
            if (payload.length() < 2) {
                return "payload invalid";
            }
            int data = Integer.parseInt(payload.substring(0, 2), 16);
            ThermostatMode mode = ThermostatMode.findById((data >> 6) & 0x03);
            double desired = (data & 0x2f) / 2.0;
            return String.format("mode %s desired %.1f", mode, desired);
        }
        return "payload " + payload;
    }
}
