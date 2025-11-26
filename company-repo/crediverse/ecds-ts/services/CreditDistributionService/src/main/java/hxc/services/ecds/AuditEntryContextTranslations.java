package hxc.services.ecds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;

public class AuditEntryContextTranslations {
	
	private final static Logger logger = LoggerFactory.getLogger(AuditEntryContextTranslations.class);
	private static ObjectMapper mapper;
	private static HashMap<String, HashMap<String, AuditEntryContextTranslation>> messages = new HashMap<String, HashMap<String, AuditEntryContextTranslation>>();

	static
	{
		final String en = "en";
		final String fr = "fr";

		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		SimpleModule module = new SimpleModule();
		module.addSerializer(java.sql.Date.class, new DateSerializer());
		mapper.registerModule(module);

//NB!!! 64 Character MAX limit for key
		//Test for spelling mistakes:
		//from directory: ecds-ts/services/CreditDistributionService/src/main/java
		//#!/bin/bash
		//$for key in $(grep "addTranslations(messages, en," hxc/services/ecds/AuditEntryContextTranslations.java | sed 's~^[[:blank:]].*addTranslations(messages, en, "\([A-Z_]*\)",.*$~\1~'); do count=$(find . -name "*.java" | grep -v "hxc/services/ecds/AuditEntryContextTranslations.java" | xargs -I{} grep ""'"'"${key}"'"'"" {} | wc -l); echo "${key} ${count}"; done;
		
		//Agent.java
		addTranslations(messages, en, "LOADED_MRD_ROOT", "Loading MRD", "Creating Root Agent");
		addTranslations(messages, en, "AGENT_AUTH", "Authenticating Agent", "Authenticating Agent; ID %d");
		addTranslations(messages, en, "AGENT_PIN_CHANGE", "Agent Pin Change", "Agent ID %d");  
		
		//Agents.java
		addTranslations(messages, en, "AGENT_UPDATE", "Agent Updated", "Agent profile updated; ID %d");
		addTranslations(messages, en, "AGENT_REMOVE", "Agent Deleted", "Agent deleted; ID %d");
		//Agents.java
		addTranslations(messages, en, "AGENT_UPDATED_IMSI", "Agent IMSI Update", "IMSI updated for agent ID %d");
		addTranslations(messages, en, "AGENT_UPDATED_IMEI", "Agent IMEI Update", "IMEI updated for agent ID %d");
		addTranslations(messages, en, "AGENT_PROFILE_UPDATE", "Agent Profile Update", "Agent profile updated ID %d");
		addTranslations(messages, en, "AGENT_CREATED", "Agent Created", "New agent created. MSISDN %s");
		//AgentUser.java
		addTranslations(messages, en, "AGENTUSER_AUTH", "Authenticating Agent User", "Authenticating agent user ID %d");
		addTranslations(messages, en, "AGENTUSER_PIN_CHANGE", "Agent User Pin Changed", "Agent ID %d");
		addTranslations(messages, en, "AGENTUSER_UPDATED_IMSI", "Agent User IMSI Update", "IMSI updated for agent user ID %d");
		addTranslations(messages, en, "AGENTUSER_UPDATED_IMEI", "Agent User IMEI Update", "IMEI updated for agent user ID %d");
		addTranslations(messages, en, "AGENTUSER_UPDATE", "Agent User Update", "Agent ID: %d updated");
		addTranslations(messages, en, "AGENTUSER_CREATE", "Agent User Created", "New agent user created. MSISDN %s");
		//Areas.java
		addTranslations(messages, en, "AREA_UPDATE", "Area Updated", "Area %s updated %d");
		addTranslations(messages, en, "AREA_CREATED", "Area Created", "Area%s created");
		//Batch.java
		addTranslations(messages, en, "BATCH_UPLOAD_STARTED", "Batch File Upload Started", "Batch file %s upload started ID %d");
		addTranslations(messages, en, "BATCH_UPLOAD_COMPLETE", "Batch File Uploaded", "Batch file %s upload completed ID %d");
		//Bundles.java
		addTranslations(messages, en, "BUNDLE_UPDATE", "Bundle Updated", "Bundle updated ID %d");
		addTranslations(messages, en, "BUNDLE_CREATE", "Bundle Created", "Bundle %s created");
		addTranslations(messages, en, "BUNDLE_REMOVE", "Bundle Deleted", "Bundle %s removed ID %d");
		//Cells.java
		addTranslations(messages, en, "CELL_UPDATE", "Cell Updated", "Cell updated ID %d");
		addTranslations(messages, en, "CELL_CREATE", "Cell Created", "Cell %s created");
		addTranslations(messages, en, "CELL_AUTO_CREATE", "Cell Auto Created", "Cell %s created automatically");
		addTranslations(messages, en, "CELL_REMOVE", "Cell Deleted", "Cell %s deleted ID %d");
		
		addTranslations(messages, en, "CELLGROUP_UPDATE", "Cell Group Updated", "Cell Group updated ID %d");
		addTranslations(messages, en, "CELLGROUP_CREATE", "Cell Group Created", "Cell Group %s created");
		addTranslations(messages, en, "CELLGROUP_REMOVE", "Cell Group Deleted", "Cell Group deleted ID %d");
		
		addTranslations(messages, en, "CLIENTSTATE_CREATE", "Client State Created", "Client State Created");
		addTranslations(messages, en, "CLIENTSTATE_UPDATE", "Client State Updated", "Client State Updated");
		
		addTranslations(messages, en, "AGENTUSER_REMOVE", "Agent User Deleted", "Agent User deleted ID %d");
		
		addTranslations(messages, en, "WEBUSER_LOAD_MRD_ADMIN", "Loading Web User MRD", "Loading webuser MRD for the Administrator user");
		addTranslations(messages, en, "WEBUSER_LOAD_MRD_SUPPLIER", "Loading Web User MRD", "Loading webuser MRD for the Supplier user");
		
		addTranslations(messages, en, "WEBUSER_AUTH", "Authenticating Web User", "Web User ID %d");
		addTranslations(messages, en, "WEBUSER_PIN_CHANGE", "Web User Pin Changed", "Web User pin changedID %d");
		
		addTranslations(messages, en, "LOADED_MRD_COMPANY", "Loading MRD", "Loading company MRD %s");
		
		addTranslations(messages, en, "CONFIGURATION_UPDATE", "Configuration Updated", "Configuration updated for company %d");
		addTranslations(messages, en, "DEPARTMENT_CREATE_MRD", "Loading MRD", "Loading department MRD; created initial department");
		addTranslations(messages, en, "DEPARTMENT_CREATE", "Department Created", "Department %s created");
		addTranslations(messages, en, "DEPARTMENT_UPDATE", "Department Updated", "Department updated ID %d");
		addTranslations(messages, en, "DEPARTMENT_REMOVE", "Department Deleted", "Department deleted ID %d");
		addTranslations(messages, en, "WEBUSER_CREATE", "Webuser Created", "Webuser created");
		addTranslations(messages, en, "WEBUSER_PROFILE_UPDATE", "Webuser Profile Updated", "Webuser's profile updated ID %d");
		addTranslations(messages, en, "WEBUSER_UPDATE", "Webuser Updated", "Webuser updated ID %d");
		addTranslations(messages, en, "WEBUSER_REMOVE", "Webuser Deleted", "Webuser deleted ID %d");
		addTranslations(messages, en, "GROUP_UPDATE", "Group Updated", "Group updated ID %d");
		addTranslations(messages, en, "GROUP_CREATE", "Group Created", "Group %s created");
		addTranslations(messages, en, "GROUP_REMOVE", "Group Deleted", "Group deleted ID %d");
		addTranslations(messages, en, "LOADED_MRD_PERMISSIONS", "Loading MRD", "Loading permissions MRD, created permission %s; %s; group %s");
		
		addTranslations(messages, en, "PERMISSION_UPDATE","Permission Updated","Permission updated ID %d");
		addTranslations(messages, en, "PROMOTION_CREATE", "Promotion Created", "Promotion %s created");
		addTranslations(messages, en, "PROMOTION_REMOVE","Promotion Deleted","Promotion deleted ID %d");
		addTranslations(messages, en, "PROMOTION_UPDATED","Promotion Updated","Promotion Updated ID %d");
		addTranslations(messages, en, "LOAD_MRD_REPORT_SPECIFICATION","Loading MRD","Loading report specification MRD; created %s");
		addTranslations(messages, en, "LOAD_MRD_REPORT_SCHEDULE", "Loading MRD", "Loading report schedule MRD; created %s");
		addTranslations(messages, en, "REPORT_SCHEDULE_CREATE","Report Schedule Created","Report schedule %s created");
		addTranslations(messages, en, "REPORT_SCHEDULE_UPDATE","Report Schedule Updated","Report schedule updated ID %d");
		addTranslations(messages, en, "REPORT_SCHEDULE_WEBUSER_ADD","Webuser Added to Report Schedule","Webuser %d added to report schedule %d");
		addTranslations(messages, en, "REPORT_SCHEDULE_WEBUSER_REMOVE","Webuser Removed from Report Schedule", "Webuser %d removed from report schedule %d");
		addTranslations(messages, en, "RETAILER_PERFORMANCE_REPORT_CREATE","Retailer Performance Report Created","Retailer performance report %s created");
		addTranslations(messages, en, "WHOLESALER_PERFORMANCE_REPORT_CREATE","Wholesaler Performance Report Created", "Wholesaler performance report %s created");
		addTranslations(messages, en, "SALES_SUMMARY_REPORT_CREATE", "Sales Summary Report Created", "Sales summary report %s created");
		addTranslations(messages, en, "DAILY_GROUP_SALES_REPORT_CREATE","Daily Group Sales Report Created","Daily group sales report %s created");
		addTranslations(messages, en, "MONTHLY_SALES_PERFORMANCE_REPORT_CREATE", "Monthly Sales Performance Report Created", "Monthly sales performance report %s created");
		addTranslations(messages, en, "ACCOUNT_BALANCE_SUMMARY_REPORT_CREATE", "Account Balance Summary Report Created", "Account balance summary report %s created");
		addTranslations(messages, en, "ACCOUNT_BALANCE_SUMMARY_REPORT_UPDATE", "Account Balance Summary Report Updated", "Account balance summary report %s updated ID %d");
		addTranslations(messages, en, "RETAILER_REPORT_SPECIFICATION_UPDATE", "Retailer Report Specification Updated", "Retailer report specification %s updated ID %d");
		addTranslations(messages, en, "WHOLESALER_REPORT_SPECIFICATION_UPDATE", "Wholesaler Report Specification Updated", "Wholesaler report specification %s updated ID %d");
		addTranslations(messages, en, "SALES_SUMMARY_REPORT_SPECIFICATION_UPDATE", "Sales Summary Report Specification Updated", "Sales summary report specification %s updated ID %d");
		addTranslations(messages, en, "DAILY_GROUP_SALES_REPORT_SPECIFICATION_UPDATE", "Daily Group Sales Report Specification Updated", "Daily group sales report Specification %s updated ID %d");
		addTranslations(messages, en, "MONTHLY_SALES_PERFORMANCE_REPORT_SPECIFICATION_UPDATE", "Monthly Sales Performance Report Specification Updated", "Monthly sales performance report specification %s updated ID %d");
		addTranslations(messages, en, "LOADED_MRD_ROLE", "Loaded MRD", "Loaded role MRD, created role %s; %s");
		addTranslations(messages, en, "MRD_ROLE_UPDATED", "Role Updated", "Role %s updated ID %d");
		addTranslations(messages, en, "ROLE_CREATE", "Role Created", "Role %s created");
		addTranslations(messages, en, "ROLE_UPDATE", "Role Updated", "Role %s updated ID %d");
		addTranslations(messages, en, "SERVICE_CLASS_CREATE", "Service Class Created", "Service class %s created");
		addTranslations(messages, en, "SERVICE_CLASS_UPDATE", "Service Class Updated", "Service class %s updated ID %d");
		addTranslations(messages, en, "LOADED_MRD_STATE", "Loaded MRD", "Loaded state MRD, created state %s; value %d");
		addTranslations(messages, en, "LOADED_MRD_TIER", "Loaded MRD", "Loaded tier MRD, created tier %s");
		addTranslations(messages, en, "TRANSFER_RULE_REMOVE", "Transfer Rule Deleted", "Transfer rule %s deleted ID %d");
		addTranslations(messages, en, "TRANSFER_RULE_UPSERT", "Transfer Rule Upserted", "Transfer rule %s upserted ID %d");
		addTranslations(messages, en, "TRANSFER_RULE_TIER_UPDATE", "Tier Updated By Transfer Rule Update", "Tier %s updated ID %d");
		addTranslations(messages, en, "SERVICE_CLASS_REMOVE", "Service Class Deleted", "Service class %s deleted ID %d");
		addTranslations(messages, en, "TIER_CREATE", "Tier Created", "Tier %s created");
		addTranslations(messages, en, "TIER_UPDATE", "Tier Updated", "Tier %s updated ID %d");
		addTranslations(messages, en, "TIER_REMOVE", "Tier Deleted", "Tier %s deleted ID %d");
		addTranslations(messages, en, "WORK_ITEM_CREATE", "Work Item Created", "Work item created");
		addTranslations(messages, en, "WORK_ITEM_UPDATE", "Work Item Updated", "Work item updated ID %d");
		addTranslations(messages, en, "WORK_ITEM_REMOVE", "Work Item Deleted", "Work item deleted ID %d");
		addTranslations(messages, en, "AREA_REMOVE", "Area Updated", "Area %s updated ID %d");
		addTranslations(messages, en, "REPORT_SCHEDULE_REMOVE", "Report Schedule Deleted", "Report schedule %s deleted ID %d");
		addTranslations(messages, en, "RETAILER_PERFORMANCE_REPORT_REMOVE", "Retailer Performance Report Deleted", "Retailer performance report %s deleted ID %d");
		addTranslations(messages, en, "WHOLESALER_PERFORMANCE_REPORT_DELETE", "Wholesaler Performance Report Deleted", "Wholesaler performance report %s deleted ID %d");
		addTranslations(messages, en, "MONTHLY_SALES_PERFORMANCE_REPORT_DELETE", "Monthly Sales Performance Report", "Monthly sales performance report %s deleted ID %d");
		addTranslations(messages, en, "DAILY_GROUP_SALES_REPORT_DELETE", "Daily Group Sales Report", "Daily group sales report %s deleted ID %d");
		addTranslations(messages, en, "ACCOUNT_BALANCE_SUMMARY_REPORT_REMOVE", "Account Balance Summary Report Deleted", "Account balance summary report %s deleted ID %d");
		addTranslations(messages, en, "ROLE_REMOVE", "Role Deleted", "Role %s deleted ID %d");
		
		addTranslations(messages, en, "BATCH_IMPORT_ADD_NEW", "Batch Import Create", "Created type %s via batch import");
		addTranslations(messages, en, "BATCH_IMPORT_UPDATE_EXISTING", "Batch Import Update", "Updated type %s, ID %d via batch import");
		addTranslations(messages, en, "BATCH_IMPORT_DELETE_EXISTING", "Batch Import Delete", "Deleted type %s, ID %d via batch import");
		addTranslations(messages, en, "BATCH_IMPORT_COMPLETE", "Batch Import Completed", "Batch import completed");
//French
		addTranslations(messages, fr, "LOADED_MRD_ROOT", "Chargement MRD", "Création le compte ROOT");
		addTranslations(messages, fr, "AGENT_AUTH", "Authentification d'agent", "Authentification de l'ID de l'agent %d");
		addTranslations(messages, fr, "AGENT_PIN_CHANGE", "Changemenet de PIN", "ID de l'agent %d");  
		//Agents.java
		addTranslations(messages, fr, "AGENT_UPDATE", "Agent modifié", "Agent modifié ID: %d");
		addTranslations(messages, fr, "AGENT_REMOVE", "Agent supprimé", "Agent supprimé ID: %d");
		//Agents.java
		addTranslations(messages, fr, "AGENT_UPDATED_IMSI", "mis à jour l'IMSI de l'agent", "L'IMSI a modifié pour l'agent ID %d");
		addTranslations(messages, fr, "AGENT_UPDATED_IMEI", "mis à jour l'IMEI de l'agent", "L'IMEI a modifié pour l'agent ID %d");
		addTranslations(messages, fr, "AGENT_PROFILE_UPDATE", "mis à jour le profil de l'agent", "Le Profil de l'agent a modifié ID %d");
		addTranslations(messages, fr, "AGENT_CREATED", "Agent a créé", "Nouvel agent créé. MSISDN %s");
		//AgentUser.java
		addTranslations(messages, fr, "AGENTUSER_AUTH", "Authentification d'agent", "Authentification d'agent ID %d");
		addTranslations(messages, fr, "AGENTUSER_PIN_CHANGE", "PIN de l'agent modifiée", "L'ID de l'Agent %d");
		addTranslations(messages, fr, "AGENTUSER_UPDATED_IMSI", "mis à jour l'IMSI de l'utilisateur d'agent", "L'IMSI a modifié pour l'agent ID %d");
		addTranslations(messages, fr, "AGENTUSER_UPDATED_IMEI", "mis à jour l'IMEI de l'utilisateur d'agent", "L'IMEI a modifié pour l'agent ID %d");
		addTranslations(messages, fr, "AGENTUSER_UPDATE", "mis à jour l'utilisateur de l'agent", "L'ID d'agent: %d modifié");
		addTranslations(messages, fr, "AGENTUSER_CREATE", "L'utilisateur d'agent créé", "Nouvel utilisateur d'agent créé. MSISDN %s");
		//Areas.java
		addTranslations(messages, fr, "AREA_UPDATE", "Area modifié", "Area %s modifié %d");
		addTranslations(messages, fr, "AREA_CREATED", "Area créée", "Area %s créée");
		//Batch.java
		addTranslations(messages, fr, "BATCH_UPLOAD_STARTED", "Téléchargement de fichier batch commencé", "Téléchargement de fichier batch %s commencé ID %d");
		addTranslations(messages, fr, "BATCH_UPLOAD_COMPLETE", "fichier batch a téléchargé", "Téléchargement de fichier batch %s terminé ID %d");
		//Bundles.java
		addTranslations(messages, fr, "BUNDLE_UPDATE", "Forfait modifié", "Forfait modifié ID %d");
		addTranslations(messages, fr, "BUNDLE_CREATE", "Forfait créée", "Forfait %s créée");
		addTranslations(messages, fr, "BUNDLE_REMOVE", "Forfait supprimé", "Forfait %s supprimé ID %d");
		//Cells.java
		addTranslations(messages, fr, "CELL_UPDATE", "Cellule modifié", "Cellule modifié ID %d");
		addTranslations(messages, fr, "CELL_CREATE", "Cellule créée", "Cellule créée %s");
		addTranslations(messages, fr, "CELL_AUTO_CREATE", "Cellule créée automatiquement", "Cellule %s créée automatiquement");
		addTranslations(messages, fr, "CELL_REMOVE", "Cellule supprimé", "Cellule %s supprimé ID %d");
		
		addTranslations(messages, fr, "CELLGROUP_UPDATE", "Groupe de cellules modifié", "Groupe de cellules modifié ID %d");
		addTranslations(messages, fr, "CELLGROUP_CREATE", "Groupe de cellules créée", "Groupe de cellules %s créée");
		addTranslations(messages, fr, "CELLGROUP_REMOVE", "Groupe de cellules supprimé", "Groupe de cellules supprimé ID %d");
		
		addTranslations(messages, fr, "CLIENTSTATE_CREATE", "État du client créé", "État du client créé");
		addTranslations(messages, fr, "CLIENTSTATE_UPDATE", "État du client modifié", "État du client modifié");
		
		addTranslations(messages, fr, "AGENTUSER_REMOVE", "Utilisateur de l'agent supprimé", "Utilisateur de l'agent supprimé ID %d");
		
		addTranslations(messages, fr, "WEBUSER_LOAD_MRD_ADMIN", "Chargement de l'utilisateur Web MRD", "Chargement de l'utilisateur Web MRD pour l'utilisateur Administrateur");
		addTranslations(messages, fr, "WEBUSER_LOAD_MRD_SUPPLIER", "Chargement de l'utilisateur Web MRD", "Chargement de l'utilisateur Web MRD pour l'utilisateur supplier");
		
		addTranslations(messages, fr, "WEBUSER_AUTH", "Authentifié l'utilisateur Web", "Utilisateur Web ID %d");
		addTranslations(messages, fr, "WEBUSER_PIN_CHANGE", "Le PIN de l'utilisateur Web modifié", "Le PIN de l'utilisateur Web modifié ID %d");

		addTranslations(messages, fr, "LOADED_MRD_COMPANY", "Chargement MRD", "Chargement MRD de l'Entreprise %s");

		addTranslations(messages, fr, "CONFIGURATION_UPDATE", "Configuration modifié", "Configuration modifié pour l'Entreprise %d ID %d");
		addTranslations(messages, fr, "DEPARTMENT_CREATE_MRD", "Chargement MRD", "Chargement le département MRD; département initial créé");
		addTranslations(messages, fr, "DEPARTMENT_CREATE", "Département créé", "Département %s créé");
		addTranslations(messages, fr, "DEPARTMENT_UPDATE", "Département modifié", "Département modifié ID %d");
		addTranslations(messages, fr, "DEPARTMENT_REMOVE", "Département supprimé", "Département supprimé ID %d");
		addTranslations(messages, fr, "WEBUSER_CREATE", "L'utilisateur Web créé", "L'utilisateur Web créé");
		addTranslations(messages, fr, "WEBUSER_PROFILE_UPDATE", "Profil de l'utilisateur Web modifié", "Profil de l'utilisateur Web modifié ID %d");
		addTranslations(messages, fr, "WEBUSER_UPDATE", "L'utilisateur Web modifié", "L'utilisateur Web modifié ID %d");
		addTranslations(messages, fr, "WEBUSER_REMOVE", "L'utilisateur Web supprimé", "L'utilisateur Web supprimé ID %d");
		addTranslations(messages, fr, "GROUP_UPDATE", "Groupe modifié", "Groupe modifié ID %d");
		addTranslations(messages, fr, "GROUP_CREATE", "Groupe créé", "Groupe %s créé");
		addTranslations(messages, fr, "GROUP_REMOVE", "Groupe supprimé", "Groupe supprimé ID %d");
		addTranslations(messages, fr, "LOADED_MRD_PERMISSIONS", "Chargement MRD", "Chargement les permissions MRD, permission créé %s; %s; groupe %s");

		addTranslations(messages, fr, "PERMISSION_UPDATE","Permission modifié","Permission modifié ID %d");
		addTranslations(messages, fr, "PROMOTION_CREATE", "Promotion créé", "Promotion %s créé");
		addTranslations(messages, fr, "PROMOTION_REMOVE","Promotion supprimé","Promotion supprimé ID %d");
		addTranslations(messages, fr, "PROMOTION_UPDATED","Promotion modifié","Promotion modifié ID %d");
		addTranslations(messages, fr, "LOAD_MRD_REPORT_SPECIFICATION","Chargement MRD","Chargement de la spécification de rapport MRD; créé %s");
		addTranslations(messages, fr, "LOAD_MRD_REPORT_SCHEDULE", "Chargement MRD", "Chargement la planification de rapport MRD; créé %s");
		addTranslations(messages, fr, "REPORT_SCHEDULE_CREATE","Planification de rapport créé","Planification de rapport %s créé");
		addTranslations(messages, fr, "REPORT_SCHEDULE_UPDATE","Planification de rapport modifié","Planification de rapport modifié ID %d");
		addTranslations(messages, fr, "REPORT_SCHEDULE_WEBUSER_ADD","L’utilisateur Web a ajouté à la planification de rapport","L’utilisateur Web %d a ajouté à la planification de rapport %d");
		addTranslations(messages, fr, "REPORT_SCHEDULE_WEBUSER_REMOVE","L’utilisateur Web a supprimé de la planification de rapport", "L’utilisateur Web %d a supprimé de la planification de rapport %d");
		addTranslations(messages, fr, "RETAILER_PERFORMANCE_REPORT_CREATE","Rapport du performance des détaillants créé","Rapport du performance des détaillants %s créé");
		addTranslations(messages, fr, "WHOLESALER_PERFORMANCE_REPORT_CREATE","Rapport du performance des grossistes créé", "Rapport du performance des grossistes %s créé");
		addTranslations(messages, fr, "SALES_SUMMARY_REPORT_CREATE", "Rapport résumé des ventes créé", "Rapport résumé des ventes %s créé");
		addTranslations(messages, fr, "DAILY_GROUP_SALES_REPORT_CREATE","Rapport quotidien sur les ventes du groupe créé","Rapport quotidien sur les ventes du groupe %s créé");
		addTranslations(messages, fr, "MONTHLY_SALES_PERFORMANCE_REPORT_CREATE", "Rapport mensuel sur les performances des ventes créé", "Rapport mensuel sur les performances des ventes %s créé");
		addTranslations(messages, fr, "ACCOUNT_BALANCE_SUMMARY_REPORT_CREATE", "Rapport résumé du solde du compte créé", "Rapport résumé du solde du compte %s créé");
		addTranslations(messages, fr, "ACCOUNT_BALANCE_SUMMARY_REPORT_UPDATE", "Rapport résumé du solde du compte modifié", "Rapport résumé du solde du compte %s modifié ID %d");
		addTranslations(messages, fr, "RETAILER_REPORT_SPECIFICATION_UPDATE", "La specification du rapport des détaillants modifié", "Rapport de specification des détaillants %s modifié ID %d");
		addTranslations(messages, fr, "WHOLESALER_REPORT_SPECIFICATION_UPDATE", "La specification du rapport des grossistes modifié", "Rapport de specification des grossistes %s modifié ID %d");
		addTranslations(messages, fr, "SALES_SUMMARY_REPORT_SPECIFICATION_UPDATE", "La specification du rapport résumé des ventes modifié", "La specification du rapport résumé des ventes %s modifié ID %d");
		addTranslations(messages, fr, "DAILY_GROUP_SALES_REPORT_SPECIFICATION_UPDATE", "La specification du Rapport quotidien sur les ventes du groupe modifié", "La specification du Rapport quotidien sur les ventes du groupe %s modifié ID %d");
		addTranslations(messages, fr, "MONTHLY_SALES_PERFORMANCE_REPORT_SPECIFICATION_UPDATE", "Spécification du rapport mensuel sur les performances des ventes", "La spécification du Rapport mensuel sur les performances des ventes %s modifié ID %d");
		addTranslations(messages, fr, "LOADED_MRD_ROLE", "MRD téléchargé", "role MRD téléchargé, role créé %s; %s");
		addTranslations(messages, fr, "MRD_ROLE_UPDATED", "Role modifié", "Role %s modifié ID %d");
		addTranslations(messages, fr, "ROLE_CREATE", "Role créé", "Role %s créé");
		addTranslations(messages, fr, "ROLE_UPDATE", "Role modifié", "Role %s modifié ID %d");
		addTranslations(messages, fr, "SERVICE_CLASS_CREATE", "Classe du service créé", "Classe du service %s créé");
		addTranslations(messages, fr, "SERVICE_CLASS_UPDATE", "Classe du service modifié", "Classe du service %s modifié ID %d");
		addTranslations(messages, fr, "LOADED_MRD_STATE", "MRD téléchargé", "Etat MRD téléchargé, Etat créé %s; valeur %d");
		addTranslations(messages, fr, "LOADED_MRD_TIER", "MRD téléchargé", "Loaded tier MRD, created tier %s");
		addTranslations(messages, fr, "TRANSFER_RULE_REMOVE", "Règle de transfert supprimée", "Règle de transfert %s supprimée ID %d");
		addTranslations(messages, fr, "TRANSFER_RULE_UPSERT", "Règle de transfert Bouleversé", "Règle de transfert %s Bouleversé ID %d");
		addTranslations(messages, fr, "TRANSFER_RULE_TIER_UPDATE", "Tier Updated By Transfer Rule Update", "Tier %s ID %d updated as a side-effect of a transfer rule update");
		addTranslations(messages, fr, "SERVICE_CLASS_REMOVE", "Classe du service supprimé", "Classe du service %s supprimé ID %d");
		addTranslations(messages, fr, "TIER_CREATE", "Niveau créé", "Niveau %s créé");
		addTranslations(messages, fr, "TIER_UPDATE", "Niveau modifié", "Niveau %s modifié ID %d");
		addTranslations(messages, fr, "TIER_REMOVE", "Niveau supprimé", "Niveau %s supprimé ID %d");
		addTranslations(messages, fr, "WORK_ITEM_CREATE", "Élément de travail créé", "Work item created");
		addTranslations(messages, fr, "WORK_ITEM_UPDATE", "Élément de travail modifié", "Élément de travail modifié ID %d");
		addTranslations(messages, fr, "WORK_ITEM_REMOVE", "Élément de travail supprimé", "Élément de travail supprimé ID %d");
		addTranslations(messages, fr, "AREA_REMOVE", "Area modifié", "Area %s modifié ID %d");
		addTranslations(messages, fr, "REPORT_SCHEDULE_REMOVE", "Planification de rapport supprimé", "Planification de rapport %s supprimé ID %d");
		addTranslations(messages, fr, "RETAILER_PERFORMANCE_REPORT_REMOVE", "Rapport du performance des détaillants supprimé", "Rapport du performance des détaillants %s supprimé ID %d");
		addTranslations(messages, fr, "WHOLESALER_PERFORMANCE_REPORT_DELETE", "Rapport du performance des grossistes supprimé", "Rapport du performance des grossistes %s supprimé ID %d");
		addTranslations(messages, fr, "MONTHLY_SALES_PERFORMANCE_REPORT_DELETE", "Ventes mensuelles du groupe", "Ventes mensuelles du groupe %s supprimé ID %d");
		addTranslations(messages, fr, "DAILY_GROUP_SALES_REPORT_DELETE", "Rapport quotidien sur les ventes du groupe", "Rapport quotidien sur les ventes du groupe %s supprimé ID %d");
		addTranslations(messages, fr, "ACCOUNT_BALANCE_SUMMARY_REPORT_REMOVE", "Rapport résumé du solde du compte supprimé", "Rapport résumé du solde du compte %s supprimé ID %d");
		addTranslations(messages, fr, "ROLE_REMOVE", "Role supprimé", "Role %s supprimé ID %d");
		addTranslations(messages, fr, "BATCH_IMPORT_UPDATE_EXISTING", "Batch Import Update", "Updated type %s, ID %d via batch import");
		addTranslations(messages, fr, "BATCH_IMPORT_ADD_NEW", "Importation par batch créé", "Type créé %s via importation par batch");
		addTranslations(messages, fr, "BATCH_IMPORT_DELETE_EXISTING", "Importation par batch supprimé", "Type supprimé %s, ID %d via importation par batch");
		addTranslations(messages, fr, "BATCH_IMPORT_COMPLETE", "Importation par batch Terminé", "Importation par batch Terminé");	}
	
	private static void addTranslations(HashMap<String, HashMap<String, AuditEntryContextTranslation>> map, String language, String key, String headline, String description)
	{
		HashMap<String, AuditEntryContextTranslation> temp;
		if(messages.containsKey(key))
		{
			temp = messages.get(key);
		} else {
			temp = new HashMap<String, AuditEntryContextTranslation>();
		}
		AuditEntryContextTranslation context = new AuditEntryContextTranslation(headline, description); 
		temp.put(language, context);
		messages.put(key, temp);
		
	}

	public static String translateReason(String key, String language)
	{
		if(messages.containsKey(key))
		{
			HashMap<String, AuditEntryContextTranslation> temp = messages.get(key);
			if(temp.containsKey(language))
			{
				AuditEntryContextTranslation context = temp.get(language);
				return context.getHeadline();
			}
		}
		return "";
	}
	
	public static String translateReasonDetail(String key, String language, Object... args)
	{
		if(messages.containsKey(key))
		{
			HashMap<String, AuditEntryContextTranslation> temp = messages.get(key);
			if(temp.containsKey(language))
			{
				AuditEntryContextTranslation context = temp.get(language);
				try {
					return String.format(context.getDescription(), args);
				} catch (Exception e) //specifically watching for following mistakes MissingFormatArgumentException | IllegalFormatConversionException  
				{
					ArrayList<Object> list = new ArrayList<Object> (Arrays.asList(args));
					String argList = list.stream().map(Object::toString).collect(Collectors.joining(","));
					if(argList.isEmpty())
						logger.error(String.format("String format bug in Audit Log Context Description [%s] with empty argument list.", context.getDescription()), e);
					else
						logger.error(String.format("String format bug in Audit Log Context Description: [%s] argument list: [%s]", context.getDescription(), argList), e );
				} 
			}
		}
		return "";
	}
}
