package hxc.connectors.file.process;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import hxc.connectors.file.FileDTO;
import hxc.connectors.file.FileType;
import hxc.connectors.file.IFileProcessorHandler;
import hxc.utils.protocol.sdp.DedicatedAccountsFileV3;
import hxc.utils.protocol.sdp.DedicatedAccountsFileV3_3;
import hxc.utils.protocol.sdp.NetworkLayoutEventV1;
import hxc.utils.protocol.sdp.OfferFileV3;
import hxc.utils.protocol.sdp.OfferFileV3_3;
import hxc.utils.protocol.sdp.SubscriberFileV3;
import hxc.utils.protocol.sdp.SubscriberFileV3_3;
import hxc.utils.protocol.sdp.ThresholdNotificationFileV2;
import hxc.utils.protocol.sdp.ThresholdNotificationFileV3;
import hxc.utils.protocol.sdp.UsageAccumulatorsFileV3;
import hxc.utils.protocol.sdp.UsageAccumulatorsFileV3_3;
import hxc.utils.protocol.sdp.UsageCounterFileV3;
import hxc.utils.protocol.sdp.UsageThresholdFileV3;

public abstract class FileProcessor
{

	// List of available file types, needs to be updated when adding more files
	// @formatter:off
	@SuppressWarnings("unchecked")
	private Class<? extends FileDTO>[] dtos = new Class[] {
		DedicatedAccountsFileV3.class,
		DedicatedAccountsFileV3_3.class,
		OfferFileV3.class, OfferFileV3_3.class,
		SubscriberFileV3.class,
		SubscriberFileV3_3.class,
		ThresholdNotificationFileV2.class,
		UsageAccumulatorsFileV3.class,
		UsageAccumulatorsFileV3_3.class,
		UsageCounterFileV3.class,
		UsageThresholdFileV3.class,
		NetworkLayoutEventV1.class,
		ThresholdNotificationFileV3.class,
	};
	// @formatter:on

	protected RandomAccessFile fileStream;
	protected FileChannel fileChannel;
	protected MappedByteBuffer buffer;
	protected IFileProcessorHandler handler;

	protected FileProcessor()
	{

	}

	// Loads the buffer from the file
	protected synchronized void configure(File file, long position) throws IOException
	{
		fileChannel = channelFromFile(file);
		buffer = mapChannelToBuffer(fileChannel, position);
	}

	// Processes the file from the beginning
	public void process(File file, String dto) throws IOException
	{
		process(file, dto, 0);
	}

	public abstract void process(File file, String dto, long position) throws IOException;

	// Creates a FileChannel from the file stream
	public FileChannel channelFromFile(File file) throws IOException
	{
		if (fileStream != null)
		{
			fileStream.close();
			fileStream = null;
		}

		// Create the file stream from a Random Access File
		fileStream = new RandomAccessFile(file, "r");
		return fileStream.getChannel();
	}

	// Maps the buffer from the start of the file channel
	public MappedByteBuffer mapChannelToBuffer(FileChannel channel) throws IOException
	{
		return mapChannelToBuffer(channel, 0);
	}

	// Maps the buffer from the position of the file channel
	public MappedByteBuffer mapChannelToBuffer(FileChannel channel, long position) throws IOException
	{
		return channel.map(MapMode.READ_ONLY, position, channel.size());
	}

	public Class<? extends FileDTO> convertToDTO(String dto)
	{

		for (Class<? extends FileDTO> c : dtos)
		{
			if (dto.equals(c.getSimpleName()))
			{
				return c;
			}
		}

		return null;
	}

	// Converts the FileType Enum into a Class
	@SuppressWarnings("unchecked")
	public Class<? extends FileDTO> convertToDTO(FileType fileType)
	{
		try
		{
			return (Class<? extends FileDTO>) ClassLoader.getSystemClassLoader().loadClass("hxc.utils.protocol.sdp." + fileType);
		}
		catch (ClassNotFoundException e)
		{
		}
		return null;
	}

	public void setHandler(IFileProcessorHandler handler)
	{
		this.handler = handler;
	}
}
