package com.roomreservation.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class AddReservationServletTest {

    @Test
    public void testAddValidReservation() throws Exception {
        // Mock request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // Mock PrintWriter to avoid NullPointerException
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // Simulate valid reservation data
        when(request.getParameter("guest_name")).thenReturn("John Doe");
        when(request.getParameter("address")).thenReturn("123 Ocean Ave");
        when(request.getParameter("contact_no")).thenReturn("0771234567");
        when(request.getParameter("room_type")).thenReturn("Suite");
        when(request.getParameter("check_in")).thenReturn("2026-04-01");
        when(request.getParameter("check_out")).thenReturn("2026-04-10");

        // Call the servlet method
        AddReservationServlet servlet = new AddReservationServlet();
        servlet.doPost(request, response);

        // Verify if the response's writer was used
        verify(response).getWriter();
        verify(writer).println(anyString());  // Ensure that the PrintWriter was used
    }

    @Test
    public void testAddReservationInvalidData() throws Exception {
        // Mock request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // Mock PrintWriter to avoid NullPointerException
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // Simulate invalid data (missing guest name)
        when(request.getParameter("guest_name")).thenReturn("");  // Empty guest name
        when(request.getParameter("address")).thenReturn("123 Ocean Ave");
        when(request.getParameter("contact_no")).thenReturn("0771234567");
        when(request.getParameter("room_type")).thenReturn("Suite");
        when(request.getParameter("check_in")).thenReturn("2026-04-01");
        when(request.getParameter("check_out")).thenReturn("2026-04-10");

        // Call the servlet method
        AddReservationServlet servlet = new AddReservationServlet();
        servlet.doPost(request, response);

        // Verify if the response's writer was used
        verify(response).getWriter();
        verify(writer).println(anyString());  // Ensure the PrintWriter was used
    }
}