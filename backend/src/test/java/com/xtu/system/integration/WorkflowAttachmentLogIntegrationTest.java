package com.xtu.system.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class WorkflowAttachmentLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldHandleWorkflowAttachmentAndLogs() throws Exception {
        String token = loginAsAdmin();
        long unique = System.currentTimeMillis();

        String applicationResponse = mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "applicantUserId": 2,
                      "applicationType": "公告发布",
                      "targetName": "流程测试申请%s",
                      "reason": "流程测试"
                    }
                    """.formatted(unique)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long applicationId = objectMapper.readTree(applicationResponse).path("data").asLong();

        mockMvc.perform(get("/api/workflow/todo")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(put("/api/workflow/tasks/{recordId}/approve", applicationId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "commentText": "工作流审批通过"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/workflow/done")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/applications/{id}/records", applicationId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].actionType").value("submit"))
            .andExpect(jsonPath("$.data[1].actionType").value("approve"));

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "workflow-log-test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "attachment-content".getBytes()
        );

        String attachmentResponse = mockMvc.perform(multipart("/api/attachments/upload")
                .file(file)
                .param("bizType", "notice")
                .param("bizId", "40001")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long attachmentId = objectMapper.readTree(attachmentResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/attachments")
                .param("bizType", "notice")
                .param("bizId", "40001")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/attachments/{id}/download", attachmentId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().bytes("attachment-content".getBytes()));

        mockMvc.perform(delete("/api/attachments/{id}", attachmentId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/login-logs")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/operation-logs")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void shouldShowAttachmentCountAndCleanupAttachmentsWhenDeletingBusinessData() throws Exception {
        String token = loginAsAdmin();
        long unique = System.currentTimeMillis();

        Long courseId = createCourse(token, unique);
        Long noticeId = createNotice(token, unique);
        Long applicationId = createApplication(token, unique);

        Long courseAttachmentId = uploadAttachment(token, "course", courseId, "course-" + unique + ".txt");
        Long noticeAttachmentId = uploadAttachment(token, "notice", noticeId, "notice-" + unique + ".txt");
        Long applicationAttachmentId = uploadAttachment(token, "application", applicationId, "application-" + unique + ".txt");

        assertAttachmentCountInPage(token, "/api/courses", "课程清理测试" + unique, courseId, 1);
        assertAttachmentCountInPage(token, "/api/notices", "公告清理测试" + unique, noticeId, 1);
        assertAttachmentCountInPage(token, "/api/applications", "申请清理对象" + unique, applicationId, 1);

        mockMvc.perform(delete("/api/courses/{id}", courseId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(delete("/api/notices/{id}", noticeId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(delete("/api/applications/{id}", applicationId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        assertAttachmentListEmpty(token, "course", courseId);
        assertAttachmentListEmpty(token, "notice", noticeId);
        assertAttachmentListEmpty(token, "application", applicationId);

        assertAttachmentDownloadMissing(token, courseAttachmentId);
        assertAttachmentDownloadMissing(token, noticeAttachmentId);
        assertAttachmentDownloadMissing(token, applicationAttachmentId);
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

    private Long createCourse(String token, long unique) throws Exception {
        String courseCode = "C" + unique;
        String response = mockMvc.perform(post("/api/courses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "courseCode": "%s",
                      "courseName": "课程清理测试%s",
                      "deptId": 2001,
                      "teacherId": 20001,
                      "credit": 2.0,
                      "courseType": "测试课程",
                      "semester": "2026春",
                      "status": 1
                    }
                    """.formatted(courseCode, unique)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(response).path("data").asLong();
    }

    private Long createNotice(String token, long unique) throws Exception {
        String response = mockMvc.perform(post("/api/notices")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "公告清理测试%s",
                      "noticeType": "系统通知",
                      "content": "公告附件清理测试",
                      "publishStatus": 0,
                      "pinned": 0
                    }
                    """.formatted(unique)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(response).path("data").asLong();
    }

    private Long createApplication(String token, long unique) throws Exception {
        String response = mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "applicantUserId": 2,
                      "applicationType": "课程调整",
                      "targetName": "申请清理对象%s",
                      "reason": "申请附件清理测试"
                    }
                    """.formatted(unique)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(response).path("data").asLong();
    }

    private Long uploadAttachment(String token, String bizType, Long bizId, String fileName) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            fileName,
            MediaType.TEXT_PLAIN_VALUE,
            ("attachment-" + bizType).getBytes()
        );

        String response = mockMvc.perform(multipart("/api/attachments/upload")
                .file(file)
                .param("bizType", bizType)
                .param("bizId", String.valueOf(bizId))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private void assertAttachmentCountInPage(String token, String url, String keyword, Long targetId, int expectedCount) throws Exception {
        String response = mockMvc.perform(get(url)
                .param("pageNum", "1")
                .param("pageSize", "20")
                .param("keyword", keyword)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode list = objectMapper.readTree(response).path("data").path("list");
        JsonNode target = findItemById(list, targetId);
        if (target == null || target.path("attachmentCount").asInt(-1) != expectedCount) {
            throw new AssertionError("分页列表中的附件数量不符合预期");
        }
    }

    private void assertAttachmentListEmpty(String token, String bizType, Long bizId) throws Exception {
        mockMvc.perform(get("/api/attachments")
                .param("bizType", bizType)
                .param("bizId", String.valueOf(bizId))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    private void assertAttachmentDownloadMissing(String token, Long attachmentId) throws Exception {
        mockMvc.perform(get("/api/attachments/{id}/download", attachmentId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("附件不存在"));
    }

    private JsonNode findItemById(JsonNode list, Long id) {
        for (JsonNode item : list) {
            if (item.path("id").asLong() == id) {
                return item;
            }
        }
        return null;
    }
}
