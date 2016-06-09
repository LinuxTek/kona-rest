/*
 * Copyright (C) 2016 LinuxTek, Inc  All Rights Reserved.
 */
package com.linuxtek.kona.rest.exception;

import javax.servlet.http.HttpServletResponse;

import com.linuxtek.kona.rest.annotation.RestException;

/**
 * Thrown when an authenticated client makes a valid request, but
 * the client does not possess the necessary privileges to access the resource.
 * Unlike an UnauthorizedException (401 Unauthorized), authenticating 
 * will make no difference. For example, throw this exception
 * if an API rate limit is reached.
 */
// Annotation not yet implemented; see com.linuxtek.kona.rest.DefaultRestErrorResolver
@RestException(status=HttpServletResponse.SC_FORBIDDEN)
public class ForbiddenException extends ApiException {
	private static final long serialVersionUID = 1L;

	public ForbiddenException() {
        super();
    }

    public ForbiddenException(final String message) {
        super(message);
    }

    public ForbiddenException(final Throwable cause) {
        super(cause);
    }

}
