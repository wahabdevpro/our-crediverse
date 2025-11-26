package com.concurrent.hxc.dao;

public interface VisitorDAO
{

	// Gets the visitor based on IP
	public Visitor getVisitor(String ip);
	
	// Inserts the visitor into the database
	public void upsert(Visitor visitor);
	
	// Deletes the visitor
	public void delete(String ip);
	
	// Increments the number of visits
	public void visit(String ip);
	
}
