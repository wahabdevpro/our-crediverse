package hxc.services.ecds.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Table(name = "mobile_numbers_format_mapping", indexes = @Index(name = "old_code_UNIQUE", columnList = "old_code"))
@NamedQueries({
		@NamedQuery(name = "MobileNumberFormatMapping.findAll", query = "SELECT p FROM MobileNumberFormatMapping p"),
		@NamedQuery(name = "MobileNumberFormatMapping.deleteAll", query = "DELETE FROM MobileNumberFormatMapping p"),
})
@Entity
public class MobileNumberFormatMapping implements Serializable {
	private String oldCode;
	private String newPrefix;

	public MobileNumberFormatMapping() {
	}

	public MobileNumberFormatMapping(String oldCode, String newPrefix) {
		this.oldCode = oldCode;
		this.newPrefix = newPrefix;
	}

	@Id
	@Column(name = "old_code", nullable = false)
	public String getOldCode() {
		return oldCode;
	}

	public void setOldCode(String oldCode) {
		this.oldCode = oldCode;
	}

	@Column(name = "new_prefix", nullable = false)
	public String getNewPrefix() {
		return newPrefix;
	}

	public void setNewPrefix(String newPrefix) {
		this.newPrefix = newPrefix;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MobileNumberFormatMapping)) {
			return false;
		}
		MobileNumberFormatMapping that = (MobileNumberFormatMapping) o;
		return oldCode.equals(that.oldCode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(oldCode);
	}
}
