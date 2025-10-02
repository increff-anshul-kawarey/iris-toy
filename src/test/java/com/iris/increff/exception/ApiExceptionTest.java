package com.iris.increff.exception;

import com.iris.increff.AbstractUnitTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ApiExceptionTest extends AbstractUnitTest {

    @Test
    public void testApiException_WithMessage() {
        String errorMessage = "This is a test error.";
        ApiException exception = new ApiException(errorMessage);

        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

}
