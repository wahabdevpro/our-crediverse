package hxc.services.ecds.util;

import java.io.Closeable;

import javax.persistence.EntityManager;

public class RequiresTransaction implements Closeable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private EntityManager em;
	private boolean mustCommit = false;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public RequiresTransaction(EntityManager em)
	{
		this.em = em;
		if (!em.isJoinedToTransaction())
		{
			em.getTransaction().begin();
			mustCommit = true;
		}
	}

	public void commit()
	{
		if (mustCommit)
		{
			em.getTransaction().commit();
			mustCommit = false;
		}
	}

	private void flush()
	{
		if (mustCommit)
		{
			em.flush();
			mustCommit = false;
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Closable
	//
	// /////////////////////////////////
	@Override
	public void close()
	{
		if (mustCommit && em.isJoinedToTransaction())
			em.getTransaction().rollback();
	}

}
