package com.armedia.acm.plugins.complaint;

import com.armedia.acm.plugins.complaint.model.Complaint;
import com.armedia.acm.plugins.complaint.service.SaveComplaintTransaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring/spring-library-data-source.xml",
        "/spring/spring-library-complaint-plugin-test.xml",
        "/spring/spring-library-complaint.xml",
        "/spring/spring-library-mule-context-manager.xml",
        "/spring/spring-library-person.xml"})
@TransactionConfiguration(defaultRollback = false, transactionManager = "transactionManager")
public class ComplaintMuleIT
{

    @Autowired
    private SaveComplaintTransaction saveComplaintTransaction;

    private ComplaintFactory complaintFactory = new ComplaintFactory();

    private Logger log = LoggerFactory.getLogger(getClass());

    @Test
    @Transactional
    public void saveComplaintFlow() throws Exception
    {
        Complaint complaint = complaintFactory.complaint();

        Authentication auth = new UsernamePasswordAuthenticationToken("testUser", "testUser");

        Complaint saved = saveComplaintTransaction.saveComplaint(complaint, auth);

        assertNotNull(saved.getComplaintId());

        log.info("New complaint id: " + saved.getComplaintId());
    }
}