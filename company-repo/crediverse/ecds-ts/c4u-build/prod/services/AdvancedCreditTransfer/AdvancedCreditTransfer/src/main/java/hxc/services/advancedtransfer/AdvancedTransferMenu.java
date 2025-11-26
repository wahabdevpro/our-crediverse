package hxc.services.advancedtransfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.Number;

import hxc.processmodel.IProcess;
import hxc.utils.processmodel.AddCreditTransferCall;
import hxc.utils.processmodel.ChangePinCall;
import hxc.utils.processmodel.End;
import hxc.utils.processmodel.ErrorDisplay;
import hxc.utils.processmodel.GetMembersCall;
import hxc.utils.processmodel.GetServiceCall;
import hxc.utils.processmodel.GetTransfersCall;
import hxc.utils.processmodel.Menu;
import hxc.utils.processmodel.MenuItem;
import hxc.utils.processmodel.MenuItems;
import hxc.utils.processmodel.MigrateCall;
import hxc.utils.processmodel.RemoveCreditTransferCall;
import hxc.utils.processmodel.RemoveMemberCall;
import hxc.utils.processmodel.ResumeCreditTransferCall;
import hxc.utils.processmodel.SubscribeCall;
import hxc.utils.processmodel.SubscriptionTest;
import hxc.utils.processmodel.SuspendCreditTransferCall;
import hxc.utils.processmodel.Test;
import hxc.utils.processmodel.UnsubscribeCall;
import hxc.utils.processmodel.UssdStart;
import hxc.utils.processmodel.Value;

public class AdvancedTransferMenu
{
	final static Logger logger = LoggerFactory.getLogger(AdvancedTransferMenu.class);

	public static IProcess getMenuProcess(String serviceID)
	{
		
		// Start with Short Code 143
		UssdStart start = new UssdStart(serviceID, "UssdMenu");

		// Error exit
		ErrorDisplay errorDisplayEnd = new ErrorDisplay(null, start.getServiceID());

		// Normal Exit
		End end = new End(null, "Thank you for using the Credit Transfer Service");
		end.getMessage().set("Thank you for Using Advanced Credit Transfer", "Merci d'avoir utilisé Advanced Credit Transfer");

		// Make Test
		SubscriptionTest subscribeTest = new SubscriptionTest(start, errorDisplayEnd, start.getSubscriberNumber(), start.getServiceID(), null);

		// Root
		Menu rootMenu = new Menu(subscribeTest, "Advanced Credit Transfer Service");
		rootMenu.getCaption().set("Welcome to Advanced Credit Transfer", "Bienvenue à Advanced Credit Transfer");
		rootMenu.getMoreText().set("#0 More", "#0 D'autre");

		{
			// Error Display with Continue to Subscribed Menu
			ErrorDisplay errorDisplayContinue = new ErrorDisplay(null, start.getServiceID(), "Type #* to continue", rootMenu);
			errorDisplayContinue.getSuffixText().set("Type #* to continue", "Tapez #* pour continuer");

			// Add Menu Items
			MenuItem viewMenuItem = rootMenu.addItem("# View Recipients");
			viewMenuItem.getText().set("# View Recipients", "# Voir vos bénéficiaires");
			MenuItem addMenuItem = rootMenu.addItem("# Add Recipient");
			addMenuItem.getText().set("# Add Recipient", "# Ajouter un bénéficiaire");
			MenuItem removeMenuItem = rootMenu.addItem("# Remove Recipient");
			removeMenuItem.getText().set("# Remove Recipient", "# Enlever un bénéficiaire");
			MenuItem changeMenuItem = rootMenu.addItem("# Change Transfers");
			changeMenuItem.getText().set("# Change Transfers", "# Modifier Transfers");
			MenuItem viewDonors = rootMenu.addItem("# View Donors");
			viewDonors.getText().set("# View provider", "# Voir le fournisseur");
			MenuItem languageMenuItem = rootMenu.addItem("# Change Language");
			languageMenuItem.getText().set("# Change Language", "# Changer de langue");
			MenuItem unsubscribeMenuItem = rootMenu.addItem("# Unsubscribe");
			unsubscribeMenuItem.getText().set("# Unsubscribe", "# Se désabonner");
			rootMenu.setBackItem(end, "#* Exit", "#* Back", "#* Retour"); // !!

			// View Recipients
			{
				// Get list of Recipients
				GetMembersCall getRecipients = new GetMembersCall(viewMenuItem, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID());

				// Display Recipients
				{
					Menu hasRecipientsMenu = new Menu(getRecipients, "Your current beneficiaries:");
					hasRecipientsMenu.getCaption().set("Recipients List:", "Numéros bénéficiaires:");
					hasRecipientsMenu.getMoreText().set("#0 D'autre", "#0 More");
					MenuItems<Number> listItems = hasRecipientsMenu.addItems("{}", "You don't have any beneficiaries", getRecipients.getMembers());
					listItems.getEmptyText().set("None", "Rien");
					hasRecipientsMenu.setBackItem(rootMenu, "Type #* to Continue", "#* Back", "#* Retour");
				}

			}

			// Add Recipient
			{
				// Ask for Recipient's Number
				Menu addRecipientMenu = new Menu(addMenuItem, "Add Recipient");
				addRecipientMenu.getCaption().set("Enter Number:", "Entrer le Numéro:");
				MenuItem addRecipientItem = addRecipientMenu.addItem(rootMenu, "Enter Recipient's Number or #* to go back");
				addRecipientItem.getText().set("or #* to go back\n> ", "ou #* pour rentrer\n> ");

				// Add
				{

					// Transfer Type
					{

						// Get Transfer Types
						GetTransfersCall getTransferTypes = new GetTransfersCall(addRecipientMenu, errorDisplayContinue, //
								start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), addRecipientMenu.getInputNumber(), null, new Value<Boolean>(false));

						Menu selectServiceMenu = new Menu(getTransferTypes, "Select Transfer Mode");
						selectServiceMenu.getCaption().set("Select Transfer Mode:", "Choisir le type de service:");
						selectServiceMenu.getMoreText().set("#0 More", "#0 D'autre");
						MenuItems<String> transferNames = selectServiceMenu.addItems("# {}", "None Available", getTransferTypes.getAvailableTransferNames());
						transferNames.getEmptyText().set("None", "Rien");
						selectServiceMenu.setBackItem(rootMenu, "#* Back", "#* Back", "#* Retour");

						// Quantity
						{
							GetTransfersCall dummyGetTransferTypes = new GetTransfersCall(transferNames, errorDisplayContinue, //
									start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), addRecipientMenu.getInputNumber(), //
									transferNames.getSelectedValue(), new Value<Boolean>(false));

							Menu quantityMenu = new Menu(dummyGetTransferTypes, "Enter {Units} to transfer:");
							quantityMenu.getCaption().set("Enter number of {Units} to transfer:", "Entrer le nombre de {Units} à partager:");
							MenuItem orBack = quantityMenu.addItem(rootMenu, "#* Back");
							orBack.getText().set("or #* to go back\n>", "ou #* pour rentrer\n>");
							quantityMenu.setNumerator(dummyGetTransferTypes.getNumerator());
							quantityMenu.setDenominator(dummyGetTransferTypes.getDemominator());

							// Error Display with Continue to Quantity Menu
							ErrorDisplay addCreditTransferErrorDisplay = new ErrorDisplay(null, start.getServiceID(), "Type #* to continue", getTransferTypes);
							addCreditTransferErrorDisplay.getSuffixText().set("Type #* to continue", "Tapez #* pour continuer");

							AddCreditTransferCall testTransferCall = new AddCreditTransferCall(quantityMenu, addCreditTransferErrorDisplay, //
									start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), addRecipientMenu.getInputNumber(), //
									transferNames.getSelectedValue(), quantityMenu.getInputAmount(), quantityMenu.getInputTotal(), null, new Value<Boolean>(true));

							// Transfer Limit
							{
								Menu transferLimitMenu = new Menu(testTransferCall, "Enter Max {Units} per to transfer:");
								transferLimitMenu.getCaption().set("Enter maximum {Units} {TransferMode} to transfer:", "Entrer le nombre de {Units} {TransferMode}  à partager:");
								MenuItem orBack1 = transferLimitMenu.addItem(rootMenu, "#* Back");
								orBack1.getText().set("or #* to go back\n>", "ou #* pour rentrer\n>");
								transferLimitMenu.setNumerator(dummyGetTransferTypes.getNumerator());
								transferLimitMenu.setDenominator(dummyGetTransferTypes.getDemominator());

								// Rate Transfer
								{
									AddCreditTransferCall rateTransferCall = new AddCreditTransferCall(transferLimitMenu, addCreditTransferErrorDisplay, //
											start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), addRecipientMenu.getInputNumber(), //
											transferNames.getSelectedValue(), quantityMenu.getInputAmount(), transferLimitMenu.getInputTotal(), null, new Value<Boolean>(true));

									// Ask Confirmation
									{
										// Display Confirmation
										Menu addConfirmationMenu = new Menu(rateTransferCall,
												"Confirm you want to transfer {Quantity} {Units} {TransferMode} to {RecipientMSISDN} at {Charge} {Currency}");
										addConfirmationMenu.getCaption().set( //
												"Confirm you want to add an Auto Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN} at {Charge} {Currency}", //
												"Confirm you want to add an Auto Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN} at {Charge} {Currency}");

										MenuItem yesAddTransfer = addConfirmationMenu.addItem("# Yes");
										yesAddTransfer.getText().set("# Yes", "# Oui");
										MenuItem exitOption = addConfirmationMenu.addItem(rootMenu, "#* Back");
										exitOption.getText().set("#* Exit", "#* Retour");

										// Test if PIN not Present
										Test missingPinTest = new Test(yesAddTransfer, errorDisplayContinue, dummyGetTransferTypes.isPinMissing());

										// Request PIN
										{
											Menu getPinMenu = new Menu(missingPinTest, "Enter PIN");
											getPinMenu.getCaption().set("You need to create a PIN first", "FR: You need to create a PIN first");
											MenuItem addPinItem = getPinMenu.addItem(rootMenu, "Enter PIN or #* to go back");
											addPinItem.getText().set("Enter PIN or #* to go back\n> ", "FR: Enter PIN or #* to go back\n> ");

											// Create PIN
											{
												ChangePinCall createPinCall = new ChangePinCall(getPinMenu, errorDisplayContinue, start.getSubscriberNumber(), //
														new Value<String>("PIN"), new Value<String>("DEF"), new Value<String>("1111"), getPinMenu.getInputText(), new Value<Boolean>(false));

												// Add Transfer
												{
													AddCreditTransferCall addTransferCall = new AddCreditTransferCall(createPinCall,
															errorDisplayContinue, //
															start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(),
															addRecipientMenu.getInputNumber(), //
															transferNames.getSelectedValue(), quantityMenu.getInputAmount(), transferLimitMenu.getInputTotal(), getPinMenu.getInputText(),
															new Value<Boolean>(false));

													// Display Success
													{
														// Display Success
														Menu addedSuccessfullyMenu = new Menu(addTransferCall,
																"You will transfer {Quantity} {Units} {TransferMode} to {RecipientMSISDN} at {Charge} {Currency}");
														addedSuccessfullyMenu.getCaption().set( //
																"You have added an Auto Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN}", //
																"You have added an Auto Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN}");
														MenuItem addedSuccess = addedSuccessfullyMenu.addItem(rootMenu, "Type #* to continue...");
														addedSuccess.getText().set("Type #* to continue", "Tapez #* pour continuer");
													}
												}
											}

										}

										// Request PIN
										{
											Test requresPINTest = new Test(null, errorDisplayContinue, rateTransferCall.getPinRequired());
											missingPinTest.setNoAction(requresPINTest);
											Menu getPinMenu = new Menu(requresPINTest, "Enter PIN");
											getPinMenu.getCaption().set("Enter PIN:", "FR: Enter PIN");
											MenuItem addPinItem = getPinMenu.addItem(rootMenu, "Enter PIN or #* to go back");
											addPinItem.getText().set("or #* to go back\n> ", "ou #* pour rentrer\n> ");

											// Add Transfer
											{
												AddCreditTransferCall addTransferCall = new AddCreditTransferCall(getPinMenu,
														errorDisplayContinue, //
														start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(),
														addRecipientMenu.getInputNumber(), //
														transferNames.getSelectedValue(), quantityMenu.getInputAmount(), transferLimitMenu.getInputTotal(), getPinMenu.getInputText(),
														new Value<Boolean>(false));
												requresPINTest.setNoAction(addTransferCall);

												// Display Success
												{
													// Display Success
													Menu addedSuccessfullyMenu = new Menu(addTransferCall,
															"You will transfer {Quantity} {Units} {TransferMode} to {RecipientMSISDN} at {Charge} {Currency}");
													addedSuccessfullyMenu.getCaption().set( //
															"You have added an Auto Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN}", //
															"You have added an Auto Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN}");
													MenuItem addedSuccess = addedSuccessfullyMenu.addItem(rootMenu, "Type #* to continue...");
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

			// Remove Recipient
			{
				// Get list of Recipients
				GetMembersCall getRecipients = new GetMembersCall(removeMenuItem, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID());

				// Display Removal Menu
				{
					// Select Recipient
					Menu removeRecipientsMenu = new Menu(getRecipients, "Recipient to remove:");
					removeRecipientsMenu.getCaption().set("Recipient to Remove", "Bénéficiaire à enlever");
					removeRecipientsMenu.getMoreText().set("#0 More", "#0 D'autre");
					MenuItems<Number> RecipientItems = removeRecipientsMenu.addItems("#) {}", "No Recipients", getRecipients.getMembers());
					RecipientItems.getEmptyText().set("None", "Rien");
					removeRecipientsMenu.setBackItem(rootMenu, "#* Back", "#* Back", "#* Retour");

					// Confirm Remove Recipient Charge
					RemoveMemberCall rateCall = new RemoveMemberCall(RecipientItems, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(),
							RecipientItems.getSelectedValue(), new Value<Boolean>(true));
					Test hasChargeTest = new Test(rateCall, errorDisplayEnd, rateCall.getHasCharge());
					Menu confirmChargeMenu = new Menu(hasChargeTest, "Confirm Charge:");
					confirmChargeMenu.getCaption().set("You are going to be charged {Charge} {Currency} for removing a Recipient",
							"Vous allez etre débité de {Charge} {Currency} pour la suppression d'un bénéficiaire");
					MenuItem confirmYes = confirmChargeMenu.addItem("# Accept");
					confirmYes.getText().set("# Accept", "# Accepter");
					MenuItem confirmNo = confirmChargeMenu.addItem(rootMenu, "#* Back");
					confirmNo.getText().set("#* Back", "#* Retour");

					// Remove Recipient
					RemoveMemberCall removeMemberCall = new RemoveMemberCall(confirmYes, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(),
							RecipientItems.getSelectedValue(), new Value<Boolean>(false));
					hasChargeTest.setNoAction(removeMemberCall);

					// Display Success
					Menu addedSuccessfullyMenu = new Menu(removeMemberCall, "Successfully removed {RecipientMSISDN}");
					addedSuccessfullyMenu.getCaption().set("You are no longer sharing credit with {RecipientMSISDN}", "Vous ne partagez plus de crédit avec {RecipientMSISDN}");
					addedSuccessfullyMenu.addItem(rootMenu, "#* Back").getText().set("Type ## to continue", "Tapez ## pour continuer");
				}
			}

			// Change Transfers
			{
				// Get list of Recipients
				GetMembersCall getRecipients = new GetMembersCall(changeMenuItem, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID());

				// Select Recipients
				{
					// Select Recipient
					Menu hasRecipientsMenu = new Menu(getRecipients, "Select Recipient to change:");
					hasRecipientsMenu.getCaption().set("Select Recipient", "Numéros à modifier");
					hasRecipientsMenu.getMoreText().set("#0 More", "#0 D'autre");
					MenuItems<Number> RecipientItems = hasRecipientsMenu.addItems("#) {}", "No Recipients", getRecipients.getMembers());
					RecipientItems.getEmptyText().set("None", "Rien");
					hasRecipientsMenu.setBackItem(rootMenu, "#* Back", "#* Back", "#* Retour");

					// Get Subscribed Quotas
					GetTransfersCall getTransfersCall = new GetTransfersCall(RecipientItems, errorDisplayContinue, //
							start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), //
							RecipientItems.getSelectedValue(), null, new Value<Boolean>(true));

					// Display Transfer List
					{
						Menu transfersListMenu = new Menu(getTransfersCall, "Transfers for {RecipientMSISDN}");
						transfersListMenu.getCaption().set("For {RecipientMSISDN}:", "Pour {RecipientMSISDN}:");
						transfersListMenu.getMoreText().set("#0 More", "#0 D'autre");
						MenuItems<String> transfersToChange = transfersListMenu.addItems("# {}", "No Transfers", getTransfersCall.getTransferValues());
						transfersToChange.getText().set("# {}", "# {}");
						transfersToChange.getEmptyText().set("None", "Rien");
						MenuItem addTransferMenuItem = transfersListMenu.addItem("# Add Transfer");
						addTransferMenuItem.getText().set("# Add Transfer", "# Rajouter un service");
						transfersListMenu.setBackItem(rootMenu, "#* Back", "#* Back", "#* Retour");

						// Modify Transfers Menu
						{
							// Rate Delete Transfer to {TransferMode}
							RemoveCreditTransferCall rateRemoveTransferCall = new RemoveCreditTransferCall(transfersToChange, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(),
									subscribeTest.getVariantID(), RecipientItems.getSelectedValue(), transfersToChange.getSelectedValue(), new Value<Boolean>(true));

							Menu modifyTransfersMenu = new Menu(rateRemoveTransferCall, "{RecipientMSISDN} {TransferMode}");
							modifyTransfersMenu.getCaption().set("For {RecipientMSISDN}:", "Pour {RecipientMSISDN}:");

							MenuItem deleteTransferMenuItem = modifyTransfersMenu.addItem("# Cancel Transfer");
							deleteTransferMenuItem.getText().set("# Cancel {TransferMode}", "# Supprimer {TransferMode}");

							MenuItem suspendTransferMenuItem = modifyTransfersMenu.addItem("# Suspend Transfer");
							suspendTransferMenuItem.getText().set("# Suspend {TransferMode}", "# Suspend {TransferMode}");

							MenuItem resumeTransferMenuItem = modifyTransfersMenu.addItem("# Resume Transfer");
							resumeTransferMenuItem.getText().set("# Resume {TransferMode}", "# Resume {TransferMode}");

							// MenuItem increaseTransferMenuItem = modifyTransfersMenu.addItem("# Adjust Transfer");
							// increaseTransferMenuItem.getText().set("# Adjust {TransferMode}", "# Augmenter le nombre de {TransferMode}"); // !!
							modifyTransfersMenu.addItem(rootMenu, "#* Back").getText().set("#* Back", "#* Retour");

							// Cancel
							{
								// Confirm Delete Transfer Menu
								Menu confirmDeleteTransferMenu = new Menu(deleteTransferMenuItem, "Confirm Cancel Transfer");
								MenuItem confirmedDeleteTransferMenuItem = confirmDeleteTransferMenu.addItem("# Yes");
								confirmedDeleteTransferMenuItem.getText().set("# Yes", "# Oui");
								confirmDeleteTransferMenu.addItem(rootMenu, "#* Back").getText().set("#* Back", "#* Retour");

								// Delete Transfer
								RemoveCreditTransferCall removeTransferCall = new RemoveCreditTransferCall(confirmedDeleteTransferMenuItem, errorDisplayContinue, start.getSubscriberNumber(),
										start.getServiceID(), subscribeTest.getVariantID(), RecipientItems.getSelectedValue(), transfersToChange.getSelectedValue(), new Value<Boolean>(false));

								// Display Success
								Menu changedSuccessfullyMenu = new Menu(removeTransferCall, "Successfully Cancelled {TransferMode} for {RecipientMSISDN}");
								changedSuccessfullyMenu.getCaption().set("You have Cancelled {TransferMode} from {RecipientMSISDN}", "Vous avez retire {TransferMode} de {RecipientMSISDN}");
								MenuItem typeToContinue = changedSuccessfullyMenu.addItem(rootMenu, "Type #* to continue...");
								typeToContinue.getText().set("Type #* to Continue", "Tapez #* pour continuer");
							}

							// Suspend
							{
								// Confirm Suspend Transfer Menu
								Menu confirmSuspendTransferMenu = new Menu(suspendTransferMenuItem, "Confirm Suspend Transfer");
								MenuItem confirmedSuspendTransferMenuItem = confirmSuspendTransferMenu.addItem("# Yes");
								confirmedSuspendTransferMenuItem.getText().set("# Yes", "# Oui");
								confirmSuspendTransferMenu.addItem(rootMenu, "#* Back").getText().set("#* Back", "#* Retour");

								// Suspend Transfer
								SuspendCreditTransferCall suspendTransferCall = new SuspendCreditTransferCall(confirmedSuspendTransferMenuItem, errorDisplayContinue, start.getSubscriberNumber(),
										start.getServiceID(), subscribeTest.getVariantID(), RecipientItems.getSelectedValue(), transfersToChange.getSelectedValue(), new Value<Boolean>(false));

								// Display Success
								Menu suspendedSuccessfullyMenu = new Menu(suspendTransferCall, "Successfully suspended {TransferMode} for {RecipientMSISDN}");
								suspendedSuccessfullyMenu.getCaption().set("You have Suspended {TransferMode} for {RecipientMSISDN}", "Vous avez suspended {TransferMode} de {RecipientMSISDN}");
								MenuItem typeToContinue = suspendedSuccessfullyMenu.addItem(rootMenu, "Type #* to continue...");
								typeToContinue.getText().set("Type #* to Continue", "Tapez #* pour continuer");
							}

							// Resume
							{
								// Confirm Resume Transfer Menu
								Menu confirmResumeTransferMenu = new Menu(resumeTransferMenuItem, "Confirm Resume Transfer");
								MenuItem confirmedResumeTransferMenuItem = confirmResumeTransferMenu.addItem("# Yes");
								confirmedResumeTransferMenuItem.getText().set("# Yes", "# Oui");
								confirmResumeTransferMenu.addItem(rootMenu, "#* Back").getText().set("#* Back", "#* Retour");

								// Resume Transfer
								ResumeCreditTransferCall resumeTransferCall = new ResumeCreditTransferCall(confirmedResumeTransferMenuItem, errorDisplayContinue, start.getSubscriberNumber(),
										start.getServiceID(), subscribeTest.getVariantID(), RecipientItems.getSelectedValue(), transfersToChange.getSelectedValue(), new Value<Boolean>(false));

								// Display Success
								Menu resumedSuccessfullyMenu = new Menu(resumeTransferCall, "Successfully resumed {TransferMode} for {RecipientMSISDN}");
								resumedSuccessfullyMenu.getCaption().set("You have Resumed {TransferMode} for {RecipientMSISDN}", "Vous avez Resumed {TransferMode} de {RecipientMSISDN}");
								MenuItem typeToContinue = resumedSuccessfullyMenu.addItem(rootMenu, "Type #* to continue...");
								typeToContinue.getText().set("Type #* to Continue", "Tapez #* pour continuer");
							}

							// Get new Quantity
							// {
							// GetQuotaCall getQuotaCall = new GetQuotaCall(increaseQuotaMenuItem, errorDisplayContinue, //
							// start.getSubscriberNumber(), start.getServiceID(), null, RecipientItems.getSelectedValue(), //
							// transfersToChange.getSelectedValue());
							//
							// Menu quantityMenu = new Menu(getQuotaCall, "Enter {Units} to share:");
							// quantityMenu.getCaption().set("Enter number of {Units} to share:", "Entrer le nombre de {Units} à partager:");
							// MenuItem orBack = quantityMenu.addItem(hasRecipientsMenu, "#* Back");
							// orBack.getText().set("or #* to go back\n>", "ou #* pour rentrer\n>");
							//
							// // Rate Quota
							// {
							//
							// UpdateQuotaCall rateQuotaCall = new UpdateQuotaCall(quantityMenu, // After
							// errorDisplayContinue, // On Error
							// start.getSubscriberNumber(), // Subscriber
							// start.getServiceID(), // Service
							// null, // Variant
							// RecipientItems.getSelectedValue(), // Member
							// transfersToChange.getSelectedValue(), // QuotaID
							// quantityMenu.getInputAmount(), // Amount
							// new Value<Boolean>(true)); // Rate Only
							//
							// // Ask Confirmation
							// {
							// // Display Success
							// Menu addConfirmationMenu = new Menu(rateQuotaCall, "Add {SharedQuantity} {Units} {QuotaName} at {Charge} CFR?");
							// addConfirmationMenu.getCaption().set(
							// //
							// "Confirm you want to share {SharedQuantity} {Units} {Destination} with {RecipientMSISDN} {DaysOfWeek}, {TimeOfDay} at {Charge} {Currency}",
							// "Confirmez que vous voulez partager {SharedQuantity} {Units} {Destination} avec {RecipientMSISDN}, {DaysOfWeek} {TimeOfDay} pour {Charge} {Currency}");
							// MenuItem yesAddQuota = addConfirmationMenu.addItem("# Yes");
							// yesAddQuota.getText().set("# Yes", "# Oui");
							// MenuItem exitOption = addConfirmationMenu.addItem(rootMenu, "#* Back");
							// exitOption.getText().set("#* Exit", "#* Sortir");
							//
							// // Update Quota
							// {
							// UpdateQuotaCall updateQuotaCall = new UpdateQuotaCall(yesAddQuota, // After
							// errorDisplayContinue, // On Error
							// start.getSubscriberNumber(), // Subscriber
							// start.getServiceID(), // Service
							// null, // Variant
							// RecipientItems.getSelectedValue(), // Member
							// transfersToChange.getSelectedValue(), // QuotaID
							// quantityMenu.getInputAmount(), // Amount
							// new Value<Boolean>(false)); // Rate Only
							//
							// // Display Success
							// {
							// // Display Success
							// Menu addedSuccessfullyMenu = new Menu(updateQuotaCall, "Added {SharedQuantity} {Units} {Service} for {RecipientMSISDN} at {Charge} {Currency}");
							// addedSuccessfullyMenu.getCaption().set( //
							// "You have shared {SharedQuantity} {Units} {Destination} with {RecipientMSISDN} {DaysOfWeek}, {TimeOfDay} for {Charge} {Currency}", //
							// "Vous avez patagé {SharedQuantity} {Units} {Destination} avec {RecipientMSISDN}, {DaysOfWeek} {TimeOfDay} pour {Charge} {Currency}");
							// MenuItem addedSuccess = addedSuccessfullyMenu.addItem(rootMenu, "Type #* to continue...");
							// addedSuccess.getText().set("Type #* to continue", "Tapez #* pour continuer");
							// }
							// }
							//
							// }
							// }
							// }
							//

						}

						// Add
						{
							// Transfer Type
							{

								// Get Transfer Types
								GetTransfersCall getTransferTypes = new GetTransfersCall(addTransferMenuItem, errorDisplayContinue, //
										start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), RecipientItems.getSelectedValue(), null, new Value<Boolean>(false));

								Menu selectServiceMenu = new Menu(getTransferTypes, "Select Transfer Mode");
								selectServiceMenu.getCaption().set("Select Transfer Mode:", "Choisir le type de service:");
								selectServiceMenu.getMoreText().set("#0 More", "#0 D'autre");
								MenuItems<String> transferNames = selectServiceMenu.addItems("# {}", "None Available", getTransferTypes.getAvailableTransferNames());
								transferNames.getEmptyText().set("None", "Rien");
								selectServiceMenu.setBackItem(hasRecipientsMenu, "#* Back", "#* Back", "#* Retour");

								// Quantity
								{
									GetTransfersCall dummyGetTransferTypes = new GetTransfersCall(transferNames, errorDisplayContinue, //
											start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), RecipientItems.getSelectedValue(), //
											transferNames.getSelectedValue(), new Value<Boolean>(false));

									Menu quantityMenu = new Menu(dummyGetTransferTypes, "Enter {Units} to transfer:");
									quantityMenu.getCaption().set("Enter number of {Units} to transfer:", "Entrer le nombre de {Units} à partager:");
									MenuItem orBack = quantityMenu.addItem(hasRecipientsMenu, "#* Back");
									orBack.getText().set("or #* to go back\n>", "ou #* pour rentrer\n>");
									quantityMenu.setNumerator(dummyGetTransferTypes.getNumerator());
									quantityMenu.setDenominator(dummyGetTransferTypes.getDemominator());

									AddCreditTransferCall testTransferCall = new AddCreditTransferCall(quantityMenu, errorDisplayContinue, //
											start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), RecipientItems.getSelectedValue(), //
											transferNames.getSelectedValue(), quantityMenu.getInputAmount(), quantityMenu.getInputTotal(), null, new Value<Boolean>(true));

									// Transfer Limit
									{
										Menu transferLimitMenu = new Menu(testTransferCall, "Enter Max {Units} per to transfer:");
										transferLimitMenu.getCaption().set("Enter maximum {Units} {TransferMode} to transfer:", "Entrer le nombre de {Units} {TransferMode}  à partager:");
										MenuItem orBack1 = transferLimitMenu.addItem(hasRecipientsMenu, "#* Back");
										orBack1.getText().set("or #* to go back\n>", "ou #* pour rentrer\n>");
										transferLimitMenu.setNumerator(dummyGetTransferTypes.getNumerator());
										transferLimitMenu.setDenominator(dummyGetTransferTypes.getDemominator());

										// Rate Transfer
										{
											AddCreditTransferCall rateTransferCall = new AddCreditTransferCall(transferLimitMenu, errorDisplayContinue, //
													start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(), RecipientItems.getSelectedValue(), //
													transferNames.getSelectedValue(), quantityMenu.getInputAmount(), transferLimitMenu.getInputTotal(), null, new Value<Boolean>(true));

											// Ask Confirmation
											{
												// Display Success
												Menu addConfirmationMenu = new Menu(rateTransferCall,
														"Confirm you want to transfer {Quantity} {Units} {TransferMode} to {RecipientMSISDN} at {Charge} {Currency}");
												addConfirmationMenu.getCaption().set( //
														"Confirm you want to add an Auto Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN} at {Charge} {Currency}", //
														"Confirm you want to add an Auto Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN} at {Charge} {Currency}");

												MenuItem yesAddTransfer = addConfirmationMenu.addItem("# Yes");
												yesAddTransfer.getText().set("# Yes", "# Oui");
												MenuItem exitOption = addConfirmationMenu.addItem(rootMenu, "#* Back");
												exitOption.getText().set("#* Exit", "#* Retour");

												// Request PIN
												{
													Test requresPINTest = new Test(yesAddTransfer, errorDisplayContinue, rateTransferCall.getPinRequired());
													Menu getPinMenu = new Menu(requresPINTest, "Enter PIN");
													getPinMenu.getCaption().set("Enter PIN:", "FR: Enter PIN");
													MenuItem addPinItem = getPinMenu.addItem(rootMenu, "Enter PIN or #* to go back");
													addPinItem.getText().set("or #* to go back\n> ", "ou #* pour rentrer\n> ");

													// Add Transfer
													{

														AddCreditTransferCall addTransferCall = new AddCreditTransferCall(getPinMenu,
																errorDisplayContinue, //
																start.getSubscriberNumber(), start.getServiceID(), subscribeTest.getVariantID(),
																RecipientItems.getSelectedValue(), //
																transferNames.getSelectedValue(), quantityMenu.getInputAmount(), transferLimitMenu.getInputTotal(), getPinMenu.getInputText(),
																new Value<Boolean>(false));
														requresPINTest.setNoAction(addTransferCall);

														// Display Success
														{
															// Display Success
															Menu addedSuccessfullyMenu = new Menu(addTransferCall,
																	"You will transfer {Quantity} {Units} {TransferMode} to {RecipientMSISDN} at {Charge} {Currency}");
															addedSuccessfullyMenu.getCaption().set( //
																	"You have added an Auto Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN}", //
																	"You have added an Auto Transfer of {Quantity} {Units} {TransferMode} to {RecipientMSISDN}");
															MenuItem addedSuccess = addedSuccessfullyMenu.addItem(rootMenu, "Type #* to continue...");
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

			// View Donors
			{
				Menu providerMenu = new Menu(viewDonors, "Sponsor: {DonorMSISDN}");
				providerMenu.getCaption().set( //
						"Sponsor:\n{DonorMSISDN}", //
						"Fournisseur:\n{DonorMSISDN}");
				providerMenu.addItem(rootMenu, "#* Back").getText().set("#* Back", "#* Retour");
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
				chooseLanguageMenu.addItem(rootMenu, "#* Back").getText().set("#* Back", "#* Retour");

				// Confirm Migration Charge
				MigrateCall rateCall = new MigrateCall(languageItems, errorDisplayContinue, //
						start.getSubscriberNumber(), getServicesCall.getInServiceID(), getServicesCall.getFirstSubscribedVariant(), //
						getServicesCall.getInServiceID(), languageItems.getSelectedValue(), new Value<Boolean>(true));
				Test hasChargeTest = new Test(rateCall, errorDisplayEnd, rateCall.getHasCharge());
				Menu confirmChargeMenu = new Menu(hasChargeTest, "Confirm Charge:");
				confirmChargeMenu.getCaption().set("You are going to be charged {Charge} {Currency} for changing language", "Vous allez etre débité de {Charge} {Currency pour changer de langue");
				MenuItem confirmYes = confirmChargeMenu.addItem("# Yes");
				confirmYes.getText().set("# Accept", "# Accepter");
				MenuItem confirmNo = confirmChargeMenu.addItem(rootMenu, "#* Back");
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

			// Unsubscribe
			{
				// Get Available Variants
				GetServiceCall getServicesCall = new GetServiceCall(unsubscribeMenuItem, end, start.getSubscriberNumber(), start.getServiceID(), new Value<Boolean>(false));

				// Create Variants Menu
				Menu variantsMenu = new Menu(getServicesCall, "Select Subscription Type:");
				variantsMenu.getCaption().set("Choose package to unsubscribe:", "Choisissez votre abonnement:");
				MenuItems<String> variantMenuItems = variantsMenu.addItems("# {}", "None Available", getServicesCall.getSubscribedVariants());
				variantMenuItems.getEmptyText().set("None", "Rien");
				MenuItem variantBack = variantsMenu.addItem(end, "#* Back");
				variantBack.getText().set("#* Exit", "#* Annuler");

				// Confirm Un-Subscription Charge
				UnsubscribeCall rateCall = new UnsubscribeCall(variantMenuItems, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), variantMenuItems.getSelectedValue(),
						new Value<Boolean>(true));
				Test hasChargeTest = new Test(rateCall, errorDisplayEnd, rateCall.getHasCharge());
				Menu confirmChargeMenu = new Menu(hasChargeTest, "Confirm Charge:");
				confirmChargeMenu.getCaption().set("You are going to be charged {Charge} {Currency} for unsubscribing from the {Variant} service",
						"Vous allez etre débité de {Charge} {Currency} pour le désabonnement au service");
				MenuItem confirmYes = confirmChargeMenu.addItem("# Yes");
				confirmYes.getText().set("# Accept", "# Accepter");
				MenuItem confirmNo = confirmChargeMenu.addItem(rootMenu, "#* Back");
				confirmNo.getText().set("#* Back", "#* Retour");

				// Unsunscribe
				UnsubscribeCall unsubscribeCall = new UnsubscribeCall(confirmYes, errorDisplayContinue, start.getSubscriberNumber(), start.getServiceID(), variantMenuItems.getSelectedValue(),
						new Value<Boolean>(false));
				hasChargeTest.setNoAction(unsubscribeCall);

				// Display Success
				Menu subscribedSuccessfullyMenu = new Menu(unsubscribeCall, "Unubscribed successfully");
				subscribedSuccessfullyMenu.getCaption().set( //
						"You have been unsubscribed from {Variant} Advanced Credit Transfer at {Charge} {Currency}", //
						"Vous avez unsouscrit à un abonnement {Variant} à Advanced Credit Transfer pour {Charge} {Currency}");
				MenuItem successMenuItem = subscribedSuccessfullyMenu.addItem(end, "Type #* to continue...");
				successMenuItem.getText().set("Type ## to Continue", "Tapez ## pour continuer");
			}

		}

		{
			// Select Variant to Subscribe
			Menu subscribeMenu = new Menu(null, "Credit Sharing Service");
			subscribeMenu.getCaption().set("Welcome to Advanced Credit Transfer", "FR: Welcome to Advanced Credit Transfer");
			subscribeTest.setNoAction(subscribeMenu);
			MenuItem subscribeOption = subscribeMenu.addItem("# Subscribe");
			subscribeOption.getText().set("# Subscribe", "# Souscrire");
			MenuItem exitMenuItem = subscribeMenu.addItem(end, "# Exit");
			exitMenuItem.getText().set("#* Exit", "#* Annuler");

			// Subscribe
			{
				// Get Available Variants
				GetServiceCall getServicesCall = new GetServiceCall(subscribeOption, end, start.getSubscriberNumber(), start.getServiceID(), new Value<Boolean>(false));

				// Create Variants Menu
				Menu variantsMenu = new Menu(getServicesCall, "Select Subscription Type:");
				variantsMenu.getCaption().set("Choose your subscription package:", "Choisissez votre abonnement:");
				MenuItems<String> variantMenuItems = variantsMenu.addItems("# {}", "None Available", getServicesCall.getAvailableVariants());
				variantMenuItems.getEmptyText().set("None", "Rien");
				MenuItem variantBack = variantsMenu.addItem(end, "#* Back");
				variantBack.getText().set("#* Exit", "#* Annuler");

				// Confirm Subscription Charge
				SubscribeCall rateCall = new SubscribeCall(variantMenuItems, errorDisplayEnd, start.getSubscriberNumber(), start.getServiceID(), variantMenuItems.getSelectedValue(),
						new Value<Boolean>(true));
				Test hasChargeTest = new Test(rateCall, errorDisplayEnd, rateCall.getHasCharge());
				Menu confirmChargeMenu = new Menu(hasChargeTest, "Confirm Charge:");
				confirmChargeMenu.getCaption().set("You are going to be charged {Charge} {Currency} for the service subscription",
						"Vous allez etre débité de {Charge} {Currency} pour la souscription au service");
				MenuItem confirmYes = confirmChargeMenu.addItem("# Accept");
				confirmYes.getText().set("# Accept", "# Accepter");
				MenuItem confirmNo = confirmChargeMenu.addItem(rootMenu, "#* Back");
				confirmNo.getText().set("#* Back", "#* Retour");

				// Subscribe the subscriber
				SubscribeCall subscribeCall = new SubscribeCall(confirmYes, errorDisplayEnd, start.getSubscriberNumber(), start.getServiceID(), variantMenuItems.getSelectedValue(),
						new Value<Boolean>(false));
				hasChargeTest.setNoAction(subscribeCall);

				// Display Success
				Menu subscribedSuccessfullyMenu = new Menu(subscribeCall, "Subscribed successfully");
				subscribedSuccessfullyMenu.getCaption().set( //
						"You have been subscribed to {Variant} Advanced Credit Transfer at {Charge} {Currency} until {ExpiryDate} {ExpiryTime}", //
						"Vous avez souscrit à un abonnement {Variant} à Advanced Credit Transfer pour {Charge} {Currency} jusqu'au {ExpiryDate} {ExpiryTime}");
				MenuItem successMenuItem = subscribedSuccessfullyMenu.addItem(subscribeTest, "Type #* to continue...");
				successMenuItem.getText().set("Type ## to Continue", "Tapez ## pour continuer");
			}

		}

		return start;
	}
}
