/*
 * Copyright (C) 2016 LinuxTek, Inc  All Rights Reserved.
 */
package com.linuxtek.kona.rest.exception;

import javax.servlet.http.HttpServletResponse;

import com.linuxtek.kona.rest.annotation.RestException;

/**
 * Thrown on validation error.
 */
// Annotation not yet implemented; see com.linuxtek.kona.rest.DefaultRestErrorResolver
@RestException(status=HttpServletResponse.SC_PRECONDITION_FAILED)
public class ValidationException extends ApiException {
	private static final long serialVersionUID = 1L;

	public ValidationException() {
        super();
    }

    public ValidationException(final String message,
            final Throwable cause) {
        super(message, cause);
    }

    public ValidationException(final String message) {
        super(message);
    }

    public ValidationException(final Throwable cause) {
        super(cause);
    }
}
