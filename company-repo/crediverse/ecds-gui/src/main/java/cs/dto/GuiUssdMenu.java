package cs.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiUssdMenu
{
	protected int id;
	protected int offset;
	protected String name;
	protected List<GuiUssdMenuButton> buttons = new ArrayList<GuiUssdMenuButton>();
}
