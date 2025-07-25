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
package org.openhab.binding.mercedesme.internal.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.handler.AccountHandler;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.mbcarkit.proto.Client.ClientMessage;
import com.daimler.mbcarkit.proto.VehicleEvents;
import com.daimler.mbcarkit.proto.VehicleEvents.PushMessage;

/**
 * {@link MBWebsocket} as socket endpoint to communicate with Mercedes
 *
 * @author Bernd Weymann - Initial contribution
 */
@WebSocket
@NonNullByDefault
public class MBWebsocket {
    // timeout 14 Minutes - just below scheduling of 15 Minutes by AccountHandler
    private static final int CONNECT_TIMEOUT_MS = 14 * 60 * 1000;
    // standard runtime of Websocket
    private static final int WS_RUNTIME_MS = 60 * 1000;
    // addon time of 1 minute for a new send command
    private static final int ADDON_MESSAGE_TIME_MS = 60 * 1000;
    // check Socket time elapsed each second
    private static final int CHECK_INTERVAL_MS = 1000;
    // additional 5 minutes after keep alive
    private static final int KEEP_ALIVE_ADDON = 5 * 60 * 1000;

    private final Logger logger = LoggerFactory.getLogger(MBWebsocket.class);
    private AccountHandler accountHandler;
    private HttpClient httpClient;
    private boolean running = false;
    private Instant runTill = Instant.now();
    private @Nullable Session session;
    private List<ClientMessage> commandQueue = new ArrayList<>();

    private boolean keepAlive = false;

    public MBWebsocket(AccountHandler ah, HttpClient hc) {
        accountHandler = ah;
        httpClient = hc;
    }

    /**
     * Is called by
     * - scheduler every 15 minutes
     * - handler sending a command
     * - handler requesting refresh
     */
    public void run() {
        synchronized (this) {
            if (running) {
                return;
            } else {
                running = true;
                runTill = Instant.now().plusMillis(WS_RUNTIME_MS);
            }
        }
        try {
            WebSocketClient client = new WebSocketClient(httpClient);
            client.setMaxIdleTimeout(CONNECT_TIMEOUT_MS);
            client.setStopTimeout(CONNECT_TIMEOUT_MS);
            ClientUpgradeRequest request = accountHandler.getClientUpgradeRequest();
            String websocketURL = accountHandler.getWSUri();
            if (Constants.JUNIT_TOKEN.equals(request.getHeader("Authorization"))) {
                // avoid unit test requesting real web socket - simply return
                return;
            }
            logger.trace("Websocket start {} max message size {}", websocketURL, client.getMaxBinaryMessageSize());
            client.start();
            client.connect(this, new URI(websocketURL), request);
            while (keepAlive || Instant.now().isBefore(runTill)) {
                try {
                    Thread.sleep(CHECK_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    logger.trace("Websocket interrupted during sleeping - stop executing");
                    runTill = Instant.MIN;
                }
                // sends one message per second
                if (sendMessage()) {
                    // add additional runtime to execute and finish command
                    runTill = runTill.plusMillis(ADDON_MESSAGE_TIME_MS);
                }
            }
            logger.trace("Websocket stop");
            client.stop();
            client.destroy();
        } catch (Throwable t) {
            // catch Exceptions of start stop and declare communication error
            accountHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/mercedesme.account.status.websocket-failure");
            logger.warn("Websocket handling exception: {}", t.getMessage());
        } finally {
            synchronized (this) {
                running = false;
            }
        }
    }

    public void setCommand(ClientMessage cm) {
        commandQueue.add(cm);
    }

    private boolean sendMessage() {
        if (!commandQueue.isEmpty()) {
            ClientMessage message = commandQueue.remove(0);
            logger.trace("Send Message {}", message.getAllFields());
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                message.writeTo(baos);
                if (session != null) {
                    session.getRemote().sendBytes(ByteBuffer.wrap(baos.toByteArray()));
                }
                return true;
            } catch (IOException e) {
                logger.warn("Error sending message {} : {}", message.getAllFields(), e.getMessage());
            }
            logger.info("Send Message {} done", message.getAllFields());
        }
        return false;
    }

    public void sendAcknowledgeMessage(ClientMessage message) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            message.writeTo(baos);
            if (session != null) {
                session.getRemote().sendBytes(ByteBuffer.wrap(baos.toByteArray()));
            }
        } catch (IOException e) {
            logger.warn("Error sending acknowledge {} : {}", message.getAllFields(), e.getMessage());
        }
    }

    public void interrupt() {
        synchronized (this) {
            runTill = Instant.MIN;
            keepAlive = false;
        }
    }

    /**
     * If disposed temp debug files are deleted
     */
    public void dispose() {
        interrupt();
    }

    public void keepAlive(boolean b) {
        if (!keepAlive) {
            if (b) {
                logger.trace("WebSocket - keep alive start");
            }
        } else {
            if (!b) {
                // after keep alive is finished add 5 minutes to cover e.g. door events after
                // trip is finished
                runTill = Instant.now().plusMillis(KEEP_ALIVE_ADDON);
                logger.trace("Websocket - keep alive stop - run till {}", runTill.toString());
            }
        }
        keepAlive = b;
    }

    /**
     * endpoints
     */

    @OnWebSocketMessage
    public void onByteArray(byte[] blob, int offset, int length) {
        try {
            byte[] message = blob;
            if (offset != 0) {
                int offsetLength = length - offset;
                message = new byte[offsetLength];
                System.arraycopy(blob, offset, message, 0, offsetLength);

            }
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(message);
            logger.trace("WebSocket - Message {}", pm.getMsgCase());
            accountHandler.enqueueMessage(pm);
            /**
             * https://community.openhab.org/t/mercedes-me/136866/12
             * Release Websocket thread as early as possible to avoid exceptions
             *
             * 1. Websocket thread responsible for reading stream into PushMessage and enqueue for
             * AccountHandler.
             * 2. AccountHamdler thread responsible for handling PushMessage. In case of
             * update enqueue PushMessage
             * at VehicleHandöer
             * 3. VehicleHandler responsible to update channels
             */
        } catch (IOException e) {
            logger.warn("IOException decoding message {}", e.getMessage());
        } catch (Error err) {
            logger.warn("Error decoding message {}", err.getMessage());
        }
    }

    @OnWebSocketClose
    public void onDisconnect(Session session, int statusCode, String reason) {
        logger.debug("Disconnected from server. Status {} Reason {}", statusCode, reason);
        this.session = null;
        // ensure execution stop if disconnect was triggered from server side
        interrupt();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        accountHandler.updateStatus(ThingStatus.ONLINE);
        this.session = session;
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        logger.debug("Error during web socket connection - {}", t.getMessage());
        accountHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/mercedesme.account.status.websocket-failure [\"" + t.getMessage() + "\"]");
    }
}
