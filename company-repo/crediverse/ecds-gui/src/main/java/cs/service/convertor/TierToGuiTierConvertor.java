package cs.service.convertor;

import org.springframework.core.convert.converter.Converter;

import cs.dto.GuiTier;
import hxc.ecds.protocol.rest.Tier;

public class TierToGuiTierConvertor implements Converter<Tier, GuiTier>
{

	@Override
	public GuiTier convert(Tier source)
	{
		GuiTier currentTier = new GuiTier(source);
		return currentTier;
	}

}
