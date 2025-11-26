package hxc.services.ecds.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Metamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;

//import org.codehaus.jackson.annotate.JsonIgnore;
import hxc.connectors.hlr.IHlrInformation;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.rest.batch.IBatchEnabled;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

////////////////////////////////////////////////////////////////////////////////////////
//
// Cell Table - Used for Agent Segmentation
//
///////////////////////////////////

@Table(name = "el_cell", uniqueConstraints = { //
		@UniqueConstraint(name = "el_cell_cid", columnNames = { "company_id", "cell_id", "lac", "mnc", "mcc" }) })
@Entity
@NamedQueries({ //
	@NamedQuery(name = "Cell.findByID", query = "SELECT p FROM Cell p where id = :id and companyID = :companyID"), //
	@NamedQuery(name = "Cell.find", query = "SELECT p FROM Cell p where mobileCountryCode = :mcc and mobileNetworkCode = :mnc and localAreaCode = :lac and cellID = :cid and companyID = :companyID"), //
	@NamedQuery(name = "Cell.referenceCellGroup", query = "SELECT p FROM Cell p where cell_group_id = :cellGroupID"), //
})
public class Cell extends hxc.ecds.protocol.rest.Cell implements Serializable, ICompanyData<Cell>, //
		IBatchEnabled<Cell>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 6399446402028599195L;

	public static final Permission MAY_ADD = new Permission(false, false, Permission.GROUP_CELLS, Permission.PERM_ADD, "May Add Cells");
	public static final Permission MAY_UPDATE = new Permission(false, false, Permission.GROUP_CELLS, Permission.PERM_UPDATE, "May Update Cells");
	public static final Permission MAY_DELETE = new Permission(false, false, Permission.GROUP_CELLS, Permission.PERM_DELETE, "May Delete Cells");
	public static final Permission MAY_VIEW = new Permission(false, false, Permission.GROUP_CELLS, Permission.PERM_VIEW, "May View Cells");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Additional Fields
	//
	// /////////////////////////////////
	@JsonIgnore
	protected int lastUserID;
	@JsonIgnore
	protected Date lastTime;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getId()
	{
		return id;
	}

	@Override
	public Cell setId(int id)
	{
		this.id = id;
		return this;
	}

	@Override
	@Column(name = "company_id", nullable = false)
	public int getCompanyID()
	{
		return companyID;
	}

	@Override
	public Cell setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	@Column(name = "mcc", nullable = false)
	public int getMobileCountryCode()
	{
		return mobileCountryCode;
	}

	@Override
	public Cell setMobileCountryCode(int mobileCountryCode)
	{
		this.mobileCountryCode = mobileCountryCode;
		return this;
	}

	@Override
	@Column(name = "mnc", nullable = false)
	public int getMobileNetworkCode()
	{
		return mobileNetworkCode;
	}

	@Override
	public Cell setMobileNetworkCode(int mobileNetworkCode)
	{
		this.mobileNetworkCode = mobileNetworkCode;
		return this;
	}

	@Override
	@Column(name = "lac", nullable = false)
	public int getLocalAreaCode()
	{
		return localAreaCode;
	}

	@Override
	public Cell setLocalAreaCode(int localAreaCode)
	{
		this.localAreaCode = localAreaCode;
		return this;
	}

	@Override
	@Column(name = "cell_id", nullable = false)
	public int getCellID()
	{
		return cellID;
	}

	@Override
	public Cell setCellID(int cellID)
	{
		this.cellID = cellID;
		return this;
	}

	@Override
	@Column(name = "lat", nullable = true)
	public Double getLatitude()
	{
		return latitude;
	}

	@Override
	public Cell setLatitude(Double latitude)
	{
		this.latitude = latitude;
		return this;
	}

	@Override
	@Column(name = "lng", nullable = true)
	public Double getLongitude()
	{
		return longitude;
	}

	@Override
	public Cell setLongitude(Double longitude)
	{
		this.longitude = longitude;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public Cell setLastUserID(int lastUserID)
	{
		this.lastUserID = lastUserID;
		return this;
	}

	@Override
	@Version
	public int getVersion()
	{
		return version;
	}

	@Override
	public Cell setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Override
	@Column(name = "lm_time", nullable = false)
	public Date getLastTime()
	{
		return lastTime;
	}

	@Override
	public Cell setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@Override
	@ManyToMany(cascade = CascadeType.DETACH)
	@JoinTable(name = "el_area_cell", joinColumns = { @JoinColumn(name = "cell_id") }, inverseJoinColumns = { @JoinColumn(name = "area_id") })
	@SuppressWarnings({ "unchecked" })
	@LazyCollection(LazyCollectionOption.FALSE)
	public List<Area> getAreas()
	{
		return (List<Area>) areas;
	}

	@Override
	public Cell setAreas(List<? extends hxc.ecds.protocol.rest.Area> areas)
	{
		super.setAreas(areas);
		return this;
	}

	@Override
	@ManyToMany(cascade = CascadeType.DETACH)
	@JoinTable(name = "el_cell_cell_group", joinColumns = { @JoinColumn(name = "cell_id") }, inverseJoinColumns = { @JoinColumn(name = "cell_group_id") })
	@SuppressWarnings({ "unchecked" })
	@LazyCollection(LazyCollectionOption.FALSE)
	public List<CellGroup> getCellGroups()
	{
		return (List<CellGroup>) cellGroups;
	}

	@Override
	public Cell setCellGroups(List<? extends hxc.ecds.protocol.rest.CellGroup> cellGroups)
	{
		super.setCellGroups(cellGroups);
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public Cell()
	{

	}

	public Cell(EntityManager em, Cell cell)
	{
		this.lastUserID = cell.lastUserID;
		this.lastTime = cell.lastTime;
		amend(em, cell);
	}

	@SuppressWarnings({ "unchecked" })
	public void amend(EntityManager em, hxc.ecds.protocol.rest.Cell cell)
	{
		this.id = cell.getId();
		this.companyID = cell.getCompanyID();
		this.version = cell.getVersion();
		this.mobileCountryCode = cell.getMobileCountryCode();
		this.mobileNetworkCode = cell.getMobileNetworkCode();
		this.localAreaCode = cell.getLocalAreaCode();
		this.cellID = cell.getCellID();
		this.latitude = cell.getLatitude();
		this.longitude = cell.getLongitude();

		// Add new Areas
		List<hxc.ecds.protocol.rest.Area> newAreas = (List<hxc.ecds.protocol.rest.Area>) cell.getAreas();
		List<Area> existingAreas = getAreas();
		if (newAreas != null)
		{
			for (hxc.ecds.protocol.rest.Area newArea : newAreas)
			{
				if (!contains(existingAreas, newArea))
				{
					existingAreas.add(Area.findByID(em, newArea.getId(), newArea.getCompanyID()));
				}
			}

			// Remove unused Areas
			int index = 0;
			while (index < existingAreas.size())
			{
				if (!contains(newAreas, existingAreas.get(index)))
					existingAreas.remove(index);
				else
					index++;
			}

		}

		// Add new CellGroups
		List<hxc.ecds.protocol.rest.CellGroup> newCellGroups = (List<hxc.ecds.protocol.rest.CellGroup>) cell.getCellGroups();
		List<CellGroup> existingCellGroups = getCellGroups();
		if (newCellGroups != null)
		{
			for (hxc.ecds.protocol.rest.CellGroup newCellGroup : newCellGroups)
			{
				if (!contains(existingCellGroups, newCellGroup))
				{
					existingCellGroups.add(CellGroup.findByID(em, newCellGroup.getId(), newCellGroup.getCompanyID()));
				}
			}

			// Remove unused CellGroups
			int index = 0;
			while (index < existingCellGroups.size())
			{
				if (!contains(newCellGroups, existingCellGroups.get(index)))
					existingCellGroups.remove(index);
				else
					index++;
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public static Cell findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<Cell> query = em.createNamedQuery("Cell.findByID", Cell.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<Cell> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static Cell find(EntityManager em, int mcc, int mnc, int lac, int cid, int companyID)
	{
		TypedQuery<Cell> query = em.createNamedQuery("Cell.find", Cell.class);
		query.setParameter("mcc", mcc);
		query.setParameter("mnc", mnc);
		query.setParameter("lac", lac);
		query.setParameter("cid", cid);
		query.setParameter("companyID", companyID);
		List<Cell> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}
	
	/* Custom Cell query functionality as opposed to using QueryBuilder.  The many to many relationship between cells 
	 * and areas makes extending the QueryBuilder in a generic fashion a bit complicated.  Purpose built functionality 
	 * has been implemented to search for cell criteria.
	 * Aim was to create one function for data and count.  SQL_CALC_FOUND_ROWS doesn't work in this case due to Hibernate
	 * firing off additional queries. 
	 * TODO: Implement Geospatial searching for Latitude and Longitude.
	 */
	private static <T, V> TypedQuery<T> getCellQuery(EntityManager em, RestParams params, int companyID, 
			Class<V> entityClass, Class<T> returnClass, boolean isCountMode) throws Exception
	{		
		if(isCountMode && returnClass != Long.class)
		{
			throw new Exception("getCellQuery called with incompatible arguments, isCountMode=[true]; returnClass=[" + returnClass + "]" ); 
		}
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<?> cq = null;
		Root<V> root = null;
		if(isCountMode)
		{
			//T is Long.class
			CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
			cq = criteria;
			root = cq.from(entityClass);
			//cb.count(root) demands that criteria be Long and not generic T.
			//Thus a warning is generated when the TypedQuery is created.
			criteria.select(cb.count(root));
		} else {
			//T is Cell.class or some other supported entity
			CriteriaQuery<T> criteria = cb.createQuery(returnClass);
			criteria.distinct(true);
			root = criteria.from(entityClass);
			cq = criteria;			
		}
		Metamodel m = em.getMetamodel();
		ArrayList<Predicate> predicates = new ArrayList<Predicate>();
		Map<String, List<String> > filterMap = QueryBuilder.getFilterMap(Cell.class, params.getFilter(), companyID);
		Set<Integer> areas = new TreeSet<Integer>(); //areas specified in Params
		Set<Integer> cellGroups = new TreeSet<Integer>(); //cellGroups specified in Params
		Map<Integer, Area> childAreas = new HashMap<Integer, Area>(); //recursive child areas of supplied areas.
		boolean hasAreas = false;
		boolean hasCellGroups = false;
		boolean hasRecursive = false;
		for(String fieldName : filterMap.keySet())
		{
			List<String> attributes = filterMap.get(fieldName);
			String operator = attributes.get(0);
			String value = attributes.get(1);
			if(fieldName.equals("areaID"))
			{			
				hasAreas = true;
				try(Scanner scanner = new Scanner(value))
				{
					scanner.useDelimiter(",");
					while (scanner.hasNextInt()) {
					    areas.add(scanner.nextInt());
					}
				} 
			} else if(fieldName.equals("cellGroupID"))
			{			
				hasCellGroups = true;
				try(Scanner scanner = new Scanner(value))
				{
					scanner.useDelimiter(",");
					while (scanner.hasNextInt()) {
					    cellGroups.add(scanner.nextInt());
					}
				} 
			} else if(fieldName.equals("recursive")) 
			{				
				//Find child areas of the areas supplied in Areas above.
				childAreas = Area.findChildren(em, areas, companyID);
				hasRecursive = true;
			} else {
				switch(operator)
				{
				case ":": //like
					Path<String> path = root.get(fieldName);
					predicates.add(cb.like(path, cb.parameter(String.class, value)));
					break;
				case "=":
					predicates.add(cb.equal(root.get(fieldName), value));
					break;
				case "#":
				case "!=":
					predicates.add(cb.notEqual(root.get(fieldName), value));
					break;
				//Other boolean operators not implemented - not applicable to current cell attributes.
				default:
					break;
				}
			}
		}
		//Add area predicates to create a consolidated list of predicates if area search criteria has been supplied
		if(hasAreas)
		{
			if(hasRecursive)
			{	
				areas.addAll(childAreas.keySet());
			} 
			//Many to Many join with Areas
			predicates.add(root.join(m.entity(entityClass).getList("areas")).in(areas));
		}
		if(hasCellGroups)
		{
			//Many to Many join with CellGroups
			predicates.add(root.join(m.entity(entityClass).getList("cellGroups")).in(cellGroups));
		}
		//Add the list of consolidated predicates (areas, mcc, mnc, lac, ci, etc) to the query criteria
		cq.where( predicates.toArray(new Predicate[predicates.size()]));
		//Sort Logic
		String sortString = params.getSort();
		if(!isCountMode && sortString != null && !sortString.isEmpty())
			cq.orderBy(getSortOrderList(cb, root, sortString));
		//Create Query.
		//Criteria type is either Long.class or returnClass;  if Long.class then T == Long.
		@SuppressWarnings("unchecked")
		TypedQuery<T> query = (TypedQuery<T>) em.createQuery(cq);
		return query;
	}
	
	public static List<Cell> findAll(EntityManager em, RestParams params, int companyID) throws Exception
	{				
		if (params.getFirst() == 0 && params.getMax() == 0) return new ArrayList<Cell>();

		TypedQuery<Cell> query = getCellQuery(em, params, companyID, Cell.class, Cell.class, false);
		if(params.getMax() > 0)
			query.setMaxResults(params.getMax());
		if (params.getFirst() > 0)
			query.setFirstResult(params.getFirst());
		List<Cell> cells = query.getResultList();		
		return cells;
	}
	
	public static Long findAllCount(EntityManager em, RestParams params, int companyID) throws Exception
	{				
		TypedQuery<Long> query = getCellQuery(em, params, companyID, Cell.class, Long.class, true);
		Long count = query.getSingleResult();		
		return count;
	}

	public static Long findCount(EntityManagerEx em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Cell.class, params, companyID);
		return query.getSingleResult();
	}	

	public static void loadMRD(EntityManager em, int companyID, Session session) throws RuleCheckException
	{
		Permission.loadMRD(em, MAY_ADD, session);
		Permission.loadMRD(em, MAY_UPDATE, session);
		Permission.loadMRD(em, MAY_DELETE, session);
		Permission.loadMRD(em, MAY_VIEW, session);
	}

	@Override
	public void persist(EntityManager em, Cell existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		QueryBuilder.persist(em, existing, this, session, AuditEntry.TYPE_CELL, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		QueryBuilder.remove(em, this, session, AuditEntry.TYPE_CELL, auditEntryContext);
	}
	
	public static boolean referencesCellGroup(EntityManager em, int cellGroupID)
	{
		TypedQuery<Cell> query = em.createNamedQuery("Cell.referenceCellGroup", Cell.class);
		query.setParameter("cellGroupID", cellGroupID);
		query.setMaxResults(1);
		List<Cell> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	@Override
	public void validate(Cell previous) throws RuleCheckException
	{
		RuleCheck.validate(this);

		if (previous != null)
		{
			RuleCheck.noChange("id", id, previous.id);
			RuleCheck.noChange("companyID", companyID, previous.companyID);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private boolean contains(List<? extends hxc.ecds.protocol.rest.Area> areas, hxc.ecds.protocol.rest.Area area)
	{
		for (hxc.ecds.protocol.rest.Area perm : areas)
		{
			if (perm.getId() == area.getId())
				return true;
		}
		return false;
	}

	private boolean contains(List<? extends hxc.ecds.protocol.rest.CellGroup> cellGroups, hxc.ecds.protocol.rest.CellGroup cellGroup)
	{
		for (hxc.ecds.protocol.rest.CellGroup item : cellGroups)
		{
			if (item.getId() == cellGroup.getId())
				return true;
		}
		return false;
	}

	public boolean containedWithin(Area area)
	{
		if (area == null)
			return false;

		for (Area testArea : this.getAreas())
		{
			Area parent = testArea;
			while (parent != null)
			{
				if (parent.getId() == area.getId())
					return true;
				parent = parent.getParentArea();
			}
		}

		return false;

	}

	public static boolean isEmpty(IHlrInformation location)
	{
		return location == null //
				|| location.getMobileCountryCode() == null //
				|| location.getMobileNetworkCode() == null //
				|| location.getLocationAreaCode() == null //
				|| location.getCellIdentity() == null;
	}
	
	private static <T> List<Order> getSortOrderList(CriteriaBuilder cb, Root<T> root, String sortString)
	{
		List<Order> orderList = new ArrayList<Order>();		
		int index;
		while(sortString.length() > 0)
		{		
			index = sortString.indexOf("+", 0) > 0 ? sortString.indexOf("+", 0) : sortString.indexOf("-", 0);
			int mode = sortString.charAt(index) == '+' ? 1 : -1;			
			String column = sortString.substring(0, index).substring(sortString.indexOf(".") + 1);
			sortString = sortString.substring(index + 1);
			if(mode > 0)
				orderList.add(cb.asc(root.get(column)));
			else 
				orderList.add(cb.desc(root.get(column)));
		}
		return orderList;
	}
}
