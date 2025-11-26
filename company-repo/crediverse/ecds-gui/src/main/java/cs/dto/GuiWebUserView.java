package cs.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hxc.ecds.protocol.rest.Role;

public interface GuiWebUserView
{
	@JsonIgnore List<? extends Role> getRoles();
	@JsonIgnore int getVersion();
	@JsonIgnore byte[] getKey1();
}
