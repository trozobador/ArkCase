package com.armedia.acm.services.notification.service;

/*-
 * #%L
 * ACM Service: Notification
 * %%
 * Copyright (C) 2014 - 2018 ArkCase LLC
 * %%
 * This file is part of the ArkCase software. 
 * 
 * If the software was purchased under a paid ArkCase license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.armedia.acm.core.AcmApplication;
import com.armedia.acm.core.AcmNotifiableEntity;
import com.armedia.acm.core.AcmObjectType;
import com.armedia.acm.data.AcmNotificationDao;
import com.armedia.acm.data.service.AcmDataService;
import com.armedia.acm.services.notification.model.NotificationConfig;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class NotificationUtils
{
    private NotificationConfig notificationConfig;

    private AcmApplication acmAppConfiguration;

    private AcmDataService acmDataService;

    public String buildNotificationLink(String parentType, Long parentId, String relatedObjectType, Long relatedObjectId)
    {
        String baseUrl = notificationConfig.getBaseUrl();
        // If relatedObjectType is null, the parent object is TOP LEVEL (Case File or Complaint)
        // else parent object is nested in top level object
        String linkedObjectType = StringUtils.isEmpty(relatedObjectType) ? parentType : relatedObjectType;
        Long linkedObjectId = StringUtils.isEmpty(relatedObjectType) ? parentId : relatedObjectId;

        // find the object type from the ACM application configuration, and get the URL from the object type
        for (AcmObjectType objectType : getAcmAppConfiguration().getObjectTypes())
        {
            Map<String, String> urlValues = objectType.getUrl();
            if (objectType.getName().equals(parentType) && StringUtils.isNotEmpty(linkedObjectType))
            {
                String objectUrl = urlValues.get(linkedObjectType);
                if (StringUtils.isNotEmpty(objectUrl))
                {
                    objectUrl = String.format(objectUrl, linkedObjectId);
                    return String.format("%s%s", baseUrl, objectUrl);
                }
            }
        }

        return null;
    }

    public String getNotificationParentOrRelatedObjectNumber(String objectType, Long objectId)
    {
        if (objectType != null && objectId != null)
        {
            AcmNotificationDao dao = getAcmDataService().getNotificationDaoByObjectType(objectType);
            if (dao != null)
            {
                AcmNotifiableEntity entity = dao.findEntity(objectId);
                if (entity != null)
                {
                    return entity.getNotifiableEntityTitle();
                }
            }
        }
        return null;
    }

    public NotificationConfig getNotificationConfig()
    {
        return notificationConfig;
    }

    public void setNotificationConfig(NotificationConfig notificationConfig)
    {
        this.notificationConfig = notificationConfig;
    }

    public AcmApplication getAcmAppConfiguration()
    {
        return acmAppConfiguration;
    }

    public void setAcmAppConfiguration(AcmApplication acmAppConfiguration)
    {
        this.acmAppConfiguration = acmAppConfiguration;
    }

    public AcmDataService getAcmDataService()
    {
        return acmDataService;
    }

    public void setAcmDataService(AcmDataService acmDataService)
    {
        this.acmDataService = acmDataService;
    }
}
