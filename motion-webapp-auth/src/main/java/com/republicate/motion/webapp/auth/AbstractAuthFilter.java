package com.republicate.motion.webapp.auth;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.republicate.motion.webapp.MotionFilter;
import org.apache.velocity.tools.view.JeeConfig;
import org.apache.velocity.tools.view.JeeFilterConfig;
import org.apache.velocity.tools.view.ServletUtils;
import org.apache.velocity.util.ExtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * <p>Authentication filter skeleton.</p>
 * <p>The filter should map all protected resources URIs. It can also map a wider range, provided
 * you have configured the <code>auth.protected</code> URI regex or overridden
 * the <code>isProtectedURI(uri)</code> method.</p>
 * <p>Configuration parameters:</p>
 * <ul>
 * <li><code>auth.protected</code> - regular expression meant to match all resources URIs which are to be protected ;
 * by default all mapped resources are protected.
 *</ul>
 */

public abstract class AbstractAuthFilter<USER> extends MotionFilter
{
    protected static Logger logger = LoggerFactory.getLogger("auth");

    // config parameters keys

    public static final String PROTECTED_RESOURCES = "auth.protected";

    protected boolean preFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    {
        return true;
    }

    protected boolean isProtectedURI(String uri)
    {
        if (protectedResources != null)
        {
            return protectedResources.matcher(uri).matches();
        }
        else
        {
            return true;
        }
    }

    protected void processPublicRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException
    {
        filterChain.doFilter(request, response);
    }

    protected USER getAuthentifiedUser(HttpServletRequest request) throws ServletException
    {
        return authenticate(request);
    }

    protected abstract USER authenticate(HttpServletRequest request);

    protected void processProtectedRequest(USER logged, HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException
    {
        logger.debug("user going towards {}: {}", request.getRequestURI(), displayUser(logged));
        filterChain.doFilter(request, response);
    }

    protected void processForbiddenRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException
    {
        logger.debug("unauthorized request towards {}", request.getRequestURI());
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    /* Filter interface */

    /**
     * <p>Filter initialization.</p>
     * <p>Child classes should start with <code>super.init(cilterConfig);</code>.</p>
     * <p>Once parent has been initialized, <code>getConfig()</code> returns a JeeConfig.</p>
     * @param filterConfig filter config
     * @throws ServletException in case of error
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        super.init(filterConfig);
        String protectedResourcesPattern = findConfigParameter(PROTECTED_RESOURCES);
        if (protectedResourcesPattern != null)
        {
            try
            {
                protectedResources = Pattern.compile(protectedResourcesPattern);
            }
            catch (PatternSyntaxException pse)
            {
                throw new ServletException("could not configure protected resources pattern", pse);
            }
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        if (!preFilter(request, response, chain))
        {
            return;
        }

        if (isProtectedURI(request.getRequestURI()))
        {
            USER user = getAuthentifiedUser(request);
            if (user == null)
            {
                processForbiddenRequest(request, response, chain);
            }
            else
            {
                processProtectedRequest(user, request, response, chain);
            }
        }
        else
        {
            processPublicRequest(request, response, chain);
        }
    }

    @Override
    public void destroy()
    {

    }

    /**
     * <p>What to display in the logs for a user, like in the logs.</p>
     * <p>A child class can change it to return something like
     * <code>return user.getString('login');</code>.</p>
     * @param user target user
     * @return string representation for the user
     */
    protected String displayUser(USER user)
    {
        // user.getString
        return String.valueOf(user);
    }

   private Pattern protectedResources = null;

    // for oauth
    // ...
}
