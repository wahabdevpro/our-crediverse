package hxc.services.sharedaccounts;

import com.concurrent.hxc.Number;
import com.concurrent.hxc.ServiceQuota;

import hxc.processmodel.IProcess;
import hxc.utils.processmodel.AddMemberCall;
import hxc.utils.processmodel.AddQuotaCall;
import hxc.utils.processmodel.End;
import hxc.utils.processmodel.ErrorDisplay;
import hxc.utils.processmodel.GetBalancesCall;
import hxc.utils.processmodel.GetMembersCall;
import hxc.utils.processmodel.GetQuotaCall;
import hxc.utils.processmodel.GetQuotasCall;
import hxc.utils.processmodel.GetServiceCall;
import hxc.utils.processmodel.MembershipTest;
import hxc.utils.processmodel.Menu;
import hxc.utils.processmodel.MenuItem;
import hxc.utils.processmodel.MenuItems;
import hxc.utils.processmodel.MigrateCall;
import hxc.utils.processmodel.RemoveMemberCall;
import hxc.utils.processmodel.RemoveQuotaCall;
import hxc.utils.processmodel.SubscribeCall;
import hxc.utils.processmodel.SubscriptionTest;
import hxc.utils.processmodel.Test;
import hxc.utils.processmodel.UnsubscribeCall;
import hxc.utils.processmodel.UpdateQuotaCall;
import hxc.utils.processmodel.UssdStart;
import hxc.utils.processmodel.Value;

public class SharedAccountsMenu
{
	public static IProcess getMenuProcess(String serviceID)
	{
		// Start with Short Code 124
		UssdStart start = new UssdStart(serviceID, "UssdMenu");

		// Error exit
		ErrorDisplay errorDisplayEnd = new ErrorDisplay(null, start.getServiceID());

		// Normal Exit
		End end = new End(null, "Thank you for using the Shared Accounts Service");
		end.getMessage().set("Thank you for Using Group Shared Accounts", "Merci d'avoir utilisé Group Shared Accounts");

		// Make Test
		SubscriptionTest subscribeTest = new SubscriptionTest(start, errorDisplayEnd, start.getSubscriberNumber(), start.getServiceID(), null);

		// Subscribed
		Menu subscribedMenu = new Menu(subscribeTest, "Shared Accounts Service");
		subscribedMenu.getCaption().set("Welcome to Group Shared Accounts", "Bienvenue à Group Shared Accounts");
		subscribedMenu.getMoreText().set("#0 More", "#0 D'autre");

		{
			// Error Display with Continue to Subscribed Menu
			ErrorDisplay errorDisplayContinue = new ErrorDisplay(null, start.getServiceID(), "Type #* to continue", subscribedMenu);
			errorDisplayContinue.getSuffixText().set("Type #* to continue", "Tapez #* pour continuer");

			// Add Menu Items
			MenuItem addMenuItem = subscribedMenu.addItem("# Add Beneficiary");
			addMenuItem.getText().set("# Add Beneficiary", "# Ajouter un bénéficiaire");
			MenuItem viewMenuItem = subscribedMenu.addItem("# View Beneficiaries");
			viewMenuItem.getText().set("# View Beneficiaries", "# Voir vos bénéficiaires");
			MenuItem removeMenuItem = subscribedMenu.addItem("# Remove Beneficiary");
			removeMenuItem.getText().set("# Remove Beneficiary", "# Enlever un bénéficiaire");
			MenuItem changeMenuItem = subscribedMenu.addItem("# Change Quotas");
			changeMenuItem.getText().set("# Change Sharing settings", "# Modifier les paramètres de partage");
			MenuItem balancesMenuItem = subscribedMenu.addItem("# View Balances");
			balancesMenuItem.getText().set("# View Balances", "# Voir les soldes");
			MenuItem languageMenuItem = subscribedMenu.addItem("# Change Language");
			languageMenuItem.getText().set("# Change Language", "# Changer de langue");
			MenuItem unsubscribeMenuItem = subscribedMenu.addItem("# Unsubscribe");
			unsubscribeMenuItem.getText().set("# Unsubscribe", "# Se désabonner");
			subscribedMenu.setBackItem(end, "#* Back", "#* Exit", "#* Annuler"); // !!

			// Add Beneficiary
			{
				// Ask for Beneficiary's Number
				Menu addBeneficiaryMenu = new Menu(addMenuItem, "Add Beneficiary");
				addBeneficiaryMenu.getCaption().set("Enter Number:", "Entrer le Numéro:");
				MenuItem addBeneficiaryItem = addBeneficiaryMenu.addItem(subscribedMenu, "Enter Beneficiary's Number or #* to go back");
				addBeneficiaryItem.getText().set("or #* to exit\n> ", "ou #* pour sortir\n> ");

				// Confirm Add Member Charge
				AddMemberCall rateCall = new AddMemberCall(addBeneficiaryMenu, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(),
						addBeneficiaryMenu.getInputNumber(), new Value<Boolean>(true));
				Test hasChargeTest = new Test(rateCall, errorDisplayEnd, rateCall.getHasCharge());
				Menu confirmChargeMenu = new Menu(hasChargeTest, "Confirm Charge:");
				confirmChargeMenu.getCaption().set("You are going to be charged {Charge} USD for adding a beneficiary", "Vous allez etre débité de {Charge} USD pour l'ajout d'un bénéficiaire");
				MenuItem confirmYesX = confirmChargeMenu.addItem("# Yes");
				confirmYesX.getText().set("# Accept", "# Accepter");
				MenuItem confirmNo = confirmChargeMenu.addItem(subscribedMenu, "#* Back");
				confirmNo.getText().set("#* Back", "#* Retour");

				// Add
				{
					// Get Quota Types
					GetQuotasCall getQuotasTypes0 = new GetQuotasCall(confirmYesX, errorDisplayContinue, //
							start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), addBeneficiaryMenu.getInputNumber(), //
							null, null, null, null, new Value<Boolean>(false));
					hasChargeTest.setNoAction(getQuotasTypes0);

					// Service Type
					{
						Menu selectServiceMenu = new Menu(getQuotasTypes0, "Select Service Type");
						selectServiceMenu.getCaption().set("Select Service Type:", "Choisir le type de service:");
						selectServiceMenu.getMoreText().set("#0 More", "#0 D'autre");
						MenuItems<String> services = selectServiceMenu.addItems("# {}", "None Available", getQuotasTypes0.getServiceNames());
						services.getEmptyText().set("None", "Rien");
						selectServiceMenu.setBackItem(subscribedMenu, "#* Back", "#* Back", "#* Retour");

						// Destination
						{
							GetQuotasCall getQuotasTypes1 = new GetQuotasCall(services, errorDisplayContinue, //
									start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), addBeneficiaryMenu.getInputNumber(), //
									services.getSelectedValue(), null, null, null, new Value<Boolean>(false));

							Menu destinationMenu = new Menu(getQuotasTypes1, "Select Destination");
							destinationMenu.getCaption().set("Select Destination:", "Destination:");
							destinationMenu.getMoreText().set("#0 More", "#0 D'autre");
							MenuItems<String> destinations = destinationMenu.addItems("# {}", "None Available", getQuotasTypes1.getDestinationNames());
							destinations.getEmptyText().set("None", "Rien");
							destinationMenu.setBackItem(subscribedMenu, "#* Back", "#* Back", "#* Retour");

							// Day Of Week
							{
								GetQuotasCall getQuotasTypes2 = new GetQuotasCall(destinations, errorDisplayContinue, //
										start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), addBeneficiaryMenu.getInputNumber(), //
										services.getSelectedValue(), destinations.getSelectedValue(), null, null, new Value<Boolean>(false));

								Menu daysOfWeekMenu = new Menu(getQuotasTypes2, "Select Days of Week");
								daysOfWeekMenu.getCaption().set("Select Days of Week:", "Choisir les jours autorisés :");
								daysOfWeekMenu.getMoreText().set("#0 More", "#0 D'autre");
								MenuItems<String> daysOfWeek = daysOfWeekMenu.addItems("# {}", "None Available", getQuotasTypes2.getDaysOfWeekNames());
								daysOfWeek.getEmptyText().set("None", "Rien");
								daysOfWeekMenu.setBackItem(subscribedMenu, "#* Back", "#* Back", "#* Retour");

								// Time of Day
								{
									GetQuotasCall getQuotasTypes3 = new GetQuotasCall(daysOfWeek, errorDisplayContinue, //
											start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), addBeneficiaryMenu.getInputNumber(), //
											services.getSelectedValue(), destinations.getSelectedValue(), daysOfWeek.getSelectedValue(), null, new Value<Boolean>(false));

									Menu timesOfWeekMenu = new Menu(getQuotasTypes3, "Select Times of Day");
									timesOfWeekMenu.getCaption().set("Select Time of Day:", "Choisir les heures autorisées:");
									timesOfWeekMenu.getMoreText().set("#0 More", "#0 D'autre");
									MenuItems<String> timesOfDay = timesOfWeekMenu.addItems("# {}", "None Available", getQuotasTypes3.getTimesOfDayNames());
									timesOfDay.getEmptyText().set("None", "Rien");
									timesOfWeekMenu.setBackItem(subscribedMenu, "#* Back", "#* Back", "#* Retour");

									// Quantity
									{
										Menu quantityMenu = new Menu(timesOfDay, "Enter {Units} to share:");
										quantityMenu.getCaption().set("Enter number of {Units} to share:", "Entrer le nombre de {Units} à partager:");
										MenuItem orBack = quantityMenu.addItem(subscribedMenu, "#* Back");
										orBack.getText().set("or #* to exit\n>", "ou #* pour sortir\n>");

										// Rate Quota
										{
											// Error Display with Continue to Quantity Menu
											ErrorDisplay errorDisplayQuantity = new ErrorDisplay(null, start.getServiceID(), "Type #* to continue?", quantityMenu);
											errorDisplayQuantity.getSuffixText().set("Type #* to continue", "Tapez #* pour continuer");

											AddQuotaCall rateQuotaCall = new AddQuotaCall(quantityMenu, // After
													errorDisplayQuantity, // On Error
													start.getSubscriberNumber(), // Subscriber
													start.getServiceID(), // Service
													subscribeTest.getVariantID(), // Variant
													addBeneficiaryMenu.getInputNumber(), // Member
													null, // QuotaID
													services.getSelectedValue(), // Service Type
													destinations.getSelectedValue(), // Destinations
													daysOfWeek.getSelectedValue(), // Days of Week
													timesOfDay.getSelectedValue(), // Times of Day
													quantityMenu.getInputAmount(), // Amount
													new Value<Boolean>(true)); // Rate Only

											// Ask Confirmation
											{
												// Display Success
												Menu addConfirmationMenu = new Menu(rateQuotaCall, "Add {SharedQuantity} {Units} {QuotaName} at {Charge} USD?");
												addConfirmationMenu.getCaption().set( //
														"Confirm you want to share {SharedQuantity} {Units} {Destination} with {ConsumerMSISDN} {DaysOfWeek}, {TimeOfDay} at {Charge} USD", //
														"Confirmez que vous voulez partager {SharedQuantity} {Units} {Destination} avec {ConsumerMSISDN}, {DaysOfWeek} {TimeOfDay} pour {Charge} USD");
												MenuItem yesAddQuota = addConfirmationMenu.addItem("# Yes");
												yesAddQuota.getText().set("# Yes", "# Oui");
												MenuItem exitOption = addConfirmationMenu.addItem(subscribedMenu, "#* Back");
												exitOption.getText().set("#* Exit", "#* Retour");

												// Add Quota
												{
													AddQuotaCall addQuotaCall = new AddQuotaCall(yesAddQuota, // After Confirmation
															errorDisplayContinue, // On Error
															start.getSubscriberNumber(), // Subscriber
															start.getServiceID(), // Service
															subscribeTest.getVariantID(), // Variant
															addBeneficiaryMenu.getInputNumber(), // Member
															null, // QuotaID
															services.getSelectedValue(), // Service Type
															destinations.getSelectedValue(), // Destinations
															daysOfWeek.getSelectedValue(), // Days of Week
															timesOfDay.getSelectedValue(), // Times of Day
															quantityMenu.getInputAmount(), // Amount
															new Value<Boolean>(false)); // Not Rate Only

													// Display Success
													{
														// Display Success
														Menu addedSuccessfullyMenu = new Menu(addQuotaCall, "Added {SharedQuantity} {Units} {Service} for {ConsumerMSISDN} at {Charge} USD");
														addedSuccessfullyMenu.getCaption().set( //
																"You have shared {SharedQuantity} {Units} {Destination} with {ConsumerMSISDN} {DaysOfWeek}, {TimeOfDay} for {Charge} USD", //
																"Vous avez patagé {SharedQuantity} {Units} {Destination} avec {ConsumerMSISDN}, {DaysOfWeek} {TimeOfDay} pour {Charge} USD");
														MenuItem addedSuccess = addedSuccessfullyMenu.addItem(subscribedMenu, "Type #* to continue...");
														addedSuccess.getText().set("Type #* to continue", "Tapez #* pour continuer");
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}

			}

			// View Beneficiaries
			{
				// Get list of Beneficiaries
				GetMembersCall getBeneficiaries = new GetMembersCall(viewMenuItem, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID());

				// Display Beneficiaries
				{
					Menu hasBeneficiariesMenu = new Menu(getBeneficiaries, "Your current beneficiaries:");
					hasBeneficiariesMenu.getCaption().set("Beneficiaries List:", "Numéros bénéficiaires:");
					hasBeneficiariesMenu.getMoreText().set("#0 More", "#0 D'autre");
					MenuItems<Number> listItems = hasBeneficiariesMenu.addItems("{}", "You don't have any beneficiaries", getBeneficiaries.getMembers());
					listItems.getEmptyText().set("None", "Rien");
					hasBeneficiariesMenu.setBackItem(subscribedMenu, "Type #* to Continue", "#* Back", "#* Retour");
				}

			}

			// Remove Beneficiary
			{
				// Get list of Beneficiaries
				GetMembersCall getBeneficiaries = new GetMembersCall(removeMenuItem, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID());

				// Display Removal Menu
				{
					// Select Beneficiary
					Menu removeBeneficiariesMenu = new Menu(getBeneficiaries, "Beneficiary to remove:");
					removeBeneficiariesMenu.getCaption().set("Beneficiary to Remove", "Bénéficiaire à enlever");
					removeBeneficiariesMenu.getMoreText().set("#0 More", "#0 D'autre");
					MenuItems<Number> beneficiaryItems = removeBeneficiariesMenu.addItems("#) {}", "No Beneficiaries", getBeneficiaries.getMembers());
					beneficiaryItems.getEmptyText().set("None", "Rien");
					removeBeneficiariesMenu.setBackItem(subscribedMenu, "#* Back", "#* Back", "#* Retour");

					// Confirm Remove Beneficiary Charge
					RemoveMemberCall rateCall = new RemoveMemberCall(beneficiaryItems, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(),
							beneficiaryItems.getSelectedValue(), new Value<Boolean>(true));
					Test hasChargeTest = new Test(rateCall, errorDisplayEnd, rateCall.getHasCharge());
					Menu confirmChargeMenu = new Menu(hasChargeTest, "Confirm Charge:");
					confirmChargeMenu.getCaption().set("You are going to be charged {Charge} USD for removing a beneficiary",
							"Vous allez etre débité de {Charge} USD pour la suppression d'un bénéficiaire");
					MenuItem confirmYes = confirmChargeMenu.addItem("# Accept");
					confirmYes.getText().set("# Accept", "# Accepter");
					MenuItem confirmNo = confirmChargeMenu.addItem(subscribedMenu, "#* Back");
					confirmNo.getText().set("#* Back", "#* Retour");

					// Remove Beneficiary
					RemoveMemberCall removeMemberCall = new RemoveMemberCall(confirmYes, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(),
							beneficiaryItems.getSelectedValue(), new Value<Boolean>(false));
					hasChargeTest.setNoAction(removeMemberCall);

					// Display Success
					Menu addedSuccessfullyMenu = new Menu(removeMemberCall, "Successfully removed {ConsumerMSISDN}");
					addedSuccessfullyMenu.getCaption().set("You are no longer sharing credit with {ConsumerMSISDN}", "Vous ne partagez plus de crédit avec {ConsumerMSISDN}");
					addedSuccessfullyMenu.addItem(subscribedMenu, "#* Back").getText().set("Type ## to continue", "Tapez ## pour continuer");
				}
			}

			// Change Quotas
			{
				// Get list of Beneficiaries
				GetMembersCall getBeneficiaries = new GetMembersCall(changeMenuItem, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID());

				// Select Beneficiaries
				{
					// Select Beneficiary
					Menu hasBeneficiariesMenu = new Menu(getBeneficiaries, "Select Beneficiary to change:");
					hasBeneficiariesMenu.getCaption().set("Select beneficiary", "Numéros à modifier");
					hasBeneficiariesMenu.getMoreText().set("#0 More", "#0 D'autre");
					MenuItems<Number> beneficiaryItems = hasBeneficiariesMenu.addItems("#) {}", "No Beneficiaries", getBeneficiaries.getMembers());
					beneficiaryItems.getEmptyText().set("None", "Rien");
					hasBeneficiariesMenu.setBackItem(subscribedMenu, "#* Back", "#* Back", "#* Retour");

					// Get Subscribed Quotas
					GetQuotasCall getQuotasCall = new GetQuotasCall(beneficiaryItems, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(),
							beneficiaryItems.getSelectedValue(), null, null, null, null, new Value<Boolean>(true));

					// Display Quotas List
					{
						Menu quotasListMenu = new Menu(getQuotasCall, "Quotas for {ConsumerMSISDN}");
						quotasListMenu.getCaption().set("For {ConsumerMSISDN}:", "Pour {ConsumerMSISDN}:");
						quotasListMenu.getMoreText().set("#0 More", "#0 D'autre");
						MenuItems<ServiceQuota> quotasToChange = quotasListMenu.addItems("# {}", "No Quotas", getQuotasCall.getQuotaList());
						quotasToChange.getText().set("# Select {}", "# Choisir {}");
						quotasToChange.getEmptyText().set("None", "Rien");
						MenuItem addQuotaMenuItem = quotasListMenu.addItem("# Add Quota");
						addQuotaMenuItem.getText().set("# Add Service", "# Rajouter un service");
						quotasListMenu.setBackItem(subscribedMenu, "#* Back", "#* Back", "#* Retour");

						// Modify Quotas Menu
						{
							// Rate Delete Quota to fixup {QuotaName}
							RemoveQuotaCall rateRemoveQuotaCall = new RemoveQuotaCall(quotasToChange, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(),
									subscribeTest.getVariantID(), beneficiaryItems.getSelectedValue(), quotasToChange.getSelectedValue(), new Value<Boolean>(true));

							Menu modifyQuotasMenu = new Menu(rateRemoveQuotaCall, "{ConsumerMSISDN} {QuotaName}");
							modifyQuotasMenu.getCaption().set("For {ConsumerMSISDN}:", "Pour {ConsumerMSISDN}:");
							MenuItem deleteQuotaMenuItem = modifyQuotasMenu.addItem("# Delete Quota");
							deleteQuotaMenuItem.getText().set("# Remove {QuotaName}", "# Supprimer {QuotaName}");
							MenuItem increaseQuotaMenuItem = modifyQuotasMenu.addItem("# Increase Quota");
							increaseQuotaMenuItem.getText().set("# Change {QuotaName}", "# Modify le nombre de {QuotaName}"); // !!
							modifyQuotasMenu.addItem(subscribedMenu, "#* Back").getText().set("#* Retour", "#* Back");

							// Delete
							{
								// Confirm Delete Quota Menu
								Menu confirmDeleteQuotaMenu = new Menu(deleteQuotaMenuItem, "Confirm Delete Quota");
								confirmDeleteQuotaMenu.getCaption().set("Confirm you want to remove {QuotaName} from {ConsumerMSISDN} ?", "Voulez vous retirer {QuotaName} de {ConsumerMSISDN} ?");
								MenuItem confirmedDeleteQuotaMenuItem = confirmDeleteQuotaMenu.addItem("# Yes");
								confirmedDeleteQuotaMenuItem.getText().set("# Yes", "# Oui");
								confirmDeleteQuotaMenu.addItem(subscribedMenu, "#* Back").getText().set("#* Back", "#* Retour");

								// Delete Quota
								RemoveQuotaCall removeQuotaCall = new RemoveQuotaCall(confirmedDeleteQuotaMenuItem, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(),
										subscribeTest.getVariantID(), beneficiaryItems.getSelectedValue(), quotasToChange.getSelectedValue(), new Value<Boolean>(false));

								// Display Success
								Menu changedSuccessfullyMenu = new Menu(removeQuotaCall, "Successfully Removed {QuotaName} quota for {ConsumerMSISDN}");
								changedSuccessfullyMenu.getCaption().set("You have Removed {QuotaName} from {ConsumerMSISDN}", "Vous avez retire {QuotaName} de {ConsumerMSISDN}");
								MenuItem typeToContinue = changedSuccessfullyMenu.addItem(subscribedMenu, "Type #* to continue...");
								typeToContinue.getText().set("Type #* to Continue", "Tapez #* pour continuer");
							}

							// Get new Quantity
							{
								GetQuotaCall getQuotaCall = new GetQuotaCall(increaseQuotaMenuItem, errorDisplayContinue, //
										start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), beneficiaryItems.getSelectedValue(), //
										quotasToChange.getSelectedValue());

								Menu quantityMenu = new Menu(getQuotaCall, "Enter {Units} to share:");
								quantityMenu.getCaption().set("Enter number of {Units} to share:", "Entrer le nombre de {Units} à partager:");
								MenuItem orBack = quantityMenu.addItem(hasBeneficiariesMenu, "#* Back");
								orBack.getText().set("or #* to exit\n>", "ou #* pour sortir\n>");

								// Rate Quota
								{

									UpdateQuotaCall rateQuotaCall = new UpdateQuotaCall(quantityMenu, // After
											errorDisplayContinue, // On Error
											start.getSubscriberNumber(), // Subscriber
											start.getServiceID(), // Service
											subscribeTest.getVariantID(), // Variant
											beneficiaryItems.getSelectedValue(), // Member
											quotasToChange.getSelectedValue(), // QuotaID
											quantityMenu.getInputAmount(), // Amount
											new Value<Boolean>(true)); // Rate Only

									// Ask Confirmation
									{
										// Display Success
										Menu addConfirmationMenu = new Menu(rateQuotaCall, "Add {SharedQuantity} {Units} {QuotaName} at {Charge} USD?");
										addConfirmationMenu.getCaption().set(
										//
												"Confirm you want to share {SharedQuantity} {Units} {Destination} with {ConsumerMSISDN} {DaysOfWeek}, {TimeOfDay} at {Charge} USD", //
												"Confirmez que vous voulez partager {SharedQuantity} {Units} {Destination} avec {ConsumerMSISDN}, {DaysOfWeek} {TimeOfDay} pour {Charge} USD");
										MenuItem yesAddQuota = addConfirmationMenu.addItem("# Yes");
										yesAddQuota.getText().set("# Yes", "# Oui");
										MenuItem exitOption = addConfirmationMenu.addItem(subscribedMenu, "#* Back");
										exitOption.getText().set("#* Exit", "#* Sortir");

										// Update Quota
										{
											UpdateQuotaCall updateQuotaCall = new UpdateQuotaCall(yesAddQuota, // After
													errorDisplayContinue, // On Error
													start.getSubscriberNumber(), // Subscriber
													start.getServiceID(), // Service
													subscribeTest.getVariantID(), // Variant
													beneficiaryItems.getSelectedValue(), // Member
													quotasToChange.getSelectedValue(), // QuotaID
													quantityMenu.getInputAmount(), // Amount
													new Value<Boolean>(false)); // Rate Only

											// Display Success
											{
												// Display Success
												Menu addedSuccessfullyMenu = new Menu(updateQuotaCall, "Added {SharedQuantity} {Units} {Service} for {ConsumerMSISDN} at {Charge} USD");
												addedSuccessfullyMenu.getCaption().set( //
														"You have shared {SharedQuantity} {Units} {Destination} with {ConsumerMSISDN} {DaysOfWeek}, {TimeOfDay} for {Charge} USD", //
														"Vous avez patagé {SharedQuantity} {Units} {Destination} avec {ConsumerMSISDN}, {DaysOfWeek} {TimeOfDay} pour {Charge} USD");
												MenuItem addedSuccess = addedSuccessfullyMenu.addItem(subscribedMenu, "Type #* to continue...");
												addedSuccess.getText().set("Type #* to continue", "Tapez #* pour continuer");
											}
										}

									}
								}
							}

						}

						// Add
						{
							// Get Quota Types
							GetQuotasCall getQuotasTypes0 = new GetQuotasCall(addQuotaMenuItem, errorDisplayContinue, //
									start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), beneficiaryItems.getSelectedValue(), //
									null, null, null, null, new Value<Boolean>(false));

							// Service Type
							{
								Menu selectServiceMenu = new Menu(getQuotasTypes0, "Select Service Type");
								selectServiceMenu.getCaption().set("Select Service Type:", "Choisir le type de service:");
								selectServiceMenu.getMoreText().set("#0 More", "#0 D'autre");
								MenuItems<String> services = selectServiceMenu.addItems("# {}", "None Available", getQuotasTypes0.getServiceNames());
								services.getEmptyText().set("None", "Rien");
								selectServiceMenu.setBackItem(hasBeneficiariesMenu, "#* Back", "#* Back", "#* Retour");

								// Destination
								{
									GetQuotasCall getQuotasTypes1 = new GetQuotasCall(services, errorDisplayContinue, //
											start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), beneficiaryItems.getSelectedValue(), //
											services.getSelectedValue(), null, null, null, new Value<Boolean>(false));

									Menu destinationMenu = new Menu(getQuotasTypes1, "Select Destination");
									destinationMenu.getCaption().set("Destination:", "Select Destination:");
									destinationMenu.getMoreText().set("#0 More", "#0 D'autre");
									MenuItems<String> destinations = destinationMenu.addItems("# {}", "None Available", getQuotasTypes1.getDestinationNames());
									destinations.getEmptyText().set("None", "Rien");
									destinationMenu.setBackItem(hasBeneficiariesMenu, "#* Back", "#* Back", "#* Retour");

									// Day Of Week
									{
										GetQuotasCall getQuotasTypes2 = new GetQuotasCall(destinations, errorDisplayContinue, //
												start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), beneficiaryItems.getSelectedValue(), //
												services.getSelectedValue(), destinations.getSelectedValue(), null, null, new Value<Boolean>(false));

										Menu daysOfWeekMenu = new Menu(getQuotasTypes2, "Select Days of Week");
										daysOfWeekMenu.getCaption().set("Select Days of Week:", "Choisir les jours autorisés :");
										daysOfWeekMenu.getMoreText().set("#0 More", "#0 D'autre");
										MenuItems<String> daysOfWeek = daysOfWeekMenu.addItems("# {}", "None Available", getQuotasTypes2.getDaysOfWeekNames());
										daysOfWeek.getEmptyText().set("None", "Rien");
										daysOfWeekMenu.setBackItem(hasBeneficiariesMenu, "#* Back", "#* Back", "#* Retour");

										// Time of Day
										{
											GetQuotasCall getQuotasTypes3 = new GetQuotasCall(daysOfWeek, errorDisplayContinue, //
													start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), beneficiaryItems.getSelectedValue(), //
													services.getSelectedValue(), destinations.getSelectedValue(), daysOfWeek.getSelectedValue(), null, new Value<Boolean>(false));

											Menu timesOfWeekMenu = new Menu(getQuotasTypes3, "Select Times of Day");
											timesOfWeekMenu.getCaption().set("Select Time of Day:", "Choisir les heures autorisées:");
											timesOfWeekMenu.getMoreText().set("#0 More", "#0 D'autre");
											MenuItems<String> timesOfDay = timesOfWeekMenu.addItems("# {}", "None Available", getQuotasTypes3.getTimesOfDayNames());
											timesOfDay.getEmptyText().set("None", "Rien");
											timesOfWeekMenu.setBackItem(hasBeneficiariesMenu, "#* Back", "#* Back", "#* Retour");

											// Quantity
											{
												Menu quantityMenu = new Menu(timesOfDay, "Enter {Units} to share:");
												quantityMenu.getCaption().set("Enter number of {Units} to share:", "Entrer le nombre de {Units} à partager:");
												MenuItem orBack = quantityMenu.addItem(hasBeneficiariesMenu, "#* Back");
												orBack.getText().set("or #* to exit\n>", "ou #* pour sortir\n>");

												// Rate Quota
												{
													// Error Display with Continue to Quantity Menu
													ErrorDisplay errorDisplayQuantity = new ErrorDisplay(null, start.getServiceID(), "Type #* to continue?", quantityMenu);
													errorDisplayQuantity.getSuffixText().set("Type #* to continue", "Tapez #* pour continuer");

													AddQuotaCall rateQuotaCall = new AddQuotaCall(quantityMenu, // After
															errorDisplayQuantity, // On Error
															start.getSubscriberNumber(), // Subscriber
															start.getServiceID(), // Service
															subscribeTest.getVariantID(), // Variant
															beneficiaryItems.getSelectedValue(), // Member
															null, // QuotaID
															services.getSelectedValue(), // Service Type
															destinations.getSelectedValue(), // Destinations
															daysOfWeek.getSelectedValue(), // Days of Week
															timesOfDay.getSelectedValue(), // Times of Day
															quantityMenu.getInputAmount(), // Amount
															new Value<Boolean>(true)); // Rate Only

													// Ask Confirmation
													{
														// Display Success
														Menu addConfirmationMenu = new Menu(rateQuotaCall, "Add {SharedQuantity} {Units} {QuotaName} at {Charge} USD?");
														addConfirmationMenu
																.getCaption()
																.set( //
																"Confirm you want to share {SharedQuantity} {Units} {Destination} with {ConsumerMSISDN} {DaysOfWeek}, {TimeOfDay} at {Charge} USD", //
																"Confirmez que vous voulez partager {SharedQuantity} {Units} {Destination} avec {ConsumerMSISDN}, {DaysOfWeek} {TimeOfDay} pour {Charge} USD");

														MenuItem yesAddQuota = addConfirmationMenu.addItem("# Yes");
														yesAddQuota.getText().set("# Yes", "# Oui");
														MenuItem exitOption = addConfirmationMenu.addItem(subscribedMenu, "#* Back");
														exitOption.getText().set("#* Exit", "#* Retour");

														// Add Quota
														{
															AddQuotaCall addQuotaCall = new AddQuotaCall(yesAddQuota, // After Confirmation
																	errorDisplayContinue, // On Error
																	start.getSubscriberNumber(), // Subscriber
																	start.getServiceID(), // Service
																	subscribeTest.getVariantID(), // Variant
																	beneficiaryItems.getSelectedValue(), // Member
																	null, // QuotaID
																	services.getSelectedValue(), // Service Type
																	destinations.getSelectedValue(), // Destinations
																	daysOfWeek.getSelectedValue(), // Days of Week
																	timesOfDay.getSelectedValue(), // Times of Day
																	quantityMenu.getInputAmount(), // Amount
																	new Value<Boolean>(false)); // Not Rate Only

															// Display Success
															{
																// Display Success
																Menu addedSuccessfullyMenu = new Menu(addQuotaCall, "Added {SharedQuantity} {Units} {Service} for {ConsumerMSISDN} at {Charge} USD");
																addedSuccessfullyMenu
																		.getCaption()
																		.set( //
																		"You have shared {SharedQuantity} {Units} {Destination} with {ConsumerMSISDN} {DaysOfWeek}, {TimeOfDay} for {Charge} USD", //
																		"Vous avez patagé {SharedQuantity} {Units} pour {Service} {Destination} avec {ConsumerMSISDN}, {DaysOfWeek} {TimeOfDay} pour {Charge} USD");
																MenuItem addedSuccess = addedSuccessfullyMenu.addItem(subscribedMenu, "Type #* to continue...");
																addedSuccess.getText().set("Type #* to continue", "Tapez #* pour continuer");
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}

			// Change Language
			{

				// Get list of available Languages
				GetServiceCall getServicesCall = new GetServiceCall(languageMenuItem, errorDisplayContinue, start.getSubscriberNumber(), new Value<String>("LangCh"), new Value<Boolean>(false));

				// Ask the User to select
				Menu chooseLanguageMenu = new Menu(getServicesCall, "Select new Language");
				chooseLanguageMenu.getCaption().set("Select new Language:", "Modifier Langue:");
				MenuItems<String> languageItems = chooseLanguageMenu.addItems("# {}", "None Available", getServicesCall.getAvailableVariants());
				languageItems.getEmptyText().set("None", "Rien");
				chooseLanguageMenu.addItem(subscribedMenu, "#* Back").getText().set("#* Back", "#* Retour");

				// Confirm Migration Charge
				MigrateCall rateCall = new MigrateCall(languageItems, errorDisplayContinue, //
						start.getSubscriberNumber(), getServicesCall.getInServiceID(), getServicesCall.getFirstSubscribedVariant(), //
						getServicesCall.getInServiceID(), languageItems.getSelectedValue(), new Value<Boolean>(true));
				Test hasChargeTest = new Test(rateCall, errorDisplayEnd, rateCall.getHasCharge());
				Menu confirmChargeMenu = new Menu(hasChargeTest, "Confirm Charge:");
				confirmChargeMenu.getCaption().set("You are going to be charged {Charge} USD for changing language", "Vous allez etre débité de {Charge} USD pour changer de langue");
				MenuItem confirmYes = confirmChargeMenu.addItem("# Yes");
				confirmYes.getText().set("# Accept", "# Accepter");
				MenuItem confirmNo = confirmChargeMenu.addItem(subscribedMenu, "#* Back");
				confirmNo.getText().set("#* Back", "#* Retour");

				// Migrate
				MigrateCall migrateCall = new MigrateCall(confirmYes, errorDisplayContinue, //
						start.getSubscriberNumber(), getServicesCall.getInServiceID(), getServicesCall.getFirstSubscribedVariant(), //
						getServicesCall.getInServiceID(), languageItems.getSelectedValue(), new Value<Boolean>(false));
				hasChargeTest.setNoAction(migrateCall);

				// Display Success
				Menu changedLanguageSuccessfullyMenu = new Menu(migrateCall, "Changed Successfully");
				changedLanguageSuccessfullyMenu.getCaption().set("Language changed successfully", "Langue changée avec succès");
				changedLanguageSuccessfullyMenu.addItem(subscribeTest, "#* Back").getText().set("#* Back", "#* Retour");
			}

			// Balance Enquiry balancesMenuItem
			{

				// Confirm Subscription Charge
				GetBalancesCall rateCall = new GetBalancesCall(balancesMenuItem, errorDisplayContinue, start.getSubscriberNumber(), //
						start.getServiceID(), subscribeTest.getVariantID(), new Value<Boolean>(true), new Value<Boolean>(true));
				Test hasChargeTest = new Test(rateCall, errorDisplayEnd, rateCall.getHasCharge());
				Menu confirmChargeMenu = new Menu(hasChargeTest, "Confirm Charge:");
				confirmChargeMenu.getCaption().set("You are going to be charged {Charge} USD for viewing balance", "Vous allez etre débité de {Charge} USD pour la consultation des soldes");
				MenuItem confirmYes = confirmChargeMenu.addItem("# Accept");
				confirmYes.getText().set("# Accept", "# Accepter");
				MenuItem confirmNo = confirmChargeMenu.addItem(subscribedMenu, "#* Back");
				confirmNo.getText().set("#* Back", "#* Retour");

				GetBalancesCall getBalancesCall = new GetBalancesCall(confirmYes, errorDisplayContinue, start.getSubscriberNumber(), //
						start.getServiceID(), subscribeTest.getVariantID(), new Value<Boolean>(true), new Value<Boolean>(false));
				hasChargeTest.setNoAction(getBalancesCall);

				// Display Success
				Menu balancesMenu = new Menu(getBalancesCall, "SMS Follows");
				balancesMenu.getCaption().set( //
						"Thank you for Using Group Shared Accounts\nYou will shortly receive an SMS", //
						"Merci d'avoir utilisé Group Shared Accounts, vous recevrez un SMS dans quelques instants");
				balancesMenu.addItem(subscribedMenu, "#* Back");
			}

			// Unsubscribe
			{
				// Confirm Un-Subscription Charge
				UnsubscribeCall rateCall = new UnsubscribeCall(unsubscribeMenuItem, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(),
						new Value<Boolean>(true));
				Test hasChargeTest = new Test(rateCall, errorDisplayEnd, rateCall.getHasCharge());
				Menu confirmChargeMenu = new Menu(hasChargeTest, "Confirm Charge:");
				confirmChargeMenu.getCaption().set("You are going to be charged {Charge} USD for unsubscribing to the service",
						"Vous allez etre débité de {Charge} USD pour le désabonnement au service");
				MenuItem confirmYes = confirmChargeMenu.addItem("# Yes");
				confirmYes.getText().set("# Accept", "# Accepter");
				MenuItem confirmNo = confirmChargeMenu.addItem(subscribedMenu, "#* Back");
				confirmNo.getText().set("#* Back", "#* Retour");

				// Unsunscribe
				UnsubscribeCall unsubscribeCall = new UnsubscribeCall(confirmYes, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(),
						new Value<Boolean>(false));
				hasChargeTest.setNoAction(unsubscribeCall);

				// Display Success
				End unsubEnd = new End(unsubscribeCall, "You are no longer subscribed to Group Shared Accounts");
				unsubEnd.getMessage().set("You are no longer subscribed to Group Shared Accounts", "Vous n'etes plus abonné à Group Shared Accounts");
			}

		}

		// Not Subscribed
		{
			// Make Test
			MembershipTest membershipTest = new MembershipTest(null, errorDisplayEnd, start.getSubscriberNumber(), start.getServiceID(), null);
			subscribeTest.setNoAction(membershipTest);

			// Is Member
			{
				Menu memberMenu = new Menu(membershipTest, "Is Member");
				memberMenu.getCaption().set("Welcome to Group Shared Accounts", "Bienvenue à Group Shared Accounts");
				MenuItem viewBalance = memberMenu.addItem("# View Balance");
				viewBalance.getText().set("# View Shared Credit", "# Voir le credit partagé");
				MenuItem viewProvider = memberMenu.addItem("# View Provider");
				viewProvider.getText().set("# View provider", "# Voir le fournisseur");
				MenuItem optOut = memberMenu.addItem("# Opt Out");
				optOut.getText().set("# Opt Out", "# Décliner le service");
				MenuItem exit = memberMenu.addItem(end, "#* Exit");
				exit.getText().set("#* Exit", "#* Annuler");

				// View Balance
				{
					GetBalancesCall getBalancesCall = new GetBalancesCall(viewBalance, errorDisplayEnd, start.getSubscriberNumber(), //
							start.getServiceID(), null, new Value<Boolean>(true), new Value<Boolean>(false));

					// Display Success
					Menu balancesMenu = new Menu(getBalancesCall, "SMS Follows");
					balancesMenu.getCaption().set( //
							"Thank you for Using Group Shared Accounts.\nYou will shortly receive an SMS", "Merci d'avoir utilisé Group Shared Accounts, vous recevrez un SMS dans quelques instants");
					balancesMenu.addItem(memberMenu, "#* Back").getText().set("#* Retour", "#* Back");
				}

				// View Provider
				{
					Menu providerMenu = new Menu(viewProvider, "Provider: {ProviderMSISDN}");
					providerMenu.getCaption().set( //
							"Provider:\n{ProviderMSISDN}", "Fournisseur:\n{ProviderMSISDN}");
					providerMenu.addItem(memberMenu, "#* Back").getText().set("#* Back", "#* Retour");
				}

				// Opt Out
				{
					// Remove Beneficiary
					RemoveMemberCall optOutCall = new RemoveMemberCall(optOut, errorDisplayEnd, membershipTest.getOwner(), start.getServiceID(), subscribeTest.getVariantID(),
							start.getSubscriberNumber(), new Value<Boolean>(false));
					optOutCall.setNextAction(end);
				}
			}

			// Neither Member or Provider
			{
				// Get Available Variants
				GetServiceCall getServicesCall = new GetServiceCall(null, end, start.getSubscriberNumber(), start.getServiceID(), new Value<Boolean>(false));
				membershipTest.setNoAction(getServicesCall);

				// Subscribe
				{
					// Create Variants Menu
					Menu variantsMenu = new Menu(getServicesCall, "Select Subscription Type:");
					variantsMenu.getCaption().set("Welcome to Group Shared Accounts. Please choose your subscription package:", "Bienvenue à Group Shared Accounts. Choisissez votre abonnement:");
					MenuItems<String> variantMenuItems = variantsMenu.addItems("# {}", "None Available", getServicesCall.getAvailableVariants());
					variantMenuItems.getEmptyText().set("None", "Rien");
					MenuItem variantBack = variantsMenu.addItem(end, "#* Back");
					variantBack.getText().set("#* Exit", "#* Annuler");

					// Confirm Subscription Charge
					SubscribeCall rateCall = new SubscribeCall(variantMenuItems, errorDisplayEnd, start.getSubscriberNumber(), start.getServiceID(), variantMenuItems.getSelectedValue(),
							new Value<Boolean>(true));
					Test hasChargeTest = new Test(rateCall, errorDisplayEnd, rateCall.getHasCharge());
					Menu confirmChargeMenu = new Menu(hasChargeTest, "Confirm Charge:");
					confirmChargeMenu.getCaption().set("You are going to be charged {Charge} USD for the service subscription",
							"Vous allez etre débité de {Charge} USD pour la souscription au service");
					MenuItem confirmYes = confirmChargeMenu.addItem("# Accept");
					confirmYes.getText().set("# Accept", "# Accepter");
					MenuItem confirmNo = confirmChargeMenu.addItem(end, "#* Back");
					confirmNo.getText().set("#* Back", "#* Retour");

					// Subscribe the subscriber
					SubscribeCall subscribeCall = new SubscribeCall(confirmYes, errorDisplayEnd, start.getSubscriberNumber(), start.getServiceID(), variantMenuItems.getSelectedValue(),
							new Value<Boolean>(false));
					hasChargeTest.setNoAction(subscribeCall);
					subscribeCall.setNextAction(subscribeTest);

				}
			}
		}

		return start;
	}

}
