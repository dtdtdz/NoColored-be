package com.ssafy.backend;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@SpringBootTest
class BackendApplicationTests {

//	@Test
//	void contextLoads() {
//	}

	@Test
	void testTextMessageProcessing() throws IOException {
		// WebSocketSession 모방 객체 생성
//		WebSocketSession mockSession = mock(WebSocketSession.class);
//
//		// 테스트할 메시지 생성
//		String testMessageContent = "Hello, WebSocket!";
//		TextMessage testMessage = new TextMessage(testMessageContent);
//
//		// TextMessageServiceImpl 인스턴스 생성
//		TextMessageServiceImpl service = new TextMessageServiceImpl();
//
//		// textMessageProcessing 메서드 실행
//		String result = service.textMessageProcessing(mockSession, testMessage);
//
//		// 반환된 메시지 내용 검증
//		assertEquals(testMessageContent, result, "The message content should match the input.");
	}

}
