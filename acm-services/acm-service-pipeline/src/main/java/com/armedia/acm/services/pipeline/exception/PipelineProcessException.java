package com.armedia.acm.services.pipeline.exception;

/*-
 * #%L
 * ACM Service: Pipeline
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

/**
 * Pipeline processing exception
 * Created by Petar Ilin <petar.ilin@armedia.com> on 26.07.2015.
 */
public class PipelineProcessException extends Exception
{
    /**
     * Constructor.
     * Create new exception with custom message
     *
     * @param message
     *            error message
     */
    public PipelineProcessException(String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * Wrap thrown exception
     *
     * @param exception
     *            exception to be wrapped
     */
    public PipelineProcessException(Exception exception)
    {
        super(exception);
    }
}
