/*
 * Copyright (C) 2016 LinuxTek, Inc  All Rights Reserved.
 */
package com.linuxtek.kona.rest.exception;

import javax.servlet.http.HttpServletResponse;

import com.linuxtek.kona.rest.annotation.RestException;

// Annotation not yet implemented; see com.linuxtek.kona.rest.DefaultRestErrorResolver
@RestException(status=HttpServletResponse.SC_NOT_FOUND)
public final class NotFoundException extends ApiException {
	private static final long serialVersionUID = 1L;

	public NotFoundException() {
        super();
    }

    public NotFoundException(final String message, 
            final Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(final String message) {
        super(message);
    }

    public NotFoundException(final Throwable cause) {
        super(cause);
    }
}
