package org.opengroup.osdu.workflow.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResponseHeaderFilter implements Filter {

  @Autowired
  private DpsHeaders dpsHeaders;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    this.dpsHeaders.addCorrelationIdIfMissing();

    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
    httpServletResponse.setHeader(
        DpsHeaders.CORRELATION_ID, dpsHeaders.getCorrelationId());

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }
}
