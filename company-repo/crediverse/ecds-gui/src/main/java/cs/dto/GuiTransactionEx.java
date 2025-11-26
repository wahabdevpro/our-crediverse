package cs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import hxc.ecds.protocol.rest.TransactionEx;

@JsonInclude(Include.ALWAYS)
public class GuiTransactionEx extends TransactionEx
{

}
