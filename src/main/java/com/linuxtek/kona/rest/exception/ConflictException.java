/*
 * Copyright (C) 2016 LinuxTek, Inc  All Rights Reserved.
 */
package com.linuxtek.kona.rest.exception;

import javax.servlet.http.HttpServletResponse;

import com.linuxtek.kona.rest.annotation.RestException;

/**
 * The request could not be completed due to a conflict with the current
 * state of the resource.
 */
// Annotation not yet implemented; see com.linuxtek.kona.rest.DefaultRestErrorResolver
@RestException(status=HttpServletResponse.SC_CONFLICT)
public final class ConflictException extends ApiException {
	private static final long serialVersionUID = 1L;

	public ConflictException() {
        super();
    }

    public ConflictException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ConflictException(final String message) {
        super(message);
    }

    public ConflictException(final Throwable cause) {
        super(cause);
    }
}
