package org.unidal.cat.spi.report.internals;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.dianping.cat.helper.TimeHelper;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportFilterManager;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.ReportStoragePolicy;
import org.unidal.cat.spi.remote.DefaultRemoteContext;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.cat.spi.report.provider.ReportProvider;
import org.unidal.cat.spi.report.storage.FileReportStorage;
import org.unidal.cat.spi.report.storage.ReportStorage;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.RoleHintEnabled;

public abstract class AbstractReportManager<T extends Report> implements ReportManager<T>, RoleHintEnabled {
	@Inject
	private ReportProvider<T> m_provider;

	@Inject
	private ReportStorage<T> m_storage;

	@Inject(FileReportStorage.ID)
	private ReportStorage<T> m_fileStorage;

	@Inject
	private ReportDelegateManager m_delegateManager;

	@Inject
	private ReportFilterManager m_filterManager;

	private String m_reportName;

	private ConcurrentMap<Long, ConcurrentMap<String, T>> m_reports = new ConcurrentHashMap<Long, ConcurrentMap<String, T>>();

	@Override
	public void doCheckpoint(int hour, int index, boolean atEnd) throws IOException {
		Date startTime = new Date(TimeUnit.HOURS.toMillis(hour));
		long key = startTime.getTime() + index;
		ConcurrentMap<String, T> map = m_reports.get(key);

		if (map != null && map.size() > 0) {
			List<T> reports = new ArrayList<T>(map.values());

			for (T report : reports) {
				if (atEnd) {
					m_storage.store(getDelegate(), ReportPeriod.HOUR, report, index, ReportStoragePolicy.FILE_AND_MYSQL);
				} else {
					m_storage.store(getDelegate(), ReportPeriod.HOUR, report, index, ReportStoragePolicy.FILE);
				}
			}
		}
	}

	@Override
	public void removeReport(int hour, int index){
		Date startTime = new Date(TimeUnit.HOURS.toMillis(hour));
		long key = startTime.getTime() + index;
		m_reports.remove(key);
	}

	@Override
	public void doInitLoad(int hour, int index) throws IOException {
		Date startTime = new Date(TimeUnit.HOURS.toMillis(hour));
		long key = startTime.getTime() + index;
		ConcurrentHashMap<String, T> map = new ConcurrentHashMap<String, T>();
		List<T> reports = m_storage.loadAll(getDelegate(), ReportPeriod.HOUR, startTime, null);

		for (T report : reports) {
			map.put(report.getDomain(), report);
		}

		m_reports.putIfAbsent(key, map);
	}

	@Override
	public void enableRoleHint(String roleHint) {
		m_reportName = roleHint;
	}

	protected ReportDelegate<T> getDelegate() {
		return m_delegateManager.getDelegate(m_reportName);
	}

	@Override
	public T getLocalReport(String domain, int hour, int index, boolean createIfNotExist) {
		Date startTime = new Date(TimeUnit.HOURS.toMillis(hour));
		long key = startTime.getTime() + index;
		ConcurrentMap<String, T> map = m_reports.get(key);

		if (map == null) {
			synchronized (m_reports) {
				map = m_reports.get(key);

				if (map == null) {
					map = new ConcurrentHashMap<String, T>();

					m_reports.put(key, map);
				}
			}
		}

		T report = map.get(domain);

		if (report == null && createIfNotExist) {
			report = getDelegate().createLocal(ReportPeriod.HOUR, domain, startTime);

			T r = map.putIfAbsent(domain, report);

			if (r != null) {
				report = r;
			}
		}

		return report;
	}

	@Override
    public Map<String, T> getLocalReports(ReportPeriod period, int hour, int index) throws IOException {
		Date startTime = new Date(TimeUnit.HOURS.toMillis(hour));
        long key = startTime.getTime() + index;
        return m_reports.get(key);
    }

	@Override
	public List<Map<String, T>> getLocalReports(ReportPeriod period, int hour) throws IOException{
		List<Map<String, T>> mapList = new ArrayList<Map<String, T>>();
		int size = getThreadsCount();
		for (int i = 0; i < size; i++){
			Map<String, T> reportMap = getLocalReports(period, hour, i);
			if (reportMap != null) {
				mapList.add(reportMap);
			}
		}
		return mapList;
	}

	@Override
	public List<T> getLocalReports(ReportPeriod period, Date startTime, String domain) throws IOException {
		if (period == ReportPeriod.HOUR && period.isCurrent(startTime)) {
			int hour = (int) TimeUnit.MILLISECONDS.toHours(startTime.getTime());
			int count = getThreadsCount();
			List<T> reports = new ArrayList<T>(count);

			for (int i = 0; i < count; i++) {
				T report = getLocalReport(domain, hour, i, false);

				if (report != null) {
					reports.add(report);
				}
			}

			return reports;
		} else {
			return m_fileStorage.loadAll(getDelegate(), period, startTime, domain);
		}
	}

	@Override
	public T getReport(ReportPeriod period, Date startTime, String domain, String filterId, String... keyValuePairs)
	      throws IOException {
		ReportDelegate<T> delegate = getDelegate();
		ReportFilter<? extends Report> filter = m_filterManager.getFilter(delegate.getName(), filterId);
		RemoteContext ctx = new DefaultRemoteContext(delegate.getName(), domain, startTime, period, filter);

		int len = keyValuePairs.length;

		if (len % 2 == 0) {
			for (int i = 0; i < len; i += 2) {
				String property = keyValuePairs[i];
				String value = keyValuePairs[i + 1];

				ctx.setProperty(property, value);
			}
		} else {
			throw new IllegalArgumentException("Parameter(keyValuePairs) is not paired!");
		}

		try {
			T report = m_provider.getReport(ctx, delegate);

			return report;
		} finally {
			ctx.destroy();
		}
	}

	public abstract int getThreadsCount();
}
