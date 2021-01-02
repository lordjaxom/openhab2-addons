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

import java.util.Objects;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Checks} class provides checks used repeatedly throughout the binding.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class Checks {

    private static final Pattern DEVICE_ADDRESS = Pattern.compile("[0-9A-Fa-f]{6}");

    public static boolean isDeviceAddress(String value) {
        return DEVICE_ADDRESS.matcher(value).matches();
    }

    /**
     * This method solely exists to silence IntelliJ IDEA's warning "argument might be null" when calling
     * Objects.requireNonNull.
     *
     * @see Objects#requireNonNull(Object)
     */
    public static <T> T requireNonNull(@Nullable T value) {
        // noinspection ConstantConditions
        return Objects.requireNonNull(value);
    }

    private Checks() {
    }
}
