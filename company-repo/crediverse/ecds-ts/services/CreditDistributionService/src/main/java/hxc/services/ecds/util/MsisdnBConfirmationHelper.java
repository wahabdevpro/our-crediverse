package hxc.services.ecds.util;

import static hxc.ecds.protocol.rest.config.IConfirmationMenuConfig.RECIPIENT_MSISDN_CONFIRMED;
import static hxc.ecds.protocol.rest.config.SalesConfig.RECIPIENT_MSISDN;
import static hxc.ecds.protocol.rest.config.UssdConfig.ROOT_MENU_ID;
import static hxc.ecds.protocol.rest.config.UssdMenuButton.TYPE_CAPTURE;
import static hxc.services.ecds.rest.ussd.MenuConstructor.shiftIds;
import static hxc.services.ecds.rest.ussd.MenuProcessor.B_NUMBER_CONFIRM_MENU_NAME;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import hxc.ecds.protocol.rest.config.BundleSalesConfig;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.SalesConfig;
import hxc.ecds.protocol.rest.config.TransfersConfig;
import hxc.ecds.protocol.rest.config.UssdMenu;
import hxc.ecds.protocol.rest.config.UssdMenuButton;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.Sell;
import hxc.services.ecds.rest.SellBundle;
import hxc.services.ecds.rest.Transfer;
import hxc.services.ecds.rest.ussd.IMenuProcessor;
import hxc.services.ecds.rest.ussd.MenuState;

public class MsisdnBConfirmationHelper {

    public static Phrase getbNumberConfirmMessage(IMenuProcessor menuProcessor, ICreditDistribution context, Session session, EntityManager em) {
        Phrase bNumberConfirmMessage = Phrase.en("");
        // Get configuration for the current type of transaction
        if (menuProcessor instanceof Sell) {
            SalesConfig config = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, SalesConfig.class);
            bNumberConfirmMessage = config.getNumberConfirmMessage();
        } else if (menuProcessor instanceof SellBundle) {
            BundleSalesConfig config = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, BundleSalesConfig.class);
            bNumberConfirmMessage = config.getNumberConfirmMessage();
        } else if (menuProcessor instanceof Transfer) {
            TransfersConfig config = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, TransfersConfig.class);
            bNumberConfirmMessage = config.getNumberConfirmMessage();
        }

        return bNumberConfirmMessage;
    }

    public static boolean haveToConfirmBNumber(IMenuProcessor menuProcessor, ICreditDistribution context, UssdMenuButton button,
                                         Session session, EntityManager em) {
        // Only when the current "button" is for entering recipient MSISDN
        if (!RECIPIENT_MSISDN.equals(button.getCaptureField())) {
            return false;
        }

        boolean confirmBNumber = false;
        // Get configuration for the current type of transaction
        if (menuProcessor instanceof Sell) {
            SalesConfig config = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, SalesConfig.class);
            confirmBNumber = config.getEnableBNumberConfirmation();
        } else if (menuProcessor instanceof SellBundle) {
            BundleSalesConfig config = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, BundleSalesConfig.class);
            confirmBNumber = config.getEnableBNumberConfirmation();
        } else if (menuProcessor instanceof Transfer) {
            TransfersConfig config = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, TransfersConfig.class);
            confirmBNumber = config.getEnableBNumberConfirmation();
        }

        return confirmBNumber;
    }

    public static Integer findConfirmMenuId(IMenuProcessor menuProcessor, Map<Integer, UssdMenu> menu) {
        for (UssdMenu ussdMenu : menu.values()) {
            if ((menuProcessor.menuName() + B_NUMBER_CONFIRM_MENU_NAME).equals(ussdMenu.getName())) {
                return ussdMenu.getId();
            }
        }
        return null;
    }

    public static Integer findConfirmMenuId(IMenuProcessor menuProcessor, List<UssdMenu> menu) {
        for (UssdMenu ussdMenu : menu) {
            if ((menuProcessor.menuName() + B_NUMBER_CONFIRM_MENU_NAME).equals(ussdMenu.getName())) {
                return ussdMenu.getId();
            }
        }
        return null;
    }

    public static Integer generateBNumberConfirmationMenu(IMenuProcessor menuProcessor, MenuState state,
        UssdMenuButton button, Phrase bNumberConfirmMessage) {
        int confirmMenuId = getFreeMenuId(state.menus);

        UssdMenuButton confirmButton = new UssdMenuButton();
        confirmButton.setType(TYPE_CAPTURE);
        confirmButton.setText(bNumberConfirmMessage);
        confirmButton.setCaptureField(RECIPIENT_MSISDN_CONFIRMED);
        confirmButton.setCommandID(confirmMenuId);
        confirmButton.setNextMenuID(button.getNextMenuID());

        List<UssdMenuButton> newButtons = new ArrayList<>();
        newButtons.add(confirmButton);
        newButtons.add(state.menus.get(state.menuID).getButtons().get(1));

        UssdMenu confirmMenu = new UssdMenu();
        confirmMenu.setId(confirmMenuId);
        confirmMenu.setButtons(newButtons);
        confirmMenu.setName(menuProcessor.menuName() + B_NUMBER_CONFIRM_MENU_NAME);

        state.menus.put(confirmMenuId, confirmMenu);
        return confirmMenuId;
    }

    public static void removeBNumberConfirmationFromMenu(IMenuProcessor menuProcessor, List<UssdMenu> menu) {
        Integer bNumberConfirmMenu = findConfirmMenuId(menuProcessor, menu);
        if (bNumberConfirmMenu != null) {
            Iterator<UssdMenu> iterator = menu.iterator();
            while (iterator.hasNext()) {
                UssdMenu ussdMenu = iterator.next();
                if (ussdMenu.getId() == bNumberConfirmMenu) {
                    iterator.remove();
                } else if (ussdMenu.getId() > bNumberConfirmMenu) {
                    ussdMenu.setId(ussdMenu.getId() - 1);
                }
            }
        } else {
            if (menu.get(0).getId() != ROOT_MENU_ID) {
                shiftIds(menu, -(menu.get(0).getId() - 1));
            }
        }
    }

    private static int getFreeMenuId(Map<Integer, UssdMenu> menus) {
        int id = 1000;
        while (menus.containsKey(id)) {
            id++;
        }
        return id;
    }
}
