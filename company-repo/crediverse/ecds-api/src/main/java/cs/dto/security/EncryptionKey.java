package cs.dto.security;

import java.security.Key;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EncryptionKey
{
	private String algorithm;
	private String encodedKey;
	private String encodingFormat;
	private String modulus;
	private String exponent;

	/*
	 * Ignore rawKey in serialized json as it cannot be easily serialized by jackson
	 */
	@JsonIgnore
	private Key rawKey;
}
