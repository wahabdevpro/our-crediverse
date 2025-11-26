package cs.dto;

import org.springframework.beans.BeanUtils;

import cs.constants.ApplicationEnum.BatchStatusEnum;
import hxc.ecds.protocol.rest.Batch;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiBatch extends Batch
{

	protected String webUserName;
	protected String coAuthWebUserName;
	protected BatchStatusEnum batchStatus;

	public GuiBatch()
	{
		super();
	}

	public GuiBatch(Batch orig)
	{
		BeanUtils.copyProperties(orig, this);
		this.batchStatus = BatchStatusEnum.fromTsValue(orig.getState());
	}

	public Batch getBatch()
	{
		Batch batch = new Batch();
		BeanUtils.copyProperties(this, batch);
		return batch;
	}
}
