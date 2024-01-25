package com.ingroupe.efti.eftigate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingroupe.efti.eftigate.service.ApIncomingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto.MESSAGE_ID;
import static com.ingroupe.efti.edeliveryapconnector.dto.ReceivedNotificationDto.SENT_SUCCESS;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApIncomingController.class)
@ContextConfiguration(classes= {ApIncomingController.class})
@ExtendWith(SpringExtension.class)
class ApIncomingControllerTest {

    @MockBean
    private ApIncomingService apIncomingService;

    @Autowired
    protected MockMvc mockMvc;

    @Test
    @WithMockUser
    void getByIdTestWithData() throws Exception {

        Map<String, Map<String, String>> body = Map.of(SENT_SUCCESS, Map.of(MESSAGE_ID, "test"));

        mockMvc.perform(post("/ws/notification")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(body)))
                .andExpect(status().isOk())
                .andReturn();
    }
}
