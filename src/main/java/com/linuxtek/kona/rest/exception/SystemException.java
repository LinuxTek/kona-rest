/*
 * Copyright (C) 2016 LinuxTek, Inc  All Rights Reserved.
 */
package com.linuxtek.kona.rest.exception;

import javax.servlet.http.HttpServletResponse;

import com.linuxtek.kona.rest.annotation.RestException;

/**
 * Thrown on internal server error.
 */
// Annotation not yet implemented; see com.linuxtek.kona.rest.DefaultRestErrorResolver
@RestException(status=HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
public class SystemException extends ApiException {
	//private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SystemException() {
        super();
    }

    public SystemException(final String message,
            final Throwable cause) {
        super(message, cause);
    }

    public SystemException(final String message) {
        super(message);
    }

    public SystemException(final Throwable cause) {
        super(cause);
    }
}
