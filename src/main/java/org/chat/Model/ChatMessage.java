package org.chat.Model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatMessage {
	
	//private Long id;
	private String sender;
	private String content;
	private String room;
	
	//@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private String timestamp;
}
