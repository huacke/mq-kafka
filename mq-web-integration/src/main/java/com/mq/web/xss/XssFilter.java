package com.mq.web.xss;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * XSS过滤
 * @author
 */
public class XssFilter implements Filter {
	private FilterConfig filterConfig = null;
	private List<String> urlExclusion = null;

	@Override
	public void init(FilterConfig config) {
		this.filterConfig =config;
		List<String> urls = new ArrayList<>();
		urls.add("/mq/send");
		setUrlExclusion(urls);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		String servletPath = httpServletRequest.getServletPath();
		httpServletRequest.getParameterMap();
		if (urlExclusion != null && urlExclusion.contains(servletPath)) {
			chain.doFilter(request, response);
		} else {
			chain.doFilter(new XssHttpServletRequestWrapper(httpServletRequest), response);
		}
	}

	@Override
	public void destroy() {
		this.filterConfig = null;
	}

	public List<String> getUrlExclusion() {
		return urlExclusion;
	}

	public void setUrlExclusion(List<String> urlExclusion) {
		this.urlExclusion = urlExclusion;
	}
}