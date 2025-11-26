package hxc.services.ecds.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Table(name = "mobile_numbers_format_config")
@NamedQueries({
		@NamedQuery(name = "MobileNumberFormatConfig.find", query = "SELECT p FROM MobileNumberFormatConfig p WHERE id = 1"),
})
@Entity
public class MobileNumberFormatConfig extends hxc.ecds.protocol.rest.MobileNumberFormatConfig implements Serializable {
	private Integer id = 1;

	@Id
	@Column(name = "id", nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "old_number_length", nullable = false)
	public Integer getOldNumberLength() {
		return oldNumberLength;
	}

	public void setOldNumberLength(Integer oldNumberLength) {
		this.oldNumberLength = oldNumberLength;
	}

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "phase", nullable = false)
	public hxc.ecds.protocol.rest.MobileNumberFormatConfig.Phase getPhase() {
		return phase;
	}

	public void setPhase(hxc.ecds.protocol.rest.MobileNumberFormatConfig.Phase phase) {
		this.phase = phase;
	}

	@Column(name = "wrong_b_number_message_en")
	public String getWrongBNumberMessageEn() {
		return wrongBNumberMessageEn;
	}

	public void setWrongBNumberMessageEn(String wrongBNumberMessageEn) {
		this.wrongBNumberMessageEn = wrongBNumberMessageEn;
	}

	@Column(name = "wrong_b_number_message_fr")
	public String getWrongBNumberMessageFr() {
		return wrongBNumberMessageFr;
	}

	public void setWrongBNumberMessageFr(String wrongBNumberMessageFr) {
		this.wrongBNumberMessageFr = wrongBNumberMessageFr;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MobileNumberFormatConfig)) {
			return false;
		}
		MobileNumberFormatConfig that = (MobileNumberFormatConfig) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
