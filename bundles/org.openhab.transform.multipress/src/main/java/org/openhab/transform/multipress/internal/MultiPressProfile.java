/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.transform.multipress.internal;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.TriggerProfile;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Profile that implements the multiPress profile
 *
 * @author Sascha Volkenandt - initial contribution
 *
 */
@NonNullByDefault
public class MultiPressProfile implements TriggerProfile {

    public static final ProfileTypeUID PROFILE_TYPE_UID = new ProfileTypeUID("multiPress", "multiPress");

    private final Logger logger = LoggerFactory.getLogger(MultiPressProfile.class);

    private final ProfileCallback callback;
    private final ProfileContext context;

    private final MultiPressConfiguration config;

    private @Nullable Future<?> future;
    private boolean state;
    private int clicks;

    public MultiPressProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        this.context = context;

        logger.info("Initializing multiPress profile with configuration {}", context.getConfiguration());

        config = context.getConfiguration().as(MultiPressConfiguration.class);
    }

    @Override
    public void onTriggerFromHandler(String event) {
        if (!stateChanged(event)) {
            return;
        }

        cancel();

        if (state) {
            logger.debug("Arming {} ms timer for multiPress profile", config.longDelay);
            future = context.getExecutorService().schedule(this::longPress, config.longDelay, TimeUnit.MILLISECONDS);
        } else if (clicks != -1) {
            logger.debug("Arming {} ms timer for multiPress profile", config.shortDelay);
            ++clicks;
            future = context.getExecutorService().schedule(this::clicks, config.shortDelay, TimeUnit.MILLISECONDS);
        } else {
            clicks = 0;
            callback.sendCommand(new StringType("RELEASE"));
        }
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // ignored
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return PROFILE_TYPE_UID;
    }

    private boolean stateChanged(String event) {
        boolean newState;
        if (event.equals(config.on)) {
            newState = true;
        } else if (event.equals(config.off)) {
            newState = false;
        } else {
            logger.warn("Channel has triggered unrecognized event {}", event);
            return false;
        }

        if (state != newState) {
            state = newState;
            return true;
        }
        return false;
    }

    private void cancel() {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
    }

    private void longPress() {
        logger.debug("Detected long press on multiPress profile");
        callback.sendCommand(new StringType("HOLD"));
        clicks = -1;
    }

    private void clicks() {
        logger.debug("Detected {} clicks on multiPress profile", clicks);
        callback.sendCommand(new StringType(String.valueOf(clicks)));
        clicks = 0;
    }
}
