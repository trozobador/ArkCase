package com.armedia.acm.web.api.service;

/*-
 * #%L
 * ACM Shared Web Artifacts
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by jovan.ivanovski on 10/7/2016.
 */
public class ApplicationMetaInfoService implements InitializingBean, ServletContextAware
{

    private final Logger log = LoggerFactory.getLogger(getClass());
    ServletContext servletContext;
    private String version;

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    @Override
    public void setServletContext(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    public void findVersion()
    {
        Properties prop = new Properties();

        try (InputStream manifestStream = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF"))
        {
            prop.load(manifestStream);
            version = prop.getProperty("Implementation-Version", "");
        }
        catch (IOException e)
        {
            log.warn("Could not open manifest file: {}", e.getMessage(), e);
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        findVersion();
    }

}
