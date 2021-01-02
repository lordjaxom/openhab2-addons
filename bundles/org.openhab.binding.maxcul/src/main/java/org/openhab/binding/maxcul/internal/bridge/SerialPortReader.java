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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SerialPortReader} buffers and reads whole lines from the serial input stream. It uses
 * {@link InputStream#available()} as a
 * means to detect available data since {@link java.io.BufferedReader} repeatedly complains "read beyond end-of-file".
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class SerialPortReader {

    private static final String SEPARATOR = "\015\012";

    private final InputStream inputStream;
    private final StringBuilder inputBuffer = new StringBuilder();

    public SerialPortReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public @Nullable String readLine() throws IOException {
        int available;
        while ((available = inputStream.available()) > 0) {
            int count;
            byte[] buffer = new byte[available];
            if ((count = inputStream.read(buffer)) == -1) {
                throw new IOException("Unexpected end-of-file!");
            }
            inputBuffer.append(new String(buffer, 0, count, StandardCharsets.US_ASCII));
        }

        int index = inputBuffer.indexOf(SEPARATOR);
        if (index != -1) {
            String message = inputBuffer.substring(0, index);
            inputBuffer.delete(0, index + SEPARATOR.length());
            return message;
        }
        return null;
    }
}
