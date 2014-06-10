package com.armedia.acm.plugins.complaint.web.api;

import com.armedia.acm.plugins.complaint.dao.ComplaintDao;
import com.armedia.acm.plugins.complaint.model.Complaint;
import com.armedia.acm.plugins.complaint.service.ComplaintEventPublisher;
import com.armedia.acm.web.api.AcmSpringMvcErrorManager;
import org.codehaus.jackson.map.ObjectMapper;
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

import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletResponse;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Created by armdev on 6/4/14.
 */
public class FindComplaintByIdAPIControllerTest extends EasyMockSupport
{
    private MockMvc mockMvc;
    private MockHttpSession mockHttpSession;

    private FindComplaintByIdAPIController unit;

    private ComplaintDao mockComplaintDao;
    private ComplaintEventPublisher mockComplaintEventPublisher;
    private AcmSpringMvcErrorManager mockErrorManager;

    private Logger log = LoggerFactory.getLogger(getClass());

    @Before
    public void setUp() throws Exception
    {
        mockComplaintDao = createMock(ComplaintDao.class);
        mockComplaintEventPublisher = createMock(ComplaintEventPublisher.class);
        mockHttpSession = new MockHttpSession();
        mockErrorManager = createMock(AcmSpringMvcErrorManager.class);

        unit = new FindComplaintByIdAPIController();

        unit.setComplaintDao(mockComplaintDao);
        unit.setEventPublisher(mockComplaintEventPublisher);
        unit.setErrorManager(mockErrorManager);

        mockMvc = MockMvcBuilders.standaloneSetup(unit).build();
    }

    @Test
    public void findComplaintById() throws Exception
    {
        String ipAddress = "ipAddress";
        String title = "The Test Title";
        Long complaintId = 500L;

        Complaint returned = new Complaint();
        returned.setComplaintId(complaintId);
        returned.setComplaintTitle(title);

        mockHttpSession.setAttribute("acm_ip_address", ipAddress);

        expect(mockComplaintDao.find(Complaint.class, complaintId)).andReturn(returned);
        mockComplaintEventPublisher.publishFindComplaintByIdEvent(
                eq(returned),
                isNull(Authentication.class),
                eq(ipAddress),
                eq(true));

        replayAll();

        MvcResult result = mockMvc.perform(
                get("/api/v1/plugin/complaint/byId/{complaintId}", complaintId)
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .session(mockHttpSession))
                .andReturn();

        verifyAll();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE));

        String json = result.getResponse().getContentAsString();

        log.info("results: " + json);


        Complaint fromJson = new ObjectMapper().readValue(json, Complaint.class);

        assertNotNull(fromJson);
        assertEquals(returned.getComplaintTitle(), fromJson.getComplaintTitle());
    }

    @Test
    public void findComplaintById_notFound() throws Exception
    {
        String ipAddress = "ipAddress";
        Long complaintId = 500L;

        mockHttpSession.setAttribute("acm_ip_address", ipAddress);

        expect(mockComplaintDao.find(Complaint.class, complaintId)).andThrow(new PersistenceException());
        mockComplaintEventPublisher.publishFindComplaintByIdEvent(
                anyObject(Complaint.class),
                isNull(Authentication.class),
                eq(ipAddress),
                eq(false));
        mockErrorManager.sendErrorResponse(
                eq(HttpStatus.BAD_REQUEST),
                eq("Complaint ID '" + complaintId + "' not found."),
                anyObject(HttpServletResponse.class));

        replayAll();

        MvcResult result = mockMvc.perform(
                get("/api/v1/plugin/complaint/byId/{complaintId}", complaintId)
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .session(mockHttpSession))
                .andReturn();

        verifyAll();

    }

}