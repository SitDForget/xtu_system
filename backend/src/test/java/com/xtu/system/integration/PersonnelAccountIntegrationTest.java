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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class PersonnelAccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateAndRemoveStudentAccount() throws Exception {
        String token = loginAsAdmin();
        long unique = System.currentTimeMillis();
        String studentNo = "S" + unique;
        String username = "stu" + unique;

        String createStudentResponse = mockMvc.perform(post("/api/students")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "studentNo": "%s",
                      "studentName": "测试学生",
                      "gender": 1,
                      "deptId": 2001,
                      "majorName": "软件工程",
                      "gradeYear": 2026,
                      "className": "测试班",
                      "phone": "13500000000",
                      "email": "student-test@xtu.edu.cn",
                      "status": 1,
                      "remark": "测试账号绑定"
                    }
                    """.formatted(studentNo)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long studentId = objectMapper.readTree(createStudentResponse).path("data").asLong();

        try {
            mockMvc.perform(post("/api/students/{id}/account", studentId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "username": "%s",
                          "password": "student123",
                          "status": 1
                        }
                        """.formatted(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

            mockMvc.perform(get("/api/students/{id}", studentId)
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountUsername").value(username));

            mockMvc.perform(delete("/api/students/{id}/account", studentId)
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

            mockMvc.perform(get("/api/students/{id}", studentId)
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").isEmpty());
        } finally {
            mockMvc.perform(delete("/api/students/{id}", studentId)
                .header("Authorization", "Bearer " + token));
        }
    }

    private String loginAsAdmin() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "admin",
                      "password": "admin123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("token").asText();
    }
}
