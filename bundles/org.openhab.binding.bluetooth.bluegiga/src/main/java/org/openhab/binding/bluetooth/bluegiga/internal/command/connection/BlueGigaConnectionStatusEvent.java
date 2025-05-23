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
package org.openhab.binding.bluetooth.bluegiga.internal.command.connection;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaDeviceResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.BluetoothAddressType;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.ConnectionStatusFlag;

/**
 * Class to implement the BlueGiga command <b>connectionStatusEvent</b>.
 * <p>
 * This event indicates the connection status and parameters.
 * <p>
 * This class provides methods for processing BlueGiga API commands.
 * <p>
 * Note that this code is autogenerated. Manual changes may be overwritten.
 *
 * @author Chris Jackson - Initial contribution of Java code generator
 */
@NonNullByDefault
public class BlueGigaConnectionStatusEvent extends BlueGigaDeviceResponse {
    public static final int COMMAND_CLASS = 0x03;
    public static final int COMMAND_METHOD = 0x00;

    /**
     * Connection status flags use connstatus-enumerator
     * <p>
     * BlueGiga API type is <i>ConnectionStatusFlag</i> - Java type is {@link ConnectionStatusFlag}
     * Parameter allows multiple options so implemented as a {@link Set}.
     */
    private Set<ConnectionStatusFlag> flags = new HashSet<>();

    /**
     * Remote devices Bluetooth address
     * <p>
     * BlueGiga API type is <i>bd_addr</i> - Java type is {@link String}
     */
    private String address;

    /**
     * Remote address type see: Bluetooth Address Types--gap
     * <p>
     * BlueGiga API type is <i>BluetoothAddressType</i> - Java type is {@link BluetoothAddressType}
     */
    private BluetoothAddressType addressType;

    /**
     * Current connection interval (units of 1.25ms)
     * <p>
     * BlueGiga API type is <i>uint16</i> - Java type is {@link int}
     */
    private int connInterval;

    /**
     * Current supervision timeout (units of 10ms)
     * <p>
     * BlueGiga API type is <i>uint16</i> - Java type is {@link int}
     */
    private int timeout;

    /**
     * Slave latency which tells how many connection intervals the slave may skip.
     * <p>
     * BlueGiga API type is <i>uint16</i> - Java type is {@link int}
     */
    private int latency;

    /**
     * Bonding handle if the device has been bonded with. Otherwise: 0xFF
     * <p>
     * BlueGiga API type is <i>uint8</i> - Java type is {@link int}
     */
    private int bonding;

    /**
     * Event constructor
     */
    public BlueGigaConnectionStatusEvent(int[] inputBuffer) {
        // Super creates deserializer and reads header fields
        super(inputBuffer);

        event = (inputBuffer[0] & 0x80) != 0;

        // Deserialize the fields
        connection = deserializeUInt8();
        flags = deserializeConnectionStatusFlag();
        address = deserializeAddress();
        addressType = deserializeBluetoothAddressType();
        connInterval = deserializeUInt16();
        timeout = deserializeUInt16();
        latency = deserializeUInt16();
        bonding = deserializeUInt8();
    }

    /**
     * Connection status flags use connstatus-enumerator
     * <p>
     * BlueGiga API type is <i>ConnectionStatusFlag</i> - Java type is {@link ConnectionStatusFlag}
     *
     * @return the current flags as {@link Set} of {@link ConnectionStatusFlag}
     */
    public Set<ConnectionStatusFlag> getFlags() {
        return flags;
    }

    /**
     * Remote devices Bluetooth address
     * <p>
     * BlueGiga API type is <i>bd_addr</i> - Java type is {@link String}
     *
     * @return the current address as {@link String}
     */
    public String getAddress() {
        return address;
    }

    /**
     * Remote address type see: Bluetooth Address Types--gap
     * <p>
     * BlueGiga API type is <i>BluetoothAddressType</i> - Java type is {@link BluetoothAddressType}
     *
     * @return the current address_type as {@link BluetoothAddressType}
     */
    public BluetoothAddressType getAddressType() {
        return addressType;
    }

    /**
     * Current connection interval (units of 1.25ms)
     * <p>
     * BlueGiga API type is <i>uint16</i> - Java type is {@link int}
     *
     * @return the current conn_interval as {@link int}
     */
    public int getConnInterval() {
        return connInterval;
    }

    /**
     * Current supervision timeout (units of 10ms)
     * <p>
     * BlueGiga API type is <i>uint16</i> - Java type is {@link int}
     *
     * @return the current timeout as {@link int}
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Slave latency which tells how many connection intervals the slave may skip.
     * <p>
     * BlueGiga API type is <i>uint16</i> - Java type is {@link int}
     *
     * @return the current latency as {@link int}
     */
    public int getLatency() {
        return latency;
    }

    /**
     * Bonding handle if the device has been bonded with. Otherwise: 0xFF
     * <p>
     * BlueGiga API type is <i>uint8</i> - Java type is {@link int}
     *
     * @return the current bonding as {@link int}
     */
    public int getBonding() {
        return bonding;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BlueGigaConnectionStatusEvent [connection=");
        builder.append(connection);
        builder.append(", flags=");
        builder.append(flags);
        builder.append(", address=");
        builder.append(address);
        builder.append(", addressType=");
        builder.append(addressType);
        builder.append(", connInterval=");
        builder.append(connInterval);
        builder.append(", timeout=");
        builder.append(timeout);
        builder.append(", latency=");
        builder.append(latency);
        builder.append(", bonding=");
        builder.append(bonding);
        builder.append(']');
        return builder.toString();
    }
}
