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
package org.openhab.binding.maxcul.internal.console;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.maxcul.internal.bridge.MaxCulBridgeHandler;
import org.openhab.binding.maxcul.internal.device.MaxDeviceHandler;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MaxCulConsoleCommandExtension} class provides commands to link and unlink MAX! devices via the karaf
 * console.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
@Component(service = ConsoleCommandExtension.class, immediate = true)
@NonNullByDefault
public class MaxCulConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private static final String COMMAND_LINK = "link";
    private static final String COMMAND_UNLINK = "unlink";

    @Reference
    @NonNullByDefault({})
    private ThingRegistry thingRegistry;

    public MaxCulConsoleCommandExtension() {
        super("maxcul", "Interact with the MAX!CUL binding.");
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length == 0) {
            return;
        }

        switch (args[0]) {
            case COMMAND_LINK:
            case COMMAND_UNLINK:
                executeLinkageCommand(args, console);
                break;

            default:
                console.println("Unknown command '" + args[0] + "'");
                printUsage(console);
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(
                buildCommandUsage(COMMAND_LINK + " <bridge> <thing> <thing>", "Associates two MAX! devices."),
                buildCommandUsage(COMMAND_UNLINK + " <bridge> <thing> <thing>", "Disassociates two MAX! devices."));
    }

    private void executeLinkageCommand(String[] args, Console console) {
        if (args.length != 4) {
            printUsage(console);
            return;
        }

        @Nullable
        MaxCulBridgeHandler bridgeHandler = findThingHandlerByThingUID(MaxCulBridgeHandler.class, args[1]);
        if (bridgeHandler == null) {
            console.println("Unknown bridge '" + args[1] + "'");
            return;
        }

        @Nullable
        MaxDeviceHandler firstDeviceHandler = findThingHandlerByThingUID(MaxDeviceHandler.class, args[2]);
        if (firstDeviceHandler == null) {
            console.println("Unknown thing '" + args[2] + "'");
            return;
        }

        @Nullable
        MaxDeviceHandler secondDeviceHandler = findThingHandlerByThingUID(MaxDeviceHandler.class, args[3]);
        if (secondDeviceHandler == null) {
            console.println("Unknown thing '" + args[3] + "'");
            return;
        }

        // TODO bridgeHandler.sendLinkage(args[0].equals(COMMAND_LINK), firstDeviceHandler, secondDeviceHandler);
    }

    @Nullable
    private <T> T findThingHandlerByThingUID(Class<T> handlerClass, String thingUID) {
        @Nullable
        Thing thing = thingRegistry.get(new ThingUID(thingUID));
        if (thing == null) {
            return null;
        }
        @Nullable
        ThingHandler thingHandler = thing.getHandler();
        if (!handlerClass.isInstance(thingHandler)) {
            return null;
        }
        return handlerClass.cast(thingHandler);
    }
}
