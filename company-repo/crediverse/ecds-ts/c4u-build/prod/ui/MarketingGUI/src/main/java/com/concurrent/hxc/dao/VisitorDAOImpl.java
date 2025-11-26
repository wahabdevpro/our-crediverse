package com.concurrent.hxc.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class VisitorDAOImpl implements VisitorDAO
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
	public VisitorDAOImpl()
	{
	}

	public VisitorDAOImpl(SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// VisitorDAO Implementation
	//
	// /////////////////////////////////
	@Override
	@Transactional
	public Visitor getVisitor(String ip)
	{
		// Create the statement
		String hqlClause = String.format("from Visitor where ip = '%s'", ip);

		// Create the query
		Query query = sessionFactory.getCurrentSession().createQuery(hqlClause);

		Visitor visitor = null;

		try
		{
			// Get the first visitor from the database
			@SuppressWarnings("unchecked")
			List<Visitor> visitors = (List<Visitor>) query.list();
			if (visitors != null && !visitors.isEmpty())
				visitor = visitors.get(0);
		}
		catch (Exception e)
		{

		}

		// Return the visitor
		return visitor;
	}

	// Inserts the visitor in the database
	@Override
	@Transactional
	public void upsert(Visitor visitor)
	{
		sessionFactory.getCurrentSession().saveOrUpdate(visitor);
	}

	// Deletes the visitor from the database
	@Override
	@Transactional
	public void delete(String ip)
	{
		// Create the visitor to delete
		Visitor visitor = new Visitor();
		
		// Fill the primary key
		visitor.setIp(ip);
		
		// Delete the visitor from the database
		sessionFactory.getCurrentSession().delete(visitor);
	}

	// Increments the number of visits
	@Override
	@Transactional
	public void visit(String ip)
	{
		// Get the visitor
		Visitor visitor = getVisitor(ip);
		
		// Ensure the visitor exists
		if (visitor == null)
		{
			return;
		}

		// Set the number of visits
		visitor.setVisits(visitor.getVisits() + 1);
		
		// Update the visitor
		upsert(visitor);
	}

}
