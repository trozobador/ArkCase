package com.armedia.acm.plugins.task.web.api;

import com.armedia.acm.plugins.task.exception.AcmTaskException;
import com.armedia.acm.plugins.task.model.AcmApplicationTaskEvent;
import com.armedia.acm.plugins.task.model.AcmTask;
import com.armedia.acm.plugins.task.service.TaskDao;
import com.armedia.acm.plugins.task.service.TaskEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.easymock.Capture;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/spring-web-acm-web.xml",
        "classpath:/spring/spring-library-task-plugin-test.xml"
})
public class ClaimTaskAPIControllerTest extends EasyMockSupport
{
    private MockMvc mockMvc;
    private MockHttpSession mockHttpSession;

    private ClaimTaskAPIController unit;

    private TaskDao mockTaskDao;
    private TaskEventPublisher mockTaskEventPublisher;
    private Authentication mockAuthentication;

    @Autowired
    private ExceptionHandlerExceptionResolver exceptionResolver;

    private Logger log = LoggerFactory.getLogger(getClass());

    @Before
    public void setUp() throws Exception
    {
        mockTaskDao = createMock(TaskDao.class);
        mockHttpSession = new MockHttpSession();
        mockAuthentication = createMock(Authentication.class);
        mockTaskEventPublisher = createMock(TaskEventPublisher.class);

        unit = new ClaimTaskAPIController();
        unit.setTaskEventPublisher(mockTaskEventPublisher);
        unit.setTaskDao(mockTaskDao);

        mockMvc = MockMvcBuilders.standaloneSetup(unit).setHandlerExceptionResolvers(exceptionResolver).build();
    }

    @Test
    public void claimTask() throws Exception
    {
        Long taskId = 500L;
        String userId = "user";

        AcmTask testTask = new AcmTask();
        testTask.setTaskId(taskId);

        Capture<AcmApplicationTaskEvent> capturedEvent = new Capture<>();

        expect(mockTaskDao.claimTask(eq(taskId), eq(userId))).andReturn(testTask);
        mockTaskEventPublisher.publishTaskEvent(capture(capturedEvent));

        // MVC test classes must call getName() somehow
        expect(mockAuthentication.getName()).andReturn(userId).atLeastOnce();

        replayAll();

        MvcResult result = mockMvc.perform(
                post("/api/v1/plugin/task/claim/{taskId}", taskId)
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .session(mockHttpSession)
                        .principal(mockAuthentication))
                .andReturn();

        verifyAll();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE));

        String returned = result.getResponse().getContentAsString();

        log.info("results: " + returned);

        ObjectMapper objectMapper = new ObjectMapper();

        AcmTask claimedTask = objectMapper.readValue(returned, AcmTask.class);

        assertNotNull(claimedTask);
        assertEquals(claimedTask.getTaskId(), taskId);

        AcmApplicationTaskEvent event = capturedEvent.getValue();
        assertEquals(taskId, event.getObjectId());
        assertEquals("TASK", event.getObjectType());
        assertTrue(event.isSucceeded());
    }

    @Test
    public void claimTask_exception() throws Exception
    {
        Long taskId = 500L;
        String userId = "user";
        String ipAddress = "ipAddress";

        AcmTask found = new AcmTask();
        found.setTaskId(taskId);

        Capture<AcmApplicationTaskEvent> capturedEvent = new Capture<>();

        mockHttpSession.setAttribute("acm_ip_address", ipAddress);

        expect(mockTaskDao.claimTask(eq(taskId), eq(userId))).andThrow(new AcmTaskException("testException"));
        mockTaskEventPublisher.publishTaskEvent(capture(capturedEvent));
        // MVC test classes must call getName() somehow
        expect(mockAuthentication.getName()).andReturn(userId).atLeastOnce();

        replayAll();

        mockMvc.perform(
                post("/api/v1/plugin/task/claim/{taskId}", taskId)
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .session(mockHttpSession)
                        .principal(mockAuthentication))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN));

        verifyAll();

        AcmApplicationTaskEvent event = capturedEvent.getValue();
        assertEquals(taskId, event.getObjectId());
        assertEquals("TASK", event.getObjectType());
        assertFalse(event.isSucceeded());
    }

    @Test
    public void unclaimTask() throws Exception
    {
        Long taskId = 500L;
        String userId = "user";

        AcmTask testTask = new AcmTask();
        testTask.setTaskId(taskId);

        Capture<AcmApplicationTaskEvent> capturedEvent = new Capture<>();

        expect(mockTaskDao.unclaimTask(eq(taskId))).andReturn(testTask);
        mockTaskEventPublisher.publishTaskEvent(capture(capturedEvent));

        // MVC test classes must call getName() somehow
        expect(mockAuthentication.getName()).andReturn(userId).atLeastOnce();

        replayAll();

        MvcResult result = mockMvc.perform(
                post("/api/v1/plugin/task/unclaim/{taskId}", taskId)
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .session(mockHttpSession)
                        .principal(mockAuthentication))
                .andReturn();

        verifyAll();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE));

        String returned = result.getResponse().getContentAsString();

        log.info("results: " + returned);

        ObjectMapper objectMapper = new ObjectMapper();

        AcmTask unclaimedTask = objectMapper.readValue(returned, AcmTask.class);

        assertNotNull(unclaimedTask);
        assertEquals(unclaimedTask.getTaskId(), taskId);

        AcmApplicationTaskEvent event = capturedEvent.getValue();
        assertEquals(taskId, event.getObjectId());
        assertEquals("TASK", event.getObjectType());
        assertTrue(event.isSucceeded());
    }

}