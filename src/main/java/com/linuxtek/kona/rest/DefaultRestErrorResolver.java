/*
 * Copyright 2012 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linuxtek.kona.rest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

/**
 * Default {@code RestErrorResolver} implementation that converts discovered Exceptions to
 * {@link RestError} instances.
 *
 * @author Les Hazlewood
 */
public class DefaultRestErrorResolver implements RestErrorResolver, MessageSourceAware, InitializingBean {

    public static final String DYNAMIC_DEVELOPER_MESSAGE_VALUE = "_developerMessage";
    public static final String DYNAMIC_CLIENT_MESSAGE_VALUE = "_clientMessage";
    public static final String DYNAMIC_MORE_INFO_MESSAGE_VALUE = "_moreInfoUrl";

    private static final Logger logger = LoggerFactory.getLogger(DefaultRestErrorResolver.class);

    private Map<String, RestError> exceptionMappings = Collections.emptyMap();

    private Map<String, String> exceptionMappingDefinitions = Collections.emptyMap();

    private MessageSource messageSource;
    private LocaleResolver localeResolver;

    private String defaultMoreInfoUrl;
    private boolean defaultEmptyCodeToStatus;
    private String defaultDeveloperMessage;

    public DefaultRestErrorResolver() {
        this.defaultEmptyCodeToStatus = false;
        //this.defaultDeveloperMessage = DYNAMIC_DEVELOPER_MESSAGE_KEY;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setLocaleResolver(LocaleResolver resolver) {
        this.localeResolver = resolver;
    }

    public void setExceptionMappingDefinitions(Map<String, String> exceptionMappingDefinitions) {
        this.exceptionMappingDefinitions = exceptionMappingDefinitions;
    }

    public void setDefaultMoreInfoUrl(String defaultMoreInfoUrl) {
        this.defaultMoreInfoUrl = defaultMoreInfoUrl;
    }

    public void setDefaultEmptyCodeToStatus(boolean defaultEmptyCodeToStatus) {
        this.defaultEmptyCodeToStatus = defaultEmptyCodeToStatus;
    }

    public void setDefaultDeveloperMessage(String defaultDeveloperMessage) {
        this.defaultDeveloperMessage = defaultDeveloperMessage;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        //populate with some defaults:
        Map<String, String> definitions = createDefaultExceptionMappingDefinitions();

        // TODO: get all list of all classes annotated with @RestException
        // and add them to the exceptionMapping

        //add in user-specified mappings (will override defaults as necessary):
        if (this.exceptionMappingDefinitions != null && !this.exceptionMappingDefinitions.isEmpty()) {
            definitions.putAll(this.exceptionMappingDefinitions);
        }

        this.exceptionMappings = toRestErrors(definitions);
    }

    protected final Map<String,String> createDefaultExceptionMappingDefinitions() {

        Map<String,String> m = new LinkedHashMap<String, String>();

        // 400
        applyDef(m, HttpMessageNotReadableException.class, HttpStatus.BAD_REQUEST);
        applyDef(m, MissingServletRequestParameterException.class, HttpStatus.BAD_REQUEST);
        applyDef(m, MethodArgumentNotValidException.class, HttpStatus.BAD_REQUEST);
        applyDef(m, MissingServletRequestPartException.class, HttpStatus.BAD_REQUEST);
        applyDef(m, BindException.class, HttpStatus.BAD_REQUEST);
        applyDef(m, TypeMismatchException.class, HttpStatus.BAD_REQUEST);
        applyDef(m, ServletRequestBindingException.class, HttpStatus.BAD_REQUEST);
        applyDef(m, "javax.validation.ValidationException", HttpStatus.BAD_REQUEST);

        // 404
        applyDef(m, NoHandlerFoundException.class, HttpStatus.NOT_FOUND);
        applyDef(m, NoSuchRequestHandlingMethodException.class, HttpStatus.NOT_FOUND);
        applyDef(m, "org.hibernate.ObjectNotFoundException", HttpStatus.NOT_FOUND);

        // 405
        applyDef(m, HttpRequestMethodNotSupportedException.class, HttpStatus.METHOD_NOT_ALLOWED);

        // 406
        applyDef(m, HttpMediaTypeNotAcceptableException.class, HttpStatus.NOT_ACCEPTABLE);

        // 409
        //can't use the class directly here as it may not be an available dependency:
        applyDef(m, "org.springframework.dao.DataIntegrityViolationException", HttpStatus.CONFLICT);

        // 415
        applyDef(m, HttpMediaTypeNotSupportedException.class, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        
        
        // 500
        applyDef(m, MissingPathVariableException.class, HttpStatus.INTERNAL_SERVER_ERROR);
        applyDef(m, ConversionNotSupportedException.class, HttpStatus.INTERNAL_SERVER_ERROR);
        applyDef(m, HttpMessageNotWritableException.class, HttpStatus.INTERNAL_SERVER_ERROR);

        return m;
    }

    private void applyDef(Map<String,String> m, Class<?> clazz, HttpStatus status) {
        applyDef(m, clazz.getName(), status);
    }

    protected String getContactEmail() {
        return "us";
    };

    private void applyDef(Map<String,String> m, String key, HttpStatus status) {
    	String clientMessage = null;
    	String developerMessage = null;
    	String moreInfoUrl = null;
    	
    	if (key.equals("org.springframework.dao.DataIntegrityViolationException")) {
    		developerMessage = "Internal data validation error. Please contact " 
                + getContactEmail() + " with API request details.";
    	} else if (key.equals("org.springframework.web.servlet.NoHandlerFoundException")) {
    		developerMessage = "Resource not found";
    	}
    	
        m.put(key, definitionFor(status, clientMessage, developerMessage, moreInfoUrl));
    }

    private String definitionFor(HttpStatus status, String clientMessage, String developerMessage, String moreInfoUrl) {
    	if (clientMessage == null) {
    		clientMessage = DYNAMIC_CLIENT_MESSAGE_VALUE;
    	}
    	
    	
    	if (developerMessage == null) {
    		developerMessage = DYNAMIC_DEVELOPER_MESSAGE_VALUE;
    	}
    	
    	if (moreInfoUrl == null) {
    		moreInfoUrl = DYNAMIC_MORE_INFO_MESSAGE_VALUE;
    	}
    	
        //return status.value() + ", " + DYNAMIC_DEVELOPER_MESSAGE_VALUE;
        return "status=" + status.value() 
        	+ ", code=0, clientMessage=" + clientMessage 
        	+ ", developerMessage=" + developerMessage 
        	+ ", moreInfoUrl=" + moreInfoUrl;
    }

    @Override
    public RestError resolveError(ServletWebRequest request, Object handler, Exception ex) {

        RestError template = getRestErrorTemplate(ex);
        if (template == null) {
            return null;
        }

        RestError.Builder builder = new RestError.Builder();
        builder.setStatus(getStatusValue(template, request, ex));
        builder.setCode(getCode(template, request, ex));
        builder.setClientMessage(getClientMessage(template, request, ex));
        builder.setDeveloperMessage(getDeveloperMessage(template, request, ex));
        builder.setMoreInfoUrl(getMoreInfoUrl(template, request, ex));
        builder.setThrowable(ex);
        return builder.build();
    }

    protected int getStatusValue(RestError template, ServletWebRequest request, Exception ex) {
        return template.getStatus().value();
    }

    protected int getCode(RestError template, ServletWebRequest request, Exception ex) {
        int code = template.getCode();
        if ( code <= 0 && defaultEmptyCodeToStatus) {
            code = getStatusValue(template, request, ex);
        }
        return code;
    }

    protected String getMoreInfoUrl(RestError template, ServletWebRequest request, Exception ex) {
        String moreInfoUrl = template.getMoreInfoUrl();
        
        if (moreInfoUrl != null && (moreInfoUrl.equalsIgnoreCase("null") || moreInfoUrl.equalsIgnoreCase("off"))) {
            return null;
        }
        
        if (moreInfoUrl != null && moreInfoUrl.equals(DYNAMIC_MORE_INFO_MESSAGE_VALUE)) {
        	if (ex instanceof AbstractRestException) {
        		AbstractRestException rex = (AbstractRestException) ex;
        		moreInfoUrl = rex.getMoreInfoUrl();
        	} else {
        		moreInfoUrl = this.defaultMoreInfoUrl;
        	}
        }
        
        if (moreInfoUrl == null || moreInfoUrl.trim().length()==0) {
            moreInfoUrl = this.defaultMoreInfoUrl;
        }
        
        return moreInfoUrl;
    }

    protected String getClientMessage(RestError template, ServletWebRequest request, Exception ex) {
        return getMessage(template.getClientMessage(), request, ex);
    }

    protected String getDeveloperMessage(RestError template, ServletWebRequest request, Exception ex) {
        String developerMessage = template.getDeveloperMessage();
        if (developerMessage == null && defaultDeveloperMessage != null) {
            developerMessage = defaultDeveloperMessage;
        }
        return getMessage(developerMessage, request, ex);
    }

    /**
     * Returns the response status message to return to the client, or {@code null} if no
     * status message should be returned.
     *
     * @return the response status message to return to the client, or {@code null} if no
     *         status message should be returned.
     */
    protected String getMessage(String message, ServletWebRequest webRequest, Exception ex) {

        logger.debug("getMessage(): raw message value: [" + message + "]");
        
        if (message != null && ex != null) {
            if (message.equalsIgnoreCase("null") || message.equalsIgnoreCase("off")) {
                return null;
            }
            
            if (message.equalsIgnoreCase(DYNAMIC_DEVELOPER_MESSAGE_VALUE)) {
            	if (ex instanceof AbstractRestException) {
            		AbstractRestException rex = (AbstractRestException) ex;
            		message = rex.getDeveloperMessage();
            	} else {
            		message = ex.getMessage();
            	}
            } else if (message.equalsIgnoreCase(DYNAMIC_CLIENT_MESSAGE_VALUE)) {
            	if (ex instanceof AbstractRestException) {
            		AbstractRestException rex = (AbstractRestException) ex;
            		message = rex.getClientMessage();
            	} else {
            		message = getDefaultClientMessage();
            	}
        	} 
            
            if (messageSource != null) {
                Locale locale = null;
                if (localeResolver != null) {
                    locale = localeResolver.resolveLocale(webRequest.getRequest());
                }
                message = messageSource.getMessage(message, null, message, locale);
            }
        }

        return message;
    }
    
    /**
     * Override this method to return a default message to the client on an exception that does not implement getClientMessage().
     * @return
     */
    protected String getDefaultClientMessage() {
    	return null;
    }

    /**
     * Returns the config-time 'template' RestError instance configured for the specified Exception, or
     * {@code null} if a match was not found.
     * <p/>
     * The config-time template is used as the basis for the RestError constructed at runtime.
     * @param ex
     * @return the template to use for the RestError instance to be constructed.
     */
    protected RestError getRestErrorTemplate(Exception ex) {
        Map<String, RestError> mappings = this.exceptionMappings;

        if (CollectionUtils.isEmpty(mappings)) {
            return null;
        }

        RestError template = null;

        String dominantMapping = null;

        int deepest = Integer.MAX_VALUE;

        for (Map.Entry<String, RestError> entry : mappings.entrySet()) {
            String key = entry.getKey();

            int depth = getDepth(key, ex);

            if (depth >= 0 && depth < deepest) {
                deepest = depth;
                dominantMapping = key;
                template = entry.getValue();
            }
        }

        if (template != null && logger.isDebugEnabled()) {
            logger.debug("Resolving to RestError template '" + template + "' for exception of type [" + ex.getClass().getName() +
                    "], based on exception mapping [" + dominantMapping + "]");
        }

        return template;
    }

    /**
     * Return the depth to the superclass matching.
     * <p>0 means ex matches exactly. Returns -1 if there's no match.
     * Otherwise, returns depth. Lowest depth wins.
     */
    protected int getDepth(String exceptionMapping, Exception ex) {
        return getDepth(exceptionMapping, ex.getClass(), 0);
    }

    private int getDepth(String exceptionMapping, Class<?> exceptionClass, int depth) {
        if (exceptionMapping.equalsIgnoreCase("throwable")) {
            return depth;
        }

        if (exceptionClass.getName().contains(exceptionMapping)) {
            // Found it!
            return depth;
        }
        // If we've gone as far as we can go and haven't found it...
        if (exceptionClass.equals(Throwable.class)) {
            return -1;
        }

        return getDepth(exceptionMapping, exceptionClass.getSuperclass(), depth + 1);
    }


    protected Map<String, RestError> toRestErrors(Map<String, String> smap) {
        if (CollectionUtils.isEmpty(smap)) {
            return Collections.emptyMap();
        }

        Map<String, RestError> map = new LinkedHashMap<String, RestError>(smap.size());

        for (Map.Entry<String, String> entry : smap.entrySet()) {
            String key = entry.getKey();

            String value = entry.getValue();

            RestError template = toRestError(value);

            map.put(key, template);
        }

        return map;
    }

    protected RestError toRestError(String exceptionConfig) {
        String[] values = StringUtils.commaDelimitedListToStringArray(exceptionConfig);

        if (values == null || values.length == 0) {
            throw new IllegalStateException("Invalid config mapping.  Exception names must map to a string configuration.");
        }

        RestError.Builder builder = new RestError.Builder();

        boolean statusSet = false;
        boolean codeSet = false;
        boolean clientMessageSet = false;
        boolean developerMessageSet = false;
        boolean moreInfoSet = false;

        for (String value : values) {

            String trimmedVal = StringUtils.trimWhitespace(value);

            logger.debug("toRestError: parsing value: [" + trimmedVal+"]");

            //check to see if the value is an explicitly named key/value pair:
            String[] pair = StringUtils.split(trimmedVal, "=");

            if (pair != null) {
                //explicit attribute set:
                String pairKey = StringUtils.trimWhitespace(pair[0]);

                if (!StringUtils.hasText(pairKey)) {
                    pairKey = null;
                }

                String pairValue = StringUtils.trimWhitespace(pair[1]);

                if (!StringUtils.hasText(pairValue)) {
                    pairValue = null;
                }

                if ("status".equalsIgnoreCase(pairKey)) {
                    int statusCode = getRequiredInt(pairKey, pairValue);
                    builder.setStatus(statusCode);
                    statusSet = true;
                } else if ("code".equalsIgnoreCase(pairKey)) {
                    int code = getRequiredInt(pairKey, pairValue);
                    builder.setCode(code);
                    codeSet = true;
                } else if ("clientMessage".equalsIgnoreCase(pairKey)) {
                    builder.setClientMessage(pairValue);
                    clientMessageSet = true;
                } else if ("developerMessage".equalsIgnoreCase(pairKey)) {
                    builder.setDeveloperMessage(pairValue);
                    developerMessageSet = true;
                } else if ("moreInfoUrl".equalsIgnoreCase(pairKey)) {
                    builder.setMoreInfoUrl(pairValue);
                    moreInfoSet = true;
                }
            } else {
                //not a key/value pair - use heuristics to determine what value is being set:
                int val;
                if (!statusSet) {
                    val = getInt("status", trimmedVal);
                    if (val > 0) {
                        builder.setStatus(val);
                        statusSet = true;
                        continue;
                    }
                }
                if (!codeSet) {
                    val = getInt("code", trimmedVal);
                    if (val > 0) {
                        builder.setCode(val);
                        codeSet = true;
                        continue;
                    }
                }
                if (!moreInfoSet && trimmedVal.toLowerCase().startsWith("http")) {
                    builder.setMoreInfoUrl(trimmedVal);
                    moreInfoSet = true;
                    continue;
                }
                if (!clientMessageSet) {
                    logger.debug("setting message to: " + trimmedVal);
                    builder.setClientMessage(trimmedVal);
                    clientMessageSet = true;
                    continue;
                }
                if (!developerMessageSet) {
                    logger.debug("setting developerMessage to: " + trimmedVal);
                    builder.setDeveloperMessage(trimmedVal);
                    developerMessageSet = true;
                    continue;
                }
                if (!moreInfoSet) {
                    builder.setMoreInfoUrl(trimmedVal);
                    moreInfoSet = true;
                    //noinspection UnnecessaryContinue
                    continue;
                }
            }
        }

        return builder.build();
    }

    private static int getRequiredInt(String key, String value) {
        try {
            int anInt = Integer.valueOf(value);
            return Math.max(-1, anInt);
        } catch (NumberFormatException e) {
            String message = "Configuration element '" + key + "' requires an integer value.  The value " +
                    "specified: " + value;
            throw new IllegalArgumentException(message, e);
        }
    }

    private static int getInt(String key, String value) {
        try {
            return getRequiredInt(key, value);
        } catch (IllegalArgumentException iae) {
            return 0;
        }
    }
}
