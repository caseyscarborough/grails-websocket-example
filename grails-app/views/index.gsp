<html>
<head>
  <meta name="layout" content="main">
  <title>WebSocket Example</title>
  <style>
  #chatroom {
    padding: 10px;
  }

  #message {
    width: 400px;
    padding: 10px;
  }

  #log {
    width: 400px;
    height: 400px;
    border: 1px solid #ddd;
    overflow: auto;
    padding: 10px;
  }
  </style>
  <script>
    $(document).ready(function () {
      var log = $("#log"),
          message = $("#message"),
          sendMessageButton = $("#send-message-button"),

      // Create the WebSocket URL link. This URI maps to the URI you specified
      // in the @ServerEndpoint annotation in ChatroomEndpoint.groovy.
          webSocketUrl = "${createLink(uri: '/chatroom', absolute: true).replaceFirst(/http/, /ws/)}",

      // Connect to the WebSocket.
          socket = new WebSocket(webSocketUrl);

      socket.onopen = function () {
        log.append("<p>Connected to server. Enter your username.</p>");
      };

      socket.onmessage = function (message) {
        log.append("<p>" + message.data + "</p>");
      };

      socket.onclose = function () {
        log.append("<p>Connection to server was lost.</p>");
      };

      sendMessageButton.on('click', function () {
        var text = message.val();
        if ($.trim(text) !== '') {
          // Send the message and clear the text.
          socket.send(text);

          message.val("");
          message.focus();
          return;
        }
        message.focus();
      });

      message.keyup(function(e) {
        if (e.keyCode == 13) {
          sendMessageButton.trigger('click');
        }
      });
    });
  </script>
</head>

<body>
<div id="chatroom">
  <input type="text" id="message" placeholder="Enter your username first, then chat"><br>

  <div id="log"></div><br>
  <button id="send-message-button">Send</button>
</div>
</body>
</html>
