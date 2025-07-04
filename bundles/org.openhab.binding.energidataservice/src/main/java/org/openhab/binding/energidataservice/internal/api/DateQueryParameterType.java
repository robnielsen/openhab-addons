/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.energidataservice.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This represents a dynamic date to be used in a query.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum DateQueryParameterType {
    NOW("now"),
    UTC_NOW("utcnow"),
    START_OF_DAY("StartOfDay"),
    START_OF_MONTH("StartOfMonth"),
    START_OF_YEAR("StartOfYear");

    private final String name;

    DateQueryParameterType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static DateQueryParameterType of(String name) {
        for (DateQueryParameterType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown date query parameter: " + name);
    }
}
