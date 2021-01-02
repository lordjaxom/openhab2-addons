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
package org.openhab.binding.maxcul.internal.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.maxcul.internal.utils.Checks.requireNonNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.mockito.invocation.InvocationOnMock;

/**
 * @author Sascha Volkenandt - Initial contribution
 */
@NonNullByDefault
public class SchedulerMockProvider {

    private final ScheduledExecutorService schedulerMock = mock(ScheduledExecutorService.class);
    private final ScheduledFuture<?> futureMock = mock(ScheduledFuture.class);

    private @Nullable Runnable runnable;

    public SchedulerMockProvider() {
        doAnswer(this::schedule).when(schedulerMock).schedule(any(Runnable.class), anyLong(), any());
    }

    public ScheduledExecutorService scheduler() {
        return schedulerMock;
    }

    public ScheduledFuture<?> future() {
        return futureMock;
    }

    public void invoke() {
        requireNonNull(runnable).run();
    }

    private ScheduledFuture<?> schedule(InvocationOnMock invocationOnMock) {
        runnable = invocationOnMock.getArgument(0);
        return futureMock;
    }
}
