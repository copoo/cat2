package com.dianping.cat.report.page.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpStatus;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportFilterManager;
import org.unidal.cat.spi.remote.DefaultRemoteContext;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.remote.RemoteSkeleton;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.report.ReportPage;

public class Handler implements PageHandler<Context> {
	@Inject
	private RemoteSkeleton m_skeleton;

	@Inject
	private ReportFilterManager m_manager;

	@SuppressWarnings("unchecked")
	private RemoteContext buildContext(HttpServletRequest req, Payload payload) {
		ReportFilter<Report> filter = m_manager.getFilter(payload.getName(), payload.getFilterId());
		DefaultRemoteContext ctx = new DefaultRemoteContext(payload.getName(), payload.getDomain(), //
		      payload.getStartTime(), payload.getPeriod(), filter);
		List<String> names = Collections.list(req.getParameterNames());

		for (String name : names) {
			String value = req.getParameter(name);

			ctx.setProperty(name, value);
		}

		return ctx;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "service")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "service")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.SERVICE);

		if (ctx.hasErrors()) {
			ctx.getHttpServletResponse().sendError(HttpStatus.SC_BAD_REQUEST, "Bad Request");
			ctx.stopProcess();
		} else {
			OutputStream out = ctx.getHttpServletResponse().getOutputStream();

			RemoteContext rc = buildContext(ctx.getHttpServletRequest(), payload);
			boolean success = m_skeleton.handleReport(rc, out);

			if (!success) {
				Cat.logEvent("Service", "MissingReport", Message.SUCCESS, rc.buildURL(""));
			}
		}
	}
}
