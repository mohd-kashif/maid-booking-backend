package com.rupeek.maidbooking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiFlowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void completesDiscoveryBookingPaymentAndCancellationFlow() throws Exception {
        OffsetDateTime start = nextMondayAt(10);
        OffsetDateTime end = start.plusHours(1);

        String maidResponse = mockMvc.perform(post("/api/maids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Asha", "locality":"Indiranagar",
                                  "services":[{"type":"CLEANING","price":400,"currency":"INR"}],
                                  "availability":[{"dayOfWeek":"MONDAY","startTime":"09:00","endTime":"18:00"}],
                                  "rating":4.5, "gender":"FEMALE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String maidId = com.jayway.jsonpath.JsonPath.read(maidResponse, "$.id");

        mockMvc.perform(get("/api/maids").param("services", "CLEANING")
                        .param("start", start.toString()).param("end", end.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(maidId));

        String bookingResponse = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"customerId\":\"customer-1\",\"maidId\":\"" + maidId + "\"," +
                                "\"type\":\"SCHEDULED\",\"services\":[\"CLEANING\"]," +
                                "\"start\":\"" + start + "\",\"end\":\"" + end + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String bookingId = com.jayway.jsonpath.JsonPath.read(bookingResponse, "$.id");

        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookingId\":\"" + bookingId
                                + "\",\"method\":\"UPI\",\"idempotencyKey\":\"flow-1\",\"paymentDetails\":\"customer@upi\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("SUCCESS"));

        mockMvc.perform(get("/api/maids").param("services", "CLEANING")
                        .param("start", start.toString()).param("end", end.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(post("/api/bookings/" + bookingId + "/cancellations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scope\":\"ENTIRE_BOOKING\",\"reason\":\"CUSTOMER_REQUEST\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.refundable").value(true));

        mockMvc.perform(get("/api/maids").param("services", "CLEANING")
                        .param("start", start.toString()).param("end", end.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(maidId));
    }

    private static OffsetDateTime nextMondayAt(int hour) {
        return LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY)
                .atTime(hour, 0).atOffset(ZoneOffset.UTC);
    }
}
