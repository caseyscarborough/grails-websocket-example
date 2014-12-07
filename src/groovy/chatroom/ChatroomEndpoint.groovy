package chatroom

import grails.util.Environment
import org.apache.catalina.core.ApplicationContext
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener
import javax.websocket.*
import javax.websocket.server.ServerContainer
import javax.websocket.server.ServerEndpoint

@ServerEndpoint("/chatroom")
@WebListener
class ChatroomEndpoint implements ServletContextListener {

  private static final Logger log = Logger.getLogger(ChatroomEndpoint.class)
  private static final Set<Session> users = ([] as Set).asSynchronized()

  @Override
  void contextInitialized(ServletContextEvent servletContextEvent) {
    ServletContext servletContext = servletContextEvent.servletContext
    ServerContainer serverContainer = servletContext.getAttribute("javax.websocket.server.ServerContainer")

    try {
      // This is necessary for Grails to add the endpoint in development.
      // In production, the endpoint will be added by the @ServerEndpoint
      // annotation.
      if (Environment.current == Environment.DEVELOPMENT) {
        serverContainer.addEndpoint(ChatroomEndpoint)
      }

      // This is mainly for demonstration of retrieving the ApplicationContext,
      // the GrailsApplication instance, and application configuration.
      ApplicationContext ctx = servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
      GrailsApplication grailsApplication = ctx.grailsApplication
      serverContainer.defaultMaxSessionIdleTimeout = grailsApplication.config.servlet.defaultMaxSessionIdleTimeout ?: 0
    } catch (IOException e) {
      log.error(e.message, e)
    }
  }

  @Override
  void contextDestroyed(ServletContextEvent servletContextEvent) {
  }

  /**
   * This method is executed when a client connects to this websocket
   * endpoint, and adds the new user's session to our users list.
   */
  @OnOpen
  public void onOpen(Session userSession) {
    users.add(userSession)
  }

  /**
   * This message is executed when a client sends a message to the
   * websocket endpoint. It sets the first message that the user sends
   * as the user's username, and treats all others as a message to
   * the chatroom.
   * @param message
   * @param userSession
   */
  @OnMessage
  public void onMessage(String message, Session userSession) {
    String username = userSession.userProperties.get("username")

    if (!username) {
      userSession.userProperties.put("username", message)
      return
    }

    // Send the message to all users in the chatroom.
    sendMessage(message)
  }

  /**
   * This method is executed when a user disconnects from the chatroom.
   */
  @OnClose
  public void onClose(Session userSession, CloseReason closeReason) {
    String username = userSession.userProperties.get("username")

    if (username) {
      sendMessage(String.format("%s has left the chatroom.", username))
    }
    users.remove(userSession)
  }

  @OnError
  public void onError(Throwable t) {
    log.error(t.message, t)
  }

  /**
   * Iterate through all chatroom users and send a message to them.
   * @param message
   */
  private void sendMessage(String message) {
    Iterator<Session> iterator = users.iterator()
    while(iterator.hasNext()) {
      iterator.next().basicRemote.sendText(message)
    }
  }
}
