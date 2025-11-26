package hxc.connectors.soap;

import hxc.servicebus.ReturnCodes;

public class FreReturnCodeTexts
{
	public static String getReturnCodeText(ReturnCodes returnCode)
	{
		switch (returnCode)
		{
			case alreadyAdded:
				return "Déja ajouté";

			case alreadyMember:
				return "Déja membre";

			case alreadyOtherMember:
				return "Déja membre autre";

			case alreadyOwner:
				return "Déja propriétaire";

			case cannotBeAdded:
				return "L'offre ne peux pas etre ajoutée";

			case insufficientBalance:
				return "Crédit insuffisant";

			case invalidQuota:
				return "Quota non valable";

			case maxMembersExceeded:
				return "nombre maximum de membres depassé";

			case notMember:
				return "pas membre";

			case quotaNotSet:
				return "Quota non défini";

			case timedOut:
				return "Expiré";

			case alreadySubscribed:
				return "Vous etes déjà abonné";

			case authorizationFailure:
				return "Utilisateur/Mot de passe fournis non autorisés";

			case cannotAddSelf:
				return "ne peut s'ajouter soit meme";

			case cannotCallSelf:
				return "ne peut s'appeller soit meme";

			case cannotMigrateToSameVariant:
				return "ne peut changer pour le meme variant";

			case cannotReceiveCredit:
				return "ne peut recevoir d'unités";

			case cannotTransferToSelf:
				return "ne peut transférer vers soit meme";

			case excessiveBalance:
				return "Crédit excessif";

			case inactiveAParty:
				return "Partie A inactive";

			case inactiveBParty:
				return "Partie B inactive";

			case incomplete:
				return "Requete incomplete";

			case invalidArguments:
				return "Paramètres fournis invalides";

			case invalidNumber:
				return "Numéro fourni invalide";

			case invalidPin:
				return "Pin non valable";

			case invalidService:
				return "Service non valable";

			case invalidVariant:
				return "Variant Invalide";

			case malformedRequest:
				return "Requete malformée";

			case notEligible:
				return "Vous n'etes pas élligible au service";

			case notSubscribed:
				return "Vous n'etes pas abonné au service";

			case notSupported:
				return "Opération non supportée pour ce service";

			case quantityTooBig:
				return "Quantité trop grande";

			case quantityTooSmall:
				return "Quantité trop petite";

			case quotaReached:
				return "Quota atteint";

			case serviceBusy:
				return "Service Occupé";

			case success:
				return "Success";

			case successfulTest:
				return "Test Réussi";

			case suspended:
				return "Suspendu";

			case technicalProblem:
				return "Un problème technique est apparu. Veuillez réessayer plus tard";

			case temporaryBlocked:
				return "Le Service n'est disponible pour l'instant";

			case memberNotEligible:
				return "Le membre fourni n'etes pas élligible au service";

			case pinBlocked:
				return "FR: Pin Blocked";

			case unregisteredPin:
				return "FR: Unregistered Pin";

			case cannotBeSuspended:
				return "FR: The Service cannot be Suspended";

			case cannotBeResumed:
				return "FR: The Service cannot be Resumed";

			default:
				return null;
		}
	}
}
