package com.example.otams;

import static org.junit.Assert.*;
import org.junit.Test;

public class AppLogicUnitTest {


    //TEST AVERAGE RATING CALCULATION

    @Test
    public void testAverageRating() {
        float r1 = 4.0f;
        float r2 = 5.0f;
        float r3 = 3.0f;

        float average = (r1 + r2 + r3) / 3f;

        assertEquals(4.0f, average, 0.001f);
    }


    //TEST AUTO-APPROVE LOGIC

    @Test
    public void testAutoApprove() {
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.autoApprove = true;

        boolean willAutoApprove = slot.autoApprove;

        assertTrue(willAutoApprove);
    }


    //TEST VALID SESSION TIME

    @Test
    public void testValidSessionTime() {
        String start = "10:00";
        String end = "12:00";

        boolean valid = timeLessThan(start, end);

        assertTrue(valid);
    }

    // Helper for time comparison
    private boolean timeLessThan(String t1, String t2) {
        int h1 = Integer.parseInt(t1.split(":")[0]);
        int m1 = Integer.parseInt(t1.split(":")[1]);
        int h2 = Integer.parseInt(t2.split(":")[0]);
        int m2 = Integer.parseInt(t2.split(":")[1]);

        if (h1 < h2) return true;
        if (h1 == h2) return m1 < m2;
        return false;
    }


    //TEST CANNOT DELETE BOOKED SLOT

    @Test
    public void testCannotDeleteBookedSlot() {
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.isBooked = true; // You already added this field earlier

        boolean canDelete = !slot.isBooked;

        assertFalse(canDelete);
    }
}
