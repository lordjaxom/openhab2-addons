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

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProfileFactory that creates the multiPress profile
 *
 * @author Sascha Volkenandt - initial contribution
 *
 */
@NonNullByDefault
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class })
public class MultiPressProfileFactory implements ProfileFactory, ProfileTypeProvider {

    private final Logger logger = LoggerFactory.getLogger(MultiPressProfileFactory.class);

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext context) {
        return new MultiPressProfile(callback, context);
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return List.of(MultiPressProfile.PROFILE_TYPE_UID);
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return List.of(ProfileTypeBuilder
                .newTrigger(MultiPressProfile.PROFILE_TYPE_UID, MultiPressProfile.PROFILE_TYPE_UID.getId()).build());
    }
}
