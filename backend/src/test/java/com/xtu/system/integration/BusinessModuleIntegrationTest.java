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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class BusinessModuleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateCourseNoticeAndReviewApplication() throws Exception {
        String token = loginAsAdmin();
        long unique = System.currentTimeMillis();
        String courseCode = "C" + unique;

        String courseResponse = mockMvc.perform(post("/api/courses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "courseCode": "%s",
                      "courseName": "自动化测试课程",
                      "deptId": 2001,
                      "teacherId": 20001,
                      "credit": 2.5,
                      "courseType": "选修",
                      "semester": "2026秋",
                      "status": 1,
                      "remark": "测试课程"
                    }
                    """.formatted(courseCode)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long courseId = objectMapper.readTree(courseResponse).path("data").asLong();

        try {
            mockMvc.perform(post("/api/notices")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "title": "自动化测试公告",
                          "noticeType": "系统通知",
                          "content": "公告内容测试",
                          "publishStatus": 0,
                          "pinned": 0
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

            String applicationResponse = mockMvc.perform(post("/api/applications")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "applicantUserId": 2,
                          "applicationType": "课程调整",
                          "targetName": "自动化测试课程",
                          "reason": "测试审批流"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

            Long applicationId = objectMapper.readTree(applicationResponse).path("data").asLong();

            mockMvc.perform(put("/api/applications/{id}/review", applicationId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "status": 1,
                          "reviewRemark": "测试审核通过"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

            mockMvc.perform(delete("/api/courses/{id}", courseId)
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        } finally {
            mockMvc.perform(delete("/api/courses/{id}", courseId)
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
