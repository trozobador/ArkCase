package com.armedia.acm.service.outlook.service.impl;

import com.armedia.acm.service.outlook.dao.OutlookDao;
import com.armedia.acm.service.outlook.exception.AcmOutlookConnectionFailedException;
import com.armedia.acm.service.outlook.exception.AcmOutlookFindItemsFailedException;
import com.armedia.acm.service.outlook.exception.AcmOutlookListItemsFailedException;
import com.armedia.acm.service.outlook.model.AcmOutlookUser;
import com.armedia.acm.service.outlook.model.OutlookCalendarItem;
import com.armedia.acm.service.outlook.model.OutlookContactItem;
import com.armedia.acm.service.outlook.model.OutlookItem;
import com.armedia.acm.service.outlook.model.OutlookMailItem;
import com.armedia.acm.service.outlook.model.OutlookResults;
import com.armedia.acm.service.outlook.model.OutlookTaskItem;
import com.armedia.acm.service.outlook.service.OutlookService;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.core.service.item.Contact;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.item.Task;
import microsoft.exchange.webservices.data.core.service.schema.AppointmentSchema;
import microsoft.exchange.webservices.data.core.service.schema.ContactSchema;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.TaskSchema;
import microsoft.exchange.webservices.data.enumeration.EmailAddressKey;
import microsoft.exchange.webservices.data.enumeration.PhoneNumberKey;
import microsoft.exchange.webservices.data.enumeration.WellKnownFolderName;
import microsoft.exchange.webservices.data.exception.ServiceLocalException;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by armdev on 4/20/15.
 */
public class OutlookServiceImpl implements OutlookService
{
    private transient final Logger log = LoggerFactory.getLogger(getClass());

    private OutlookDao dao;

    @Override
    public OutlookResults<OutlookMailItem> findMailItems(AcmOutlookUser user, int start, int maxItems, String sortField,
                                                         boolean sortAscending)
            throws AcmOutlookConnectionFailedException, AcmOutlookListItemsFailedException
    {
        ExchangeService service = connect(user);

        PropertySet mailProperties = new PropertySet(
                EmailMessageSchema.From,
                EmailMessageSchema.Sender,
                EmailMessageSchema.IsRead
        );

        FindItemsResults<Item> items = getDao().findItems(service, WellKnownFolderName.Inbox, mailProperties, start,
                maxItems, sortField, sortAscending);

        OutlookResults<OutlookMailItem> results = new OutlookResults<>();
        populateResultHeaderFields(results, start, maxItems, sortField, sortAscending, items.getTotalCount(),
                items.isMoreAvailable(), items.getNextPageOffset() == null ? -1 : items.getNextPageOffset());

        List<OutlookMailItem> messages = items.getItems().stream().map(this::messageFrom).collect(Collectors.toList());
        results.setItems(messages);

        return results;
    }

    @Override
    public OutlookResults<OutlookTaskItem> findTaskItems(AcmOutlookUser user, int start, int maxItems, String sortField,
                                                         boolean sortAscending)
            throws AcmOutlookConnectionFailedException, AcmOutlookListItemsFailedException
    {
        ExchangeService service = connect(user);

        PropertySet taskProperties = new PropertySet(
                TaskSchema.DueDate,
                TaskSchema.StartDate,
                TaskSchema.CompleteDate,
                TaskSchema.IsComplete,
                TaskSchema.PercentComplete
        );

        FindItemsResults<Item> items = getDao().findItems(service, WellKnownFolderName.Tasks, taskProperties, start,
            maxItems, sortField, sortAscending);

        OutlookResults<OutlookTaskItem> results = new OutlookResults<>();
        populateResultHeaderFields(results, start, maxItems, sortField, sortAscending, items.getTotalCount(),
                items.isMoreAvailable(), items.getNextPageOffset() == null ? - 1 : items.getNextPageOffset());

        List<OutlookTaskItem> tasks = items.getItems().stream().map(this::taskFrom).collect(Collectors.toList());
        results.setItems(tasks);

        return results;
    }

    private void populateResultHeaderFields(OutlookResults<? extends OutlookItem> results, int start, int maxItems,
                                            String sortField, boolean sortAscending, int totalCount,
                                            boolean isMoreAvailable, int nextStartRow)
    {
        results.setTotalItems(totalCount);
        results.setMoreItemsAvailable(isMoreAvailable);
        results.setCurrentStartIndex(start);
        results.setCurrentMaxItems(maxItems);
        results.setNextStartIndex(nextStartRow);
        results.setCurrentSortField(sortField);
        results.setCurrentSortAscending(sortAscending);
    }

    private OutlookMailItem messageFrom(Item item)
    {
        EmailMessage message = (EmailMessage) item;

        OutlookMailItem omi = new OutlookMailItem();

        try
        {
            populateCoreFields(item, omi);

            omi.setFrom(message.getFrom() == null ? null : message.getFrom().getAddress());
            omi.setSender(message.getSender() == null ? null : message.getSender().getAddress());
            omi.setRead(message.getIsRead());

            return omi;
        }
        catch (ServiceLocalException sle)
        {
            throw new AcmOutlookFindItemsFailedException(sle.getMessage(), sle);
        }
    }

    private OutlookTaskItem taskFrom(Item item)
    {
        Task task = (Task) item;

        OutlookTaskItem oti = new OutlookTaskItem();

        try
        {
            populateCoreFields(item, oti);

            oti.setComplete(task.getIsComplete());
            oti.setCompleteDate(task.getCompleteDate());
            oti.setDueDate(task.getDueDate());
            oti.setPercentComplete(task.getPercentComplete());
            oti.setStartDate(task.getStartDate());

            return oti;
        }
        catch (ServiceLocalException sle)
        {
            throw new AcmOutlookFindItemsFailedException(sle.getMessage(), sle);
        }
    }

    private OutlookCalendarItem calendarFrom(Item item)
    {
        Appointment appt = (Appointment) item;

        OutlookCalendarItem oci = new OutlookCalendarItem();

        try
        {
            populateCoreFields(item, oci);

            oci.setAllDayEvent(appt.getIsAllDayEvent());
            oci.setCancelled(appt.getIsCancelled());
            oci.setMeeting(appt.getIsMeeting());
            oci.setRecurring(appt.getIsRecurring());
            oci.setStartDate(appt.getStart());
            oci.setEndDate(appt.getEnd());

            return oci;
        }
        catch (ServiceLocalException sle)
        {
            throw new AcmOutlookFindItemsFailedException(sle.getMessage(), sle);
        }
    }

    private OutlookContactItem contactFrom(Item item)
    {
        Contact contact = (Contact) item;

        OutlookContactItem oci = new OutlookContactItem();

        try
        {
            populateCoreFields(item, oci);

            oci.setSurname(contact.getSurname());
            oci.setDisplayName(contact.getDisplayName());
            oci.setCompleteName(contact.getCompleteName() == null ? null : contact.getCompleteName().getFullName());
            oci.setCompanyName(contact.getCompanyName());
            if(contact.getPhoneNumbers().contains(PhoneNumberKey.PrimaryPhone))
                oci.setPrimaryTelephone(contact.getPhoneNumbers().getPhoneNumber(PhoneNumberKey.PrimaryPhone));
            if(contact.getEmailAddresses().contains(EmailAddressKey.EmailAddress1))
                oci.setEmailAddress1(contact.getEmailAddresses().getEmailAddress(EmailAddressKey.EmailAddress1));
            if(contact.getEmailAddresses().contains(EmailAddressKey.EmailAddress2))
                oci.setEmailAddress2(contact.getEmailAddresses().getEmailAddress(EmailAddressKey.EmailAddress2));

            return oci;
        }
        catch (ServiceLocalException sle)
        {
            throw new AcmOutlookFindItemsFailedException(sle.getMessage(), sle);
        }
    }

    private void populateCoreFields(Item fromOutlook, OutlookItem coreItem) throws ServiceLocalException
    {
        coreItem.setSubject(fromOutlook.getSubject());
        coreItem.setSent(fromOutlook.getDateTimeSent());
        coreItem.setModified(fromOutlook.getLastModifiedTime());
        coreItem.setId(fromOutlook.getId().toString());
        coreItem.setBody(fromOutlook.getBody() == null ? null : fromOutlook.getBody().toString());
        coreItem.setCreated(fromOutlook.getDateTimeCreated());
        coreItem.setSize(fromOutlook.getSize());
    }

    @Override
    public OutlookResults<OutlookCalendarItem> findCalendarItems(AcmOutlookUser user, int start, int maxItems, String sortField,
                                                                 boolean sortAscending)
            throws AcmOutlookConnectionFailedException, AcmOutlookListItemsFailedException
    {
        ExchangeService service = connect(user);

        PropertySet calendarProperties = new PropertySet(
                AppointmentSchema.IsAllDayEvent,
                AppointmentSchema.IsCancelled,
                AppointmentSchema.IsMeeting,
                AppointmentSchema.IsRecurring,
                AppointmentSchema.Start,
                AppointmentSchema.End
        );

        FindItemsResults<Item> items = getDao().findItems(service, WellKnownFolderName.Calendar, calendarProperties, start,
                maxItems, sortField, sortAscending);

        OutlookResults<OutlookCalendarItem> results = new OutlookResults<>();
        populateResultHeaderFields(results, start, maxItems, sortField, sortAscending, items.getTotalCount(),
                items.isMoreAvailable(), items.getNextPageOffset() == null ? - 1 : items.getNextPageOffset());

        List<OutlookCalendarItem> appts = items.getItems().stream().map(this::calendarFrom).collect(Collectors.toList());
        results.setItems(appts);

        return results;
    }

    @Override
    public OutlookResults<OutlookContactItem> findContactItems(AcmOutlookUser user, int start, int maxItems, String sortField,
                                                               boolean sortAscending)
            throws AcmOutlookConnectionFailedException, AcmOutlookListItemsFailedException
    {
        ExchangeService service = connect(user);

        PropertySet contactProperties = new PropertySet(
                ContactSchema.Surname,
                ContactSchema.DisplayName,
                ContactSchema.CompleteName,
                ContactSchema.CompanyName,
                ContactSchema.PrimaryPhone,
                ContactSchema.EmailAddress1,
                ContactSchema.EmailAddress2
        );

        FindItemsResults<Item> items = getDao().findItems(service, WellKnownFolderName.Contacts, contactProperties, start,
                maxItems, sortField, sortAscending);

        OutlookResults<OutlookContactItem> results = new OutlookResults<>();
        populateResultHeaderFields(results, start, maxItems, sortField, sortAscending, items.getTotalCount(),
                items.isMoreAvailable(), items.getNextPageOffset() == null ? - 1 : items.getNextPageOffset());

        List<OutlookContactItem> contacts = items.getItems().stream().map(this::contactFrom).collect(Collectors.toList());
        results.setItems(contacts);

        return results;
    }

    protected ExchangeService connect(AcmOutlookUser user) throws AcmOutlookConnectionFailedException
    {
        return getDao().connect(user);
    }

    public OutlookDao getDao()
    {
        return dao;
    }

    public void setDao(OutlookDao dao)
    {
        this.dao = dao;
    }
}
