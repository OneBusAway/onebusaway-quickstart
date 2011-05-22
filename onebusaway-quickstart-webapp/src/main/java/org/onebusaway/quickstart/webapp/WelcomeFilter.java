package org.onebusaway.quickstart.webapp;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * I'm not sure why I REALLY need this, but Jetty doesn't seem to have the same
 * welcome file behavior as Tomcat. Aka it won't map "/where/standard" to
 * "/where/standard/index.action" automatically.
 * 
 * @author bdferris
 */
public class WelcomeFilter implements Filter {

  public void init(FilterConfig filterConfig) {

  }

  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    String path = ((HttpServletRequest) request).getServletPath();
    if (path.endsWith("/"))
      ((HttpServletResponse) response).sendRedirect(path + "index.action");
    else
      chain.doFilter(request, response);
  }

  public void destroy() {

  }
}
