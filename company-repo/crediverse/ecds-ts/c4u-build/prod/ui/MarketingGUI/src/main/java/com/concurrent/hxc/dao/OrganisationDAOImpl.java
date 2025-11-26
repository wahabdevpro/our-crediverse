package com.concurrent.hxc.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class OrganisationDAOImpl implements OrganisationDAO
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Autowired
	private SessionFactory sessionFactory;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	public OrganisationDAOImpl()
	{
	}

	public OrganisationDAOImpl(SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// OrganisationDAO Implementation
	//
	// /////////////////////////////////
	
	@Override
	@Transactional
	public Organisation getOrganisation(String email)
	{
		// Create the statement
		String hqlClause = String.format("from Organisation where email = '%s'", email);

		// Create the query
		Query query = sessionFactory.getCurrentSession().createQuery(hqlClause);

		Organisation org = null;

		try
		{
			// Do a list and get the first record
			@SuppressWarnings("unchecked")
			List<Organisation> orgs = (List<Organisation>) query.list();
			if (orgs != null && !orgs.isEmpty())
				org = orgs.get(0);
		}
		catch (Exception e)
		{

		}

		// Return the organisation
		return org;
	}

	// Inserts the record
	@Override
	@Transactional
	public void upsert(Organisation org)
	{
		sessionFactory.getCurrentSession().saveOrUpdate(org);
	}

	// Deletes the organisation record
	@Override
	@Transactional
	public void delete(String email)
	{
		// Create an organisation to delete
		Organisation deleteOrg = new Organisation();
		
		// Fill in the primary key
		deleteOrg.setEmail(email);
		
		// Deletes the object from the database
		sessionFactory.getCurrentSession().delete(deleteOrg);
	}

	// Checks if the number range exists
	@Override
	@Transactional
	public boolean hasNumberRange(String numberRange)
	{
		// Create the statement
		String hqlClause = String.format("from Organisation where numberRange = '%s'", numberRange);

		// Create the query
		Query query = sessionFactory.getCurrentSession().createQuery(hqlClause);

		try
		{
			// Check if there is a record that exists
			@SuppressWarnings("unchecked")
			List<Organisation> orgs = (List<Organisation>) query.list();
			if (orgs != null && !orgs.isEmpty())
				return true;
		}
		catch (Exception e)
		{

		}

		// Return false if it doesn't exist
		return false;
	}

}
