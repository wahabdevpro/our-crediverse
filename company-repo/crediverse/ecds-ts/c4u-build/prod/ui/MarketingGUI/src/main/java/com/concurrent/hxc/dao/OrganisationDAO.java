package com.concurrent.hxc.dao;

public interface OrganisationDAO
{
	// Gets the organisation from the email
	public Organisation getOrganisation(String email);
	
	// Inserts the organisation
	public void upsert(Organisation organisation);
	
	// Deletes the organisation from the email
	public void delete(String email);
	
	// Checks if the number range exists in the database
	public boolean hasNumberRange(String numberRange);
}
