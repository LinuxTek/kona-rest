package com.linuxtek.kona.rest;

public abstract class AbstractRestException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private String clientMessage = null;
    private String developerMessage = null;
    private String moreInfoUrl = null;
    
    public AbstractRestException() {
        super();
    }

    public AbstractRestException(final String message, final Throwable cause) {
        super(message, cause);
        this.developerMessage = message;
    }

    public AbstractRestException(final String message) {
        super(message);
        this.developerMessage = message;
    }

    public AbstractRestException(final Throwable cause) {
        super(cause);
    }
    
    public void setClientMessage(String clientMessage) {
        this.clientMessage = clientMessage;
    }

    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }
    
    public String getClientMessage() {
        return clientMessage;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public void setMoreInfoUrl(String moreInfoUrl) {
        this.moreInfoUrl = moreInfoUrl;
    }

    public String getMoreInfoUrl() {
        return moreInfoUrl;
    }
}
