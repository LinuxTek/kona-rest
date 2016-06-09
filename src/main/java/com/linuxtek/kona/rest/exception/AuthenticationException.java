/*
 * Copyright (C) 2016 LinuxTek, Inc  All Rights Reserved.
 */
package com.linuxtek.kona.rest.exception;

import javax.servlet.http.HttpServletResponse;

import com.linuxtek.kona.rest.annotation.RestException;

/**
 * Similar to ForbiddenException (403 Forbidden), but specifically for 
 * use when authentication is required and has failed or has not yet 
 * been provided.
 */
// Annotation not yet implemented; see com.linuxtek.kona.rest.DefaultRestErrorResolver
@RestException(status=HttpServletResponse.SC_UNAUTHORIZED)
public class AuthenticationException extends ApiException {
	private static final long serialVersionUID = 1L;

	public AuthenticationException() {
        super();
    }

    public AuthenticationException(final String message) {
        super(message);
    }

    public AuthenticationException(final Throwable cause) {
        super(cause);
    }

}
