package hxc.utils.asn1.generator;

public enum ASN1Type
{
	// Simple Types
	BOOLEAN, INTEGER, BIT_STRING, OCTET_STRING, NULL, OBJECT_IDENTIFIER, REAL, ENUMERATED, CHARACTER_STRING,

	// Complex Types
	CHOICE, SEQUENCE_OF, SEQUENCE, SET_OF, SET, UTCTIME,

	// Character String Types
	IA5String, PrintableString, T61String, UTF8String,

	// Other Types
	Identifier, PositiveInteger,

	UNKNOWN
}
