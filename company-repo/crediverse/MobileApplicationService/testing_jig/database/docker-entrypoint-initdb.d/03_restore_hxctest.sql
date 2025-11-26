use hxctest; 
-- MariaDB dump 10.19  Distrib 10.10.3-MariaDB, for Linux (x86_64)
--
-- Host: 0.0.0.0    Database: hxctest
-- ------------------------------------------------------
-- Server version	10.6.12-MariaDB-1:10.6.12+maria~ubu2004

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `CF_Config`
--

DROP TABLE IF EXISTS `CF_Config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CF_Config` (
  `SerialVersionUID` bigint(20) NOT NULL,
  `name` varchar(80) NOT NULL,
  `sequence` int(11) NOT NULL,
  `value` varbinary(512) DEFAULT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`SerialVersionUID`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CF_Config`
--

LOCK TABLES `CF_Config` WRITE;
/*!40000 ALTER TABLE `CF_Config` DISABLE KEYS */;
INSERT INTO `CF_Config` VALUES
(4155099346172906269,'airPath',5,'/Air','2023-02-28 09:45:37'),
(4155099346172906269,'airPort',4,'\0\0\'\Z','2023-02-28 09:45:37'),
(4155099346172906269,'autoStateSaveIntervalMinutes',11,'\0\0\'`','2023-02-28 09:45:37'),
(4155099346172906269,'currency',9,'CFR','2023-02-28 09:45:37'),
(4155099346172906269,'isFit',12,'','2023-02-28 09:45:37'),
(4155099346172906269,'maxBacklog',8,'\0\0\0','2023-02-28 09:45:37'),
(4155099346172906269,'maxThreadPoolSize',7,'\0\0\0','2023-02-28 09:45:37'),
(4155099346172906269,'soapPath',3,'/Air','2023-02-28 09:45:37'),
(4155099346172906269,'soapPort',2,'\0\0\'','2023-02-28 09:45:37'),
(4155099346172906269,'stateFilename',10,'/tmp/C4U/air_sim_state.json','2023-02-28 09:45:37'),
(4155099346172906269,'threadQueueCapacity',6,'\0\0\0','2023-02-28 09:45:37'),
(4155099346172906269,'version',1,'\0\0\0','2023-02-28 09:45:37');
/*!40000 ALTER TABLE `CF_Config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ct_role`
--

DROP TABLE IF EXISTS `ct_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ct_role` (
  `serverRoleName` varchar(32) NOT NULL,
  `exclusive` bit(1) NOT NULL,
  `attachCommand` varchar(255) DEFAULT NULL,
  `detachCommand` varchar(255) DEFAULT NULL,
  `owner` varchar(64) DEFAULT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`serverRoleName`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_role`
--

LOCK TABLES `ct_role` WRITE;
/*!40000 ALTER TABLE `ct_role` DISABLE KEYS */;
INSERT INTO `ct_role` VALUES
('DatabaseServer','',NULL,NULL,NULL,'2023-02-02 03:57:25');
/*!40000 ALTER TABLE `ct_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ct_server`
--

DROP TABLE IF EXISTS `ct_server`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ct_server` (
  `serverHost` varchar(64) NOT NULL,
  `peerHost` varchar(64) DEFAULT NULL,
  `transactionNumberPrefix` varchar(50) DEFAULT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`serverHost`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ct_server`
--

LOCK TABLES `ct_server` WRITE;
/*!40000 ALTER TABLE `ct_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `ct_server` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `se_perm`
--

DROP TABLE IF EXISTS `se_perm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `se_perm` (
  `permissionId` varchar(50) NOT NULL,
  `category` varchar(50) NOT NULL,
  `path` varchar(250) NOT NULL,
  `description` varchar(80) NOT NULL,
  `implies` varchar(50) NOT NULL,
  `roles` bigint(20) NOT NULL,
  `control` int(11) NOT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`permissionId`,`category`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `se_perm`
--

LOCK TABLES `se_perm` WRITE;
/*!40000 ALTER TABLE `se_perm` DISABLE KEYS */;
INSERT INTO `se_perm` VALUES
('CAI Simulator','CaiSim','hxc.services.caisim','CAI Simulator Permissions','',3,0,'2023-02-02 04:19:38'),
('ChangeAirParameters','Air','hxc.connectors.air','Change Air Parameters','ViewAirParameters',1,0,'2023-02-02 04:19:38'),
('ChangeAirSimParameters','Air Simulator','hxc.services.airsim','Change Air Simulator Parameters','ViewAirSimParameters',1,0,'2023-02-02 04:19:38'),
('ChangeArchivingDestination','ArchivingDestination','hxc.connectors.archiving','Change Archiving Destination Parameters','ViewArchivingDestination',1,0,'2023-02-02 04:19:38'),
('ChangeArchivingParameters','Archiving','hxc.connectors.archiving','Change Archiving Connector Parameters','ViewArchivingParameters',1,0,'2023-02-02 04:19:38'),
('ChangeC4USutParameters','C4U System Under Test','hxc.connectors.sut','Change Air Simulator Parameters','ViewC4USutParameters',1,0,'2023-02-02 04:19:38'),
('ChangeCaiParameters','Cai','hxc.connectors.cai','Change Cai Parameters','ViewCaiParameters',1,0,'2023-02-02 04:19:38'),
('ChangeCdrParameters','CDR','hxc.services.transactions','Change CDR Parameters','ViewCdrParameters',1,0,'2023-02-02 04:19:38'),
('ChangeCreditDistributionParameters','Financial Services','hxc.services.ecds','Change Electronic Credit Distribution Parameters','ViewCreditDistributionParameters',1,0,'2023-02-02 04:19:38'),
('ChangeCtrlParameters','Control Connector','hxc.connectors.ctrl','Change Control Connector Parameters','ViewCtrlParameters',1,0,'2023-02-02 04:19:38'),
('ChangeDiagnosticParameters','Diagnostic','hxc.connectors.diagnostic','Change Diagnostic Parameters','ViewDiagnosticParameters',1,0,'2023-02-02 04:19:38'),
('ChangeEcdsApiConnectorParameters','Cai','hxc.connectors.ecdsapi','Change Cai Parameters','ViewEcdsApiConnectorParameters',1,0,'2023-02-02 04:19:38'),
('ChangeFileParameters','File','hxc.connectors.file','Change File Parameters','ViewFileParameters',1,0,'2023-02-02 04:19:38'),
('ChangeHmxNotifications','Hmx','hxc.connectors.hmx','Change Hmx Connector Notifications','ViewHmxNotifications',1,0,'2023-02-02 04:19:38'),
('ChangeHmxParameters','Hmx','hxc.connectors.hmx','Change Hmx Connector Parameters','ViewHmxParameters',1,0,'2023-02-02 04:19:38'),
('ChangeHuxNotifications','HuX','hxc.connectors.hux','Change HuX Connector Notifications','ViewHuxNotifications',1,0,'2023-02-02 04:19:38'),
('ChangeHuxParameters','HuX','hxc.connectors.hux','Change HuX Connector Parameters','ViewHuxParameters',1,0,'2023-02-02 04:19:38'),
('ChangeKerberosNotifications','Kerberos','hxc.connectors.kerberos','Change Kerberos Connector Notifications','ViewKerberosNotifications',1,0,'2023-02-02 04:19:38'),
('ChangeKerberosParameters','Kerberos','hxc.connectors.kerberos','Change Kerberos Connector Parameters','ViewKerberosParameters',1,0,'2023-02-02 04:19:38'),
('ChangeLifecycleParameters','Lifecycle','hxc.connectors.lifecycle','Change Lifecycle Parameters','ViewLifecycleParameters',1,0,'2023-02-02 04:19:38'),
('ChangeLocale','Locale','hxc.servicebus','Change Locale Parameters','ViewLocale',1,0,'2023-02-02 04:19:38'),
('ChangeLoggerParameters','Logger','hxc.services.logging','Change Logger Parameters','ViewLoggerParameters',1,0,'2023-02-02 04:19:38'),
('ChangeMySqlParameters','MySql','hxc.connectors.database.mysql','Change MySql Parameters','ViewMySqlParameters',1,0,'2023-02-02 03:57:55'),
('ChangeNumberPlanChangeParameters','NumberPlanChange','hxc.services.numberplan','Change Number Plan Change Parameters','ViewNumberPlanChangeParameters',3,0,'2023-02-02 04:19:38'),
('ChangePccBundleParameters','PCC Bundle','hxc.connectors.pcc_bundles','Change PCC Bundle Parameters','ViewPccBundleParameters',3,0,'2023-02-02 04:19:38'),
('ChangePCCSuTParameters','PCC Test Connector','hxc.connectors.sut','Change PCC Test Connector Parameters','ViewPCCSuTParameters',1,0,'2023-02-02 04:19:38'),
('ChangePermissions','User Permissions','hxc.services.security','Change Permissions','ViewPermissions',3,0,'2023-02-02 04:19:38'),
('ChangeReportingService','ReportingService','hxc.services.reporting','Change Reporting Service','ViewReportingService',1,0,'2023-02-02 04:19:38'),
('ChangeRoles','User Roles','hxc.services.security','Change Roles','ViewRoles',3,0,'2023-02-02 04:19:38'),
('ChangeSmppNotifications','SMPP','hxc.connectors.smpp','Change SMPP Connector Notifications','ViewSmppNotifications',1,0,'2023-02-02 04:19:38'),
('ChangeSmppParameters','SMPP','hxc.connectors.smpp','Change SMPP Connector Parameters','ViewSmppParameters',1,0,'2023-02-02 04:19:38'),
('ChangeSMSC','SMSC','hxc.connectors.smpp','Change SMSC Parameters','ViewSMSC',1,0,'2023-02-02 04:19:38'),
('ChangeSmtpParameters','SMTP','hxc.connectors.smtp','Change SMTP Parameters','ViewSmtpParameters',1,0,'2023-02-02 04:19:38'),
('ChangeSmtpServerParameters','SMTP','hxc.connectors.smtp','Change SMTP Parameters','ViewSmtpServerParameters',1,0,'2023-02-02 04:19:38'),
('ChangeSnmpParameters','SNMP','hxc.connectors.snmp','Change SNMP Parameters','ViewSnmpParameters',1,0,'2023-02-02 04:19:38'),
('ChangeSoapParameters','SOAP','hxc.connectors.soap','Change SOAP Parameters','ViewSoapParameters',1,0,'2023-02-02 04:19:38'),
('ChangeSoapTuning','SOAP Connector','hxc.connectors.soap','Change Tuning Parameters','ViewSoapTuning',1,0,'2023-02-02 04:19:38'),
('ChangeTuning','Service Bus','hxc.servicebus','Change Tuning Parameters','ViewTuning',1,0,'2023-02-02 04:19:38'),
('ChangeUIConnectorParameters','UI Connector','hxc.connectors.ui','Change UI Connector Parameters','ViewUIConnectorParameters',1,0,'2023-02-02 04:19:38'),
('ChangeUsers','User Configuration','hxc.services.security','Change Users','ViewUsers',3,0,'2023-02-02 04:18:06'),
('PerformArchivingParameters','Archiving','hxc.connectors.archiving','Perform Archiving Connector Calls','',1,0,'2023-02-02 04:19:38'),
('PerformDiagnosticMigration','Diagnostic','hxc.connectors.diagnostic','Perform NPC Migration','',1,0,'2023-02-02 04:19:38'),
('PerformLogfileRotate','Logger','hxc.services.logging','Perform Rotation of Logfile','',1,0,'2023-02-02 04:19:38'),
('PerformMigration','Lifecycle','hxc.connectors.lifecycle','Perform NPC Migration','',1,0,'2023-02-02 04:19:38'),
('ViewAirParameters','Air','hxc.connectors.air','View Air Parameters','',1,0,'2023-02-02 03:57:55'),
('ViewAirSimParameters','Air Simulator','hxc.services.airsim','View Air Simulator Parameters','',1,0,'2023-02-02 03:57:27'),
('ViewArchivingDestination','ArchivingDestination','hxc.connectors.archiving','View Archiving Destination Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewArchivingParameters','Archiving','hxc.connectors.archiving','View Archiving Connector Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewC4USutParameters','C4U System Under Test','hxc.connectors.sut','View Air Simulator Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewCaiParameters','Cai','hxc.connectors.cai','View Cai Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewCdrParameters','CDR','hxc.services.transactions','View CDR Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewCreditDistributionParameters','Financial Services','hxc.services.ecds','View Electronic Credit Distribution','',1,0,'2023-02-02 04:00:00'),
('ViewCtrlNotifications','Control Connector','hxc.connectors.ctrl','View Control Connector Notifications','',1,0,'2023-02-02 04:19:38'),
('ViewCtrlParameters','Control Connector','hxc.connectors.ctrl','View Control Connector Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewDiagnosticParameters','Diagnostic','hxc.connectors.diagnostic','View Diagnostic Parameters','',1,0,'2023-02-02 03:58:28'),
('ViewEcdsApiConnectorParameters','Cai','hxc.connectors.ecdsapi','View Cai Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewFileParameters','File','hxc.connectors.file','View File Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewHmxNotifications','Hmx','hxc.connectors.hmx','View Hmx Connector Notifications','',1,0,'2023-02-02 04:19:38'),
('ViewHmxParameters','Hmx','hxc.connectors.hmx','View Hmx Connector Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewHuxNotifications','HuX','hxc.connectors.hux','View HuX Connector Notifications','',1,0,'2023-02-02 04:19:38'),
('ViewHuxParameters','HuX','hxc.connectors.hux','View HuX Connector Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewKerberosNotifications','Kerberos','hxc.connectors.kerberos','View Kerberos Connector Notifications','',1,0,'2023-02-02 04:19:38'),
('ViewKerberosParameters','Kerberos','hxc.connectors.kerberos','View Kerberos Connector Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewLifecycleParameters','Lifecycle','hxc.connectors.lifecycle','View Lifecycle Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewLocale','Locale','hxc.servicebus','View Locale Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewLoggerParameters','Logger','hxc.services.logging','View Logger Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewMySqlParameters','MySql','hxc.connectors.database.mysql','View MySql Parameters','',1,0,'2023-02-02 03:57:55'),
('ViewNumberPlanChangeParameters','NumberPlanChange','hxc.services.numberplan','View Number Plan Change Parameters','',3,0,'2023-02-02 04:19:38'),
('ViewPccBundleParameters','PCC Bundle','hxc.connectors.pcc_bundles','View PCC Bundle Parameters','',3,0,'2023-02-02 04:19:38'),
('ViewPCCSuTParameters','PCC Test Connector','hxc.connectors.sut','View PCC Test Connector Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewPermissions','User Permissions','hxc.services.security','View Permission','',3,0,'2023-02-02 04:19:38'),
('ViewReportingService','ReportingService','hxc.services.reporting','View Reporting Service','',1,0,'2023-02-02 04:19:38'),
('ViewRoles','User Roles','hxc.services.security','View Roles','',3,0,'2023-02-02 04:19:38'),
('ViewSmppNotifications','SMPP','hxc.connectors.smpp','View SMPP Connector Notifications','',1,0,'2023-02-02 04:19:38'),
('ViewSmppParameters','SMPP','hxc.connectors.smpp','View SMPP Connector Parameters','',1,0,'2023-02-02 03:57:55'),
('ViewSMSC','SMSC','hxc.connectors.smpp','View SMSC Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewSmtpParameters','SMTP','hxc.connectors.smtp','View SMTP Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewSmtpServerParameters','SMTP','hxc.connectors.smtp','View SMTP Server Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewSnmpParameters','SNMP','hxc.connectors.snmp','View SNMP Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewSoapParameters','SOAP','hxc.connectors.soap','View SOAP Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewSoapTuning','SOAP Connector','hxc.connectors.soap','View Tuning Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewTuning','Service Bus','hxc.servicebus','View Tuning Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewUIConnectorParameters','UI Connector','hxc.connectors.ui','View UI Connector Parameters','',1,0,'2023-02-02 04:19:38'),
('ViewUsers','User Configuration','hxc.services.security','View Users','',3,0,'2023-02-02 04:18:06');
/*!40000 ALTER TABLE `se_perm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `se_user`
--

DROP TABLE IF EXISTS `se_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `se_user` (
  `userId` varchar(15) NOT NULL,
  `name` varchar(60) NOT NULL,
  `mobileNumber` varchar(15) DEFAULT NULL,
  `password` varbinary(50) NOT NULL,
  `publicKey` varbinary(50) NOT NULL,
  `builtIn` bit(1) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `roles` bigint(20) NOT NULL DEFAULT 3,
  `control` int(11) NOT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `se_user`
--

LOCK TABLES `se_user` WRITE;
/*!40000 ALTER TABLE `se_user` DISABLE KEYS */;
INSERT INTO `se_user` VALUES
('Supplier','Supplier','+27738175876','êèÖLÜqF“hC=z¯{5—Q','/e>}œŽE¨nì³*ŠDÐC','','',1,0,'2023-02-02 04:18:06');
/*!40000 ALTER TABLE `se_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-02-28 11:47:33
