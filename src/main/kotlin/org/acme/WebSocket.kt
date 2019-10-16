package org.acme

import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.Session;

@ServerEndpoint("/djca/{username}")
@ApplicationScoped
class WebSocket {

    private var sessions: ConcurrentHashMap<String, Session> = ConcurrentHashMap()

    @OnOpen
    fun onOpen(session: Session, @PathParam("username") username: String) {
        sessions[username] = session
        broadcast("User $username joined")
    }

    @OnClose
    fun onClose(session: Session, @PathParam("username") username: String) {
        sessions.remove(username)
        broadcast("User $username left")
    }

    @OnError
    fun onError(session: Session, @PathParam("username") username: String, throwable: Throwable) {
        sessions.remove(username)
        broadcast("User $username left on error: $throwable")
    }

    @OnMessage
    fun onMessage(message: String, @PathParam("username") username: String) {
        broadcast(">> $username: $message")
    }

    private fun broadcast(message: String) {
        sessions.values.forEach { s ->
            s.asyncRemote.sendObject(message) { result ->
                if (result.exception != null) {
                    println("Unable to send message: ${result.exception}")
                }
            }
        }
    }
}