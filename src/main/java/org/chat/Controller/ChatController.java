package org.chat.Controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.chat.Model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;

//@RestController
//@RequestMapping("/chatapp")
@Controller
public class ChatController {

	
	/* 
	  Message Flow:
        Client sends to /app/sendMessage

        Spring handles it via @MessageMapping("/sendMessage")

        Server broadcasts to /topic/message

        Clients subscribe to /topic/message
	 */
	
	
	// /app/sendMessage
//	@MessageMapping("/sendMessage")
//	@SendTo("/topic/message")  // Broadcast to this topic
//	public ChatMessage sendMessage(ChatMessage message)
//	{
//		message.setTimestamp(LocalDateTime.now());
//		System.out.println(message);
//		return message;
//	}
	
	@Autowired
    private SimpMessagingTemplate messagingTemplate;
	
	private Map<String, Set<String>> activeUsers=new ConcurrentHashMap<>();

    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload ChatMessage message) {
        message.setTimestamp(Instant.now().toString());
     
        // Add user to active list
        activeUsers
            .computeIfAbsent(message.getRoom(), k -> ConcurrentHashMap.newKeySet())
            .add(message.getSender());

        messagingTemplate.convertAndSend("/topic/" + message.getRoom(), message);
        
        System.out.println(message);
     // Broadcast updated user list
        messagingTemplate.convertAndSend("/topic/" + message.getRoom() + "/users", 
            activeUsers.get(message.getRoom()));
    }
    
    @MessageMapping("/leaveRoom")
    public void leaveRoom(@Payload ChatMessage message) {
        String room = message.getRoom();
        String sender = message.getSender();

        if (room != null && sender != null) {
            Set<String> users = activeUsers.get(room);
            if (users != null) {
                users.remove(sender);
            }

            // Send system message about user leaving
            ChatMessage leaveMsg = new ChatMessage();
            leaveMsg.setSender(sender);
            leaveMsg.setRoom(room);
            leaveMsg.setTimestamp(Instant.now().toString());
            leaveMsg.setContent("👋 " + sender + " has left the room.");

            messagingTemplate.convertAndSend("/topic/" + room, leaveMsg);

            // Send updated user list
            messagingTemplate.convertAndSend("/topic/" + room + "/users", users);
        }
    }

	
	@GetMapping("/chat")
	public String chat()
	{
		return "chat";  // Return Thymeleaf template
	}
}
