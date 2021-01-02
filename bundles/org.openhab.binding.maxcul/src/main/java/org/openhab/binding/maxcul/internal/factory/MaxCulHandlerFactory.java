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
package org.openhab.binding.maxcul.internal.factory;

import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.BINDING_PACKAGE;
import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.THING_TYPE_BRIDGE;
import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.THING_TYPE_HEATING_THERMOSTAT;
import static org.openhab.binding.maxcul.internal.MaxCulBindingConstants.THING_TYPE_VIRTUAL_THERMOSTAT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.maxcul.internal.bridge.MaxCulBridgeHandler;
import org.openhab.binding.maxcul.internal.device.HeatingThermostatDeviceHandler;
import org.openhab.binding.maxcul.internal.device.MaxDeviceHandler;
import org.openhab.binding.maxcul.internal.device.VirtualThermostatMaxDeviceHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MaxCulHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@Component(configurationPid = "binding.maxcul", service = ThingHandlerFactory.class)
@NonNullByDefault
public class MaxCulHandlerFactory extends BaseThingHandlerFactory {

    private final SerialPortManager serialPortManager;
    private final Storage<Object> storage;

    @Activate
    public MaxCulHandlerFactory(@Reference SerialPortManager serialPortManager,
            @Reference StorageService storageService) {
        this.serialPortManager = serialPortManager;
        storage = storageService.getStorage(BINDING_PACKAGE, MaxDeviceHandler.class.getClassLoader());
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new MaxCulBridgeHandler((Bridge) thing, serialPortManager);
        }
        if (THING_TYPE_HEATING_THERMOSTAT.equals(thingTypeUID)) {
            return new HeatingThermostatDeviceHandler(thing, storage);
        }
        if (THING_TYPE_VIRTUAL_THERMOSTAT.equals(thingTypeUID)) {
            return new VirtualThermostatMaxDeviceHandler(thing, storage);
        }
        return null;
    }
}
