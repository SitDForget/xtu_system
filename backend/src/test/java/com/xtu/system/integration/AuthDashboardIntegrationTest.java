package com.xtu.system.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthDashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldLoginAndReturnDashboardSummary() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.userCount").isNumber())
            .andExpect(jsonPath("$.data.courseCount").isNumber())
            .andExpect(jsonPath("$.data.pendingApplicationCount").isNumber());
    }

    @Test
    void shouldUpdateCurrentUserPassword() throws Exception {
        String adminToken = loginAsAdmin();
        long unique = System.currentTimeMillis();
        String username = "pwd" + unique;
        String oldPassword = "Initial123!";
        String newPassword = "Changed123!";

        String createUserResponse = mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "%s",
                      "password": "%s",
                      "realName": "密码测试用户",
                      "userType": 4,
                      "deptId": 1001,
                      "gender": 1,
                      "status": 1,
                      "phone": "13600000000",
                      "email": "password-test@xtu.edu.cn",
                      "roleIds": []
                    }
                    """.formatted(username, oldPassword)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long userId = objectMapper.readTree(createUserResponse).path("data").asLong();

        try {
            String userToken = login(username, oldPassword);

            mockMvc.perform(put("/api/auth/password")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "oldPassword": "%s",
                          "newPassword": "%s",
                          "confirmPassword": "%s"
                        }
                        """.formatted(oldPassword, newPassword, newPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(username, oldPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("账号或密码错误"));

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(username, newPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        } finally {
            mockMvc.perform(delete("/api/users/{id}", userId)
                    .header("Authorization", "Bearer " + adminToken));
        }
    }

    private String loginAsAdmin() throws Exception {
        return login("admin", "admin123");
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "%s",
                      "password": "%s"
                    }
                    """.formatted(username, password)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("token").asText();
    }
}
