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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MaxDeviceConfiguration} class contains fields mapping the device's configuration parameters.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class MaxDeviceConfiguration {

    /**
     * The RF address of this device.
     */
    public String address = "";

    /**
     * The room name is used for linking devices that are in the same room.
     */
    public String roomName = "";
}
