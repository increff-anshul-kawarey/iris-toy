package com.iris.increff.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for MessageData model
 * Tests all getters and setters for complete coverage
 */
public class MessageDataTest {

    @Test
    public void testGettersAndSetters() {
        MessageData data = new MessageData();

        // Test setter and getter
        data.setMessage("Test message");
        assertEquals("Test message", data.getMessage());

        // Test null message
        data.setMessage(null);
        assertNull(data.getMessage());

        // Test empty message
        data.setMessage("");
        assertEquals("", data.getMessage());
    }

    @Test
    public void testMessageDataWithLongMessage() {
        MessageData data = new MessageData();
        String longMessage = "This is a very long message that contains multiple words and sentences. " +
                           "It is used to test that the MessageData class can handle long strings properly.";

        data.setMessage(longMessage);
        assertEquals(longMessage, data.getMessage());
    }

    @Test
    public void testMessageDataWithSpecialCharacters() {
        MessageData data = new MessageData();
        String specialMessage = "Message with special chars: @#$%^&*()_+-=[]{}|;':\",./<>?";

        data.setMessage(specialMessage);
        assertEquals(specialMessage, data.getMessage());
    }
}

