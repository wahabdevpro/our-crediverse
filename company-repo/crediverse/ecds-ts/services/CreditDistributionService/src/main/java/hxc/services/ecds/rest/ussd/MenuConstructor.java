package hxc.services.ecds.rest.ussd;

import static hxc.ecds.protocol.rest.config.IConfirmationMenuConfig.RECIPIENT_MSISDN_CONFIRMED;
import static hxc.ecds.protocol.rest.config.UssdConfig.ROOT_MENU_ID;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import hxc.ecds.protocol.rest.UssdCommand;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.UssdConfig;
import hxc.ecds.protocol.rest.config.UssdMenu;
import hxc.ecds.protocol.rest.config.UssdMenuButton;
import hxc.services.ecds.Session;

public abstract class MenuConstructor
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Default Menu Construction
	//
	// /////////////////////////////////
	public void createDefaultMenu(EntityManager em, Session session, UssdConfig config, UssdCommand[] commands)
	{
		List<UssdMenu> menus = new ArrayList<UssdMenu>();

		int id = ROOT_MENU_ID;

		// Create command selection menu as the root Menu (no splash screen)
		UssdMenu rootMenu = createMenu(menus, id++, "Menu1");
		addText(rootMenu, Phrase.en("Available Commands"));
		int index = 1;
		for (UssdCommand command : commands)
		{
			String menuName = String.format("%s.%d", rootMenu.getName(), index++);
			id = addCommand(em, menus, rootMenu, id, command, menuName);
		}

		// Add Previous/Next
		List<UssdMenuButton> buttons = rootMenu.getButtons();
		buttons.add(UssdMenuButton.createPrevious(Phrase.en("[n]) Previous")));
		buttons.add(UssdMenuButton.createNext(Phrase.en("[n]) Next")));

		// Add Exit
		String menuName = String.format("%s.%d", rootMenu.getName(), index++);
		addExit(menus, rootMenu, menuName, id++, Phrase.en("[n]) Exit"), Phrase.en("Thank You."));

		config.setMenus(new ArrayList<>(menus));
	}

	private static void addText(UssdMenu menu, Phrase text)
	{
		UssdMenuButton button = UssdMenuButton.createText(text);
		menu.getButtons().add(button);
	}

	private static void addNav(UssdMenu menu, Phrase text, int menuID)
	{
		UssdMenuButton button = UssdMenuButton.createNavigate(text, menuID);
		menu.getButtons().add(button);
	}

	private void addExit(List<UssdMenu> menus, UssdMenu commandMenu, String menuName, int id, Phrase menuItem, Phrase menuText)
	{
		UssdMenu exitMenu = createMenu(menus, id, menuName);
		addText(exitMenu , menuText);		
		List<UssdMenuButton> menuButtons = commandMenu.getButtons();
		menuButtons.add(UssdMenuButton.createExit(menuItem, id));	
	}

	private static void addCommand(UssdMenu menu, int commandID, Phrase text, int nextMenuID, int options)
	{
		UssdMenuButton button = UssdMenuButton.createCommand(commandID, text, nextMenuID);
		button.setOptions(options);
		menu.getButtons().add(button);
	}

	private static void addCapture(UssdMenu menu, int commandID, Phrase text, int nextMenuID, int options, String captureFieldName) {
		UssdMenuButton button = UssdMenuButton.createCapture(commandID, text, captureFieldName, nextMenuID);
		button.setOptions(options);
		menu.getButtons().add(button);
	}

	private int addCommand(EntityManager em, List<UssdMenu> menus, UssdMenu commandMenu, int id, UssdCommand command, String menuName)
	{
		// Add Command
		UssdMenuButton button = UssdMenuButton.createCommand(command, id);
		commandMenu.getButtons().add(button);

		// Add Capture Menus
		for (Phrase field : command.getCommandFields())
		{
			UssdMenu captureMenu = createMenu(menus, id++, menuName);
			menuName = menuName + ".1";

			// Check if Auto Options are available
			String fieldName = field.safe(Phrase.ENG, "??");
			IMenuProcessor processor = getUssdProcessor(command.getId());
			String caption = processor.menuDescribeField(fieldName);
			caption = caption.replace("{", "").replace("}", "");
			MenuOption[] options = processor.menuOptions(em, null, fieldName);

			List<UssdMenuButton> buttons = captureMenu.getButtons();
			if (options != null)
			{
				UssdMenuButton heading = UssdMenuButton.createText(Phrase.en(String.format("Select %s:", caption)));
				buttons.add(heading);

				UssdMenuButton optionsButton = UssdMenuButton.createOptions(command.getId(), Phrase.en("[n]) "), fieldName, id); //
				buttons.add(optionsButton);

				// Add Previous/Next
				buttons.add(UssdMenuButton.createPrevious(Phrase.en("[n]) Previous")));
				buttons.add(UssdMenuButton.createNext(Phrase.en("[n]) Next")));
			}
			else
			{
				Phrase text = Phrase.en(String.format("Enter %s", caption));
				UssdMenuButton capture = UssdMenuButton.createCapture(command.getId(), text, fieldName, id); //
				buttons.add(capture);
			}
			addNav(captureMenu, Phrase.en("or [*] to go back"), commandMenu.getId());

		}

		// Add Confirmation Menu
		{
			UssdMenu confirmMenu = createMenu(menus, id++, menuName);
			menuName = menuName + ".1";
			addText(confirmMenu, Phrase.en("Confirm?"));
			addNav(confirmMenu, Phrase.en("[n] Yes"), id);
			addNav(confirmMenu, Phrase.en("[n] No"), commandMenu.getId());
		}

		// Add Execution Result Menu
		{
			UssdMenu executionMenu = createMenu(menus, id++, menuName);
			UssdMenuButton execute = UssdMenuButton.createResult(command.getId());
			executionMenu.getButtons().add(execute);
			addNav(executionMenu, Phrase.en("Type [n] to continue..."), commandMenu.getId());
		}

		return id;
	}

	private static UssdMenu createMenu(List<UssdMenu> menus, int id, String name)
	{
		UssdMenu menu = new UssdMenu() //
				.setName(name) //
				.setId(id);
		menus.add(menu);
		return menu;
	}

	public List<UssdMenu> createConfirmationMenu(int commandID, String confirmPhrase, String cancelPhrase)
	{
		List<UssdMenu> menus = new ArrayList<UssdMenu>();

		// Create Root Menu
		UssdMenu rootMenu = createMenu(menus, ROOT_MENU_ID, "Menu1");
		addText(rootMenu, Phrase.en(confirmPhrase));
		
		addCommand(rootMenu, commandID, Phrase.en("[n]) Confirm"), ROOT_MENU_ID + 1, 0);
		addNav(rootMenu, Phrase.en("[n]) Cancel"), ROOT_MENU_ID + 2);
		addCommand(rootMenu, commandID, Phrase.en("[n]) Confirm and don't ask me again"), ROOT_MENU_ID + 3, IMenuProcessor.OPTION_OPT_OUT);
		
		UssdMenu normalMenu = createMenu(menus, ROOT_MENU_ID + 1, "Menu1.1");
		normalMenu.getButtons().add(UssdMenuButton.createResult(commandID));

		UssdMenu cancelMenu = createMenu(menus, ROOT_MENU_ID + 2, "Menu1.2");
		addText(cancelMenu, Phrase.en(cancelPhrase));

		UssdMenu optOutMenu = createMenu(menus, ROOT_MENU_ID + 3, "Menu1.3");
		optOutMenu.getButtons().add(UssdMenuButton.createResult(commandID));
		addText(optOutMenu, Phrase.en("Confirmation Prompts Turned Off"));

		return menus;
	}

	public static List<UssdMenu> createBNumberConfirmationMenu(int commandID, List<UssdMenu> confirmationMenu, Phrase bNumberConfirmMessage) {
		List<UssdMenu> menuList = new ArrayList<>();
		
		UssdMenu rootMenu = createMenu(menuList, ROOT_MENU_ID, "B.number.confirm.Menu1");
		int nextMenuId = ROOT_MENU_ID + 1;
		
		addCapture(rootMenu,
				commandID,
				bNumberConfirmMessage,
				nextMenuId,
				0,
				RECIPIENT_MSISDN_CONFIRMED);
		
		if (confirmationMenu != null) {
			if (confirmationMenu.get(0).getId() == ROOT_MENU_ID) {
				shiftIds(confirmationMenu, menuList.size());
			}
			menuList.addAll(confirmationMenu);
		} else {
			UssdMenu optOutMenu = createMenu(menuList, nextMenuId, "B.number.confirm.Menu1.1");
			optOutMenu.getButtons().add(UssdMenuButton.createResult(commandID));
		}
		
		return menuList;
	}
	
	public static void shiftIds(List<UssdMenu> menuList, int shift) {
		for (UssdMenu menu : menuList) {
			menu.setId(menu.getId() + shift);
			for (UssdMenuButton button : menu.getButtons()) {
				if (button.getNextMenuID() != null) {
					button.setNextMenuID(button.getNextMenuID() + shift);
				}
			}
		}
	}
	
	public List<UssdMenu> createDeDuplicationMenu(int commandID, String confirmPhrase, String cancelPhrase)
	{
		List<UssdMenu> menus = new ArrayList<UssdMenu>();

		// Create Root Menu
		UssdMenu rootMenu = createMenu(menus, ROOT_MENU_ID, "Menu1a");
		addText(rootMenu, Phrase.en(confirmPhrase));
		
		addCommand(rootMenu, commandID, Phrase.en("[n]) Confirm"), ROOT_MENU_ID + 1, 0);
		addNav(rootMenu, Phrase.en("[n]) Cancel"), ROOT_MENU_ID + 2);
		
		UssdMenu normalMenu = createMenu(menus, ROOT_MENU_ID + 1, "Menu1a.1");
		normalMenu.getButtons().add(UssdMenuButton.createResult(commandID));

		UssdMenu cancelMenu = createMenu(menus, ROOT_MENU_ID + 2, "Menu1a.2");
		addText(cancelMenu, Phrase.en(cancelPhrase));

		return menus;
	}

	protected abstract IMenuProcessor getUssdProcessor(int id);
}
