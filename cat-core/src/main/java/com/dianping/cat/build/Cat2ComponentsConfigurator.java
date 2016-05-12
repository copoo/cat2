package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.message.storage.hdfs.HdfsBucket;
import org.unidal.cat.message.storage.hdfs.HdfsBucketManager;
import org.unidal.cat.message.storage.hdfs.HdfsFileBuilder;
import org.unidal.cat.message.storage.hdfs.HdfsIndex;
import org.unidal.cat.message.storage.hdfs.HdfsIndexManager;
import org.unidal.cat.message.storage.hdfs.HdfsMessageConsumerFinder;
import org.unidal.cat.message.storage.hdfs.HdfsSystemManager;
import org.unidal.cat.message.storage.hdfs.HdfsTokenMapping;
import org.unidal.cat.message.storage.hdfs.HdfsTokenMappingManager;
import org.unidal.cat.message.storage.hdfs.HdfsUploader;
import org.unidal.cat.message.storage.hdfs.LogviewProcessor;
import org.unidal.cat.message.storage.internals.DefaultBlockDumper;
import org.unidal.cat.message.storage.internals.DefaultBlockDumperManager;
import org.unidal.cat.message.storage.internals.DefaultBlockWriter;
import org.unidal.cat.message.storage.internals.DefaultByteBufPool;
import org.unidal.cat.message.storage.internals.DefaultMessageDumper;
import org.unidal.cat.message.storage.internals.DefaultMessageDumperManager;
import org.unidal.cat.message.storage.internals.DefaultMessageFinderManager;
import org.unidal.cat.message.storage.internals.DefaultMessageProcessor;
import org.unidal.cat.message.storage.internals.DefaultStorageConfiguration;
import org.unidal.cat.message.storage.local.LocalBucket;
import org.unidal.cat.message.storage.local.LocalBucketManager;
import org.unidal.cat.message.storage.local.LocalFileBuilder;
import org.unidal.cat.message.storage.local.LocalIndex;
import org.unidal.cat.message.storage.local.LocalIndexManager;
import org.unidal.cat.message.storage.local.LocalTokenMapping;
import org.unidal.cat.message.storage.local.LocalTokenMappingManager;
import org.unidal.cat.metric.DefaultBenchmarkManager;
import org.unidal.cat.service.internals.DefaultCompressionService;
import org.unidal.cat.service.internals.GzipCompressionService;
import org.unidal.cat.service.internals.ZlibCompressionService;
import org.unidal.cat.spi.DefaultReportConfiguration;
import org.unidal.cat.spi.analysis.DefaultMessageDispatcher;
import org.unidal.cat.spi.analysis.DefaultPipelineManager;
import org.unidal.cat.spi.analysis.event.DefaultTimeWindowManager;
import org.unidal.cat.spi.remote.DefaultRemoteSkeleton;
import org.unidal.cat.spi.remote.DefaultRemoteStub;
import org.unidal.cat.spi.report.internals.DefaultReportDelegateManager;
import org.unidal.cat.spi.report.internals.DefaultReportFilterManager;
import org.unidal.cat.spi.report.internals.DefaultReportManagerManager;
import org.unidal.cat.spi.report.provider.DefaultReportProvider;
import org.unidal.cat.spi.report.provider.HistoricalReportProvider;
import org.unidal.cat.spi.report.provider.RecentReportProvider;
import org.unidal.cat.spi.report.storage.DefaultReportStorage;
import org.unidal.cat.spi.report.storage.FileReportStorage;
import org.unidal.cat.spi.report.storage.MysqlReportStorage;
import org.unidal.cat.spi.task.internals.DefaultTaskManager;
import org.unidal.cat.spi.task.internals.TaskDispatcher;
import org.unidal.cat.spi.task.internals.TaskQueue;
import org.unidal.cat.spi.task.internals.TaskRegistry;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

class Cat2ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(DefaultReportConfiguration.class));
		all.add(A(DefaultReportManagerManager.class));
		all.add(A(DefaultReportFilterManager.class));
		all.add(A(DefaultReportDelegateManager.class));

		all.add(A(DefaultRemoteStub.class));
		all.add(A(DefaultRemoteSkeleton.class));

		all.add(A(DefaultReportProvider.class));
		all.add(A(RecentReportProvider.class));
		all.add(A(HistoricalReportProvider.class));

		all.add(A(DefaultReportStorage.class));
		all.add(A(MysqlReportStorage.class));
		all.add(A(FileReportStorage.class));

		all.add(A(DefaultCompressionService.class));
		all.add(A(GzipCompressionService.class));
		all.add(A(ZlibCompressionService.class));

		all.add(A(DefaultTaskManager.class));
		all.add(A(TaskRegistry.class));
		all.add(A(TaskDispatcher.class));
		all.add(A(TaskQueue.class));

		all.add(A(DefaultBenchmarkManager.class));

		all.add(A(DefaultPipelineManager.class));
		all.add(A(DefaultMessageDispatcher.class));
		all.add(A(DefaultTimeWindowManager.class));
		
		all.addAll(defineLocalComponents());
		return all;
	}
	
	public List<Component> defineLocalComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(DefaultMessageDumperManager.class));
		all.add(A(DefaultMessageFinderManager.class));
		all.add(A(DefaultMessageDumper.class));
		all.add(A(DefaultMessageProcessor.class));
		all.add(A(DefaultBlockDumperManager.class));
		all.add(A(DefaultBlockDumper.class));
		all.add(A(DefaultBlockWriter.class));
		
		all.add(A(HdfsSystemManager.class));
		
		all.add(A(HdfsMessageConsumerFinder.class));

		all.add(A(LocalBucket.class));
		all.add(A(LocalBucketManager.class));
		all.add(A(HdfsBucket.class));
		all.add(A(HdfsBucketManager.class));
		
		all.add(A(LocalIndex.class));
		all.add(A(LocalIndexManager.class));
		all.add(A(HdfsIndex.class));
		all.add(A(HdfsIndexManager.class));

		all.add(A(LocalFileBuilder.class));
		all.add(A(HdfsFileBuilder.class));
		all.add(A(LocalTokenMapping.class));
		all.add(A(HdfsTokenMapping.class));
		all.add(A(LocalTokenMappingManager.class));
		all.add(A(HdfsTokenMappingManager.class));

		all.add(A(DefaultStorageConfiguration.class));
		all.add(A(DefaultByteBufPool.class));

		all.add(A(HdfsUploader.class));
		all.add(A(LogviewProcessor.class));

		return all;
	}
}
