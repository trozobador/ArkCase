package com.armedia.acm.plugins.task.web.api;

import com.armedia.acm.core.exceptions.AcmObjectNotFoundException;
import com.armedia.acm.plugins.task.exception.AcmTaskException;
import com.armedia.acm.plugins.task.model.AcmFindTaskByIdEvent;
import com.armedia.acm.plugins.task.model.AcmTask;
import com.armedia.acm.plugins.task.service.TaskDao;
import com.armedia.acm.plugins.task.service.TaskEventPublisher;
import org.codehaus.jackson.map.ObjectMapper;
import org.easymock.Capture;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class FindTaskByIdAPIControllerTest extends EasyMockSupport
{
    private MockMvc mockMvc;
    private MockHttpSession mockHttpSession;

    private FindTaskByIdAPIController unit;

    private TaskDao mockTaskDao;
    private TaskEventPublisher mockTaskEventPublisher;
    private Authentication mockAuthentication;

    private Logger log = LoggerFactory.getLogger(getClass());

    @Before
    public void setUp() throws Exception
    {
        mockTaskDao = createMock(TaskDao.class);
        mockTaskEventPublisher = createMock(TaskEventPublisher.class);
        mockHttpSession = new MockHttpSession();
        mockAuthentication = createMock(Authentication.class);

        unit = new FindTaskByIdAPIController();

        unit.setTaskDao(mockTaskDao);
        unit.setTaskEventPublisher(mockTaskEventPublisher);

        mockMvc = MockMvcBuilders.standaloneSetup(unit).build();
    }

    @Test
    public void findTaskById() throws Exception
    {
        String ipAddress = "ipAddress";
        String title = "The Test Title";
        Long taskId = 500L;

        AcmTask returned = new AcmTask();
        returned.setTaskId(taskId);
        returned.setTitle(title);

        mockHttpSession.setAttribute("acm_ip_address", ipAddress);

        expect(mockTaskDao.findById(taskId)).andReturn(returned);

        Capture<AcmFindTaskByIdEvent> eventRaised = new Capture<>();
        mockTaskEventPublisher.publishTaskEvent(capture(eventRaised), eq(mockAuthentication), eq(ipAddress));

        // MVC test classes must call getName() somehow
        expect(mockAuthentication.getName()).andReturn("user");

        replayAll();

        MvcResult result = mockMvc.perform(
                get("/api/v1/plugin/task/byId/{taskId}", taskId)
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .session(mockHttpSession)
                        .principal(mockAuthentication))
                .andReturn();

        verifyAll();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE));

        String json = result.getResponse().getContentAsString();

        log.info("results: " + json);

        AcmTask fromJson = new ObjectMapper().readValue(json, AcmTask.class);

        assertNotNull(fromJson);
        assertEquals(returned.getTitle(), fromJson.getTitle());

        AcmFindTaskByIdEvent event = eventRaised.getValue();
        assertTrue(event.isSucceeded());
        assertEquals(taskId, event.getObjectId());
    }

    @Test
    public void findTaskById_exception() throws Exception
    {
        Long taskId = 500L;
        String ipAddress = "ipAddress";

        mockHttpSession.setAttribute("acm_ip_address", ipAddress);

        expect(mockTaskDao.findById(taskId)).andThrow(new AcmTaskException());

        Capture<AcmFindTaskByIdEvent> eventRaised = new Capture<>();
        mockTaskEventPublisher.publishTaskEvent(capture(eventRaised), eq(mockAuthentication), eq(ipAddress));

        expect(mockAuthentication.getName()).andReturn("user").atLeastOnce();

        replayAll();

        // Our controller should throw an exception. When the full dispatcher servlet is running, an
        // @ExceptionHandler will send the right HTTP response code to the browser.  In this test, we just have to
        // make sure the right exception is thrown.
        try
        {
            mockMvc.perform(
                    get("/api/v1/plugin/task/byId/{taskId}", taskId)
                            .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                            .principal(mockAuthentication)
                            .session(mockHttpSession));
            fail("should have thrown an exception");
        }
        catch (NestedServletException e)
        {
            // Spring MVC wraps the real exception with a NestedServletException
            assertNotNull(e.getCause());
            assertEquals(AcmObjectNotFoundException.class, e.getCause().getClass());
        }
        catch (Exception e)
        {
            fail("Threw the wrong exception! " + e.getClass().getName());
        }

        verifyAll();

        AcmFindTaskByIdEvent event = eventRaised.getValue();
        assertFalse(event.isSucceeded());
        assertEquals(taskId, event.getObjectId());

    }
}
