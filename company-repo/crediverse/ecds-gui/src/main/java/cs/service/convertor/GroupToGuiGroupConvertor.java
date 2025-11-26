package cs.service.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import cs.dto.GuiGroup;
import hxc.ecds.protocol.rest.Group;

@Component
public class GroupToGuiGroupConvertor implements Converter<Group, GuiGroup>
{

	@Override
	public GuiGroup convert(Group source)
	{
		GuiGroup group = new GuiGroup();
		BeanUtils.copyProperties(source, group);
		return group;
	}

}
