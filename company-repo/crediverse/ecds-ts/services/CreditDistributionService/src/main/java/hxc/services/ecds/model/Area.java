package hxc.services.ecds.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.ConstructorResult;
import javax.persistence.ColumnResult;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.rest.batch.IBatchEnabled;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;

////////////////////////////////////////////////////////////////////////////////////////
//
// Area Table - Used for Security checks
//
///////////////////////////////////

@SqlResultSetMappings({
	@SqlResultSetMapping(name="AreaToParentMapping.findByIDin", classes = {
		@ConstructorResult(targetClass = Area.SimpleAreaMapping.class, 
			columns = {
				@ColumnResult(name="id", type=Integer.class),
				@ColumnResult(name="name", type=String.class),
				@ColumnResult(name="type", type=String.class),
				@ColumnResult(name="parent_id", type=Integer.class)
		})
	}),
	@SqlResultSetMapping(name="AreaToCellMapping.findByCellIDin", classes = {
		@ConstructorResult(targetClass = Area.SimpleAreaMapping.class, 
			columns = {
				@ColumnResult(name="cell_id", type=Integer.class),
				@ColumnResult(name="area_id", type=Integer.class),
				@ColumnResult(name="area_name", type=String.class),
				@ColumnResult(name="area_type", type=String.class),
				@ColumnResult(name="parent_id", type=Integer.class)
		})
	}),
	@SqlResultSetMapping(name="AreaToCellMapping.findAllSimple", classes = {
		@ConstructorResult(targetClass = Area.SimpleAreaMapping.class, 
			columns = {
				@ColumnResult(name="cell_id", type=Integer.class),
				@ColumnResult(name="area_id", type=Integer.class),
				@ColumnResult(name="area_name", type=String.class),
				@ColumnResult(name="area_type", type=String.class),
				@ColumnResult(name="parent_id", type=Integer.class)
		})
	})
})

@Table(name = "el_area", uniqueConstraints = { //
		@UniqueConstraint(name = "el_area_name", columnNames = { "company_id", "name" }) })
@Entity
@NamedQueries({ //
		@NamedQuery(name = "Area.findByName", query = "SELECT p FROM Area p where name = :name and companyID = :companyID"), //
		@NamedQuery(name = "Area.findByNameAndType", query = "SELECT p FROM Area p where name = :name and type = :type and companyID = :companyID"), //
		@NamedQuery(name = "Area.findByID", query = "SELECT p FROM Area p where id = :id and companyID = :companyID"), //
		@NamedQuery(name = "Area.referenceArea", query = "SELECT p FROM Area p where parentAreaID = :id"), //

})

public class Area extends hxc.ecds.protocol.rest.Area implements Serializable, ICompanyData<Area>, IBatchEnabled<Area>
{
	public static class SimpleAreaMapping {
		private Integer cellID;
		private Integer areaID;
		private String areaName;
		private String areaType;
        private Integer parentID;

        public SimpleAreaMapping(
            Integer areaID,
            String areaName,
            String areaType,
            Integer parentID
        ) {
            this.cellID = null;
            this.areaID = areaID;
			this.areaName = areaName;
			this.areaType = areaType;
            this.parentID = parentID;
        }

        public SimpleAreaMapping(
            Integer cellID,
            Integer areaID,
            String areaName,
            String areaType,
            Integer parentID
        ) {
            this.cellID = cellID;
            this.areaID = areaID;
			this.areaName = areaName;
			this.areaType = areaType;
            this.parentID = parentID;
        }

		public Integer getCellID() { return cellID; }
		public Integer getAreaID() { return areaID; }
		public String getAreaName() { return areaName; }
		public String getAreaType() { return areaType; }
		public Integer getParentID() { return parentID; }
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String UNKNOWN_AREA = "Unknown";
	public static final String UNKNOWN_AREA_TYPE = "Unknown";

	private static final long serialVersionUID = 7622513006085787600L;

	public static final Permission MAY_ADD = new Permission(false, false, Permission.GROUP_AREAS, Permission.PERM_ADD, "May Add Areas");
	public static final Permission MAY_UPDATE = new Permission(false, false, Permission.GROUP_AREAS, Permission.PERM_UPDATE, "May Update Areas");
	public static final Permission MAY_DELETE = new Permission(false, false, Permission.GROUP_AREAS, Permission.PERM_DELETE, "May Delete Areas");
	public static final Permission MAY_VIEW = new Permission(false, false, Permission.GROUP_AREAS, Permission.PERM_VIEW, "May View Areas");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Additional Fields
	//
	// /////////////////////////////////
	@JsonIgnore
	protected int lastUserID;
	@JsonIgnore
	protected Date lastTime;
	@JsonIgnore
	protected Area parentArea;
	@JsonIgnore
	protected List<Area> subAreas = new ArrayList<Area>();

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
	public Area setId(int id)
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
	public Area setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	@Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
	public String getName()
	{
		return name;
	}

	@Override
	public Area setName(String name)
	{
		this.name = name;
		return this;

	}

	@Override
	@Column(name = "type", nullable = false, length = TYPE_MAX_LENGTH)
	public String getType()
	{
		return type;
	}

	@Override
	public Area setType(String type)
	{
		this.type = type;
		return this;
	}

	@Override
	@Column(name = "parent_id", nullable = true, insertable = false, updatable = false)
	public Integer getParentAreaID()
	{
		return parentAreaID;
	}

	@Override
	public Area setParentAreaID(Integer parentAreaID)
	{
		this.parentAreaID = parentAreaID;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public Area setLastUserID(int lastUserID)
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
	@Version
	public Area setVersion(int version)
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
	public Area setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "FK_Area_Parent"))
	public Area getParentArea()
	{
		return parentArea;
	}

	public Area setParentArea(Area parentArea)
	{
		this.parentArea = parentArea;
		return this;
	}

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
	@JoinTable(name = "el_area_area", joinColumns = { @JoinColumn(name = "id") }, inverseJoinColumns = { @JoinColumn(name = "sub_id") })
	public List<Area> getSubAreas()
	{
		return subAreas;
	}

	public void setSubAreas(List<Area> subAreas)
	{
		this.subAreas = subAreas;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public Area()
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cloning
	//
	// /////////////////////////////////

	// Make deep copy
	public Area copy()
	{
		Area copy = new Area();
		copy.lastUserID = this.lastUserID;
		copy.lastTime = this.lastTime;
		copy.amend(this);
		return copy;
	}

	// Amend selected fields
	public void amend(hxc.ecds.protocol.rest.Area area)
	{
		this.id = area.getId();
		this.companyID = area.getCompanyID();
		this.version = area.getVersion();
		this.name = area.getName();
		this.type = area.getType();
		this.parentAreaID = area.getParentAreaID();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Finders
	//
	// /////////////////////////////////

	public static Area findByName(EntityManager em, int companyID, String name)
	{
		TypedQuery<Area> query = em.createNamedQuery("Area.findByName", Area.class);
		query.setParameter("companyID", companyID);
		query.setParameter("name", name);
		List<Area> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static Area findByNameAndType(EntityManager em, int companyID, String name, String type)
	{
		TypedQuery<Area> query = em.createNamedQuery("Area.findByNameAndType", Area.class);
		query.setParameter("companyID", companyID);
		query.setParameter("name", name);
		query.setParameter("type", type);
		List<Area> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<Area.SimpleAreaMapping> findAllSimple(EntityManager em, int companyID)
	{
		String nativeQuery = "SELECT id as area_id, name as area_name, parent_id, type as area_type, cell_id FROM " +
						"el_area as _area, el_area_cell _area_cell " +
						"WHERE _area.id = _area_cell.area_id " +
						"AND _area_cell.area_id is not NULL " + // superfolous, just being safe
						"AND _area.company_id = " + String.valueOf(companyID);

		Query query = em.createNativeQuery(nativeQuery, "AreaToCellMapping.findAllSimple");
		@SuppressWarnings("unchecked")
		List<Area.SimpleAreaMapping> result = query.getResultList();

		return result;
	}

	public static List<Area.SimpleAreaMapping> findByCellIDin(EntityManager em, HashSet<Integer> cellIDs, int companyID)
	{
		if (cellIDs.size() == 0) return new ArrayList<Area.SimpleAreaMapping>();

		HashSet<String> idsAsStrings = new HashSet<>();
		for(Integer cellID  : cellIDs) {
			idsAsStrings.add(String.valueOf(cellID));
		}

		//String nativeQuery = "AreaToParentMapping.findByIDin", query = "SELECT sub_id as child_id, _area.id as parent_id, name as parent_name, parent_id as next_parent_id FROM el_area _area, el_area_area _area_parents  WHERE _area.company_id = :companyID AND _area.id = _area_parents.id AND _area_parents.sub_id IN (1,2,3,4,5,6,7);"), //
		String nativeQuery = "SELECT id as area_id, name as area_name, parent_id, type as area_type, cell_id FROM " +
						"el_area as _area, el_area_cell _area_cell " +
						"WHERE _area.id = _area_cell.area_id " +
						"AND _area_cell.area_id is not NULL " + // superfolous, just being safe
						"AND _area.company_id = " + String.valueOf(companyID) + " " +
						"AND _area_cell.cell_id in (" + String.join(",", idsAsStrings) + ");";

		Query query = em.createNativeQuery(nativeQuery, "AreaToCellMapping.findByCellIDin");
		@SuppressWarnings("unchecked")
		List<Area.SimpleAreaMapping> result = query.getResultList();

		return result;
	}

	public static HashMap<Integer, Area.SimpleAreaMapping> findAreasWithRecursiveParentsByIDin(EntityManager em, HashSet<Integer> ids, int companyID)
	{
		if (ids.size() == 0) return new HashMap<Integer, Area.SimpleAreaMapping>();

		HashSet<String> idsAsStrings = new HashSet<>();
		for(Integer id  : ids) {
			idsAsStrings.add(String.valueOf(id));
		}

		/*
		// UNTESTED
		String nativeQuery = "SELECT child_id, t1.parent_id as parent_id, name as parent_name, type as parent_type, el_area.parent_id as next_parent_id FROM " +
						"el_area, " +
							"(SELECT id AS child_id, name AS child_name, type AS child_type, parent_id FROM el_area WHERE " +
							"company_id = " + String.valueOf(companyID) + " " +
							"AND id IN (" + String.join(",", idsAsStrings) + ")) as t1 " +
						"WHERE t1.parent_id = el_area.id AND el_area.company_id = " + String.valueOf(companyID);
		*/

		String nativeQuery = "WITH RECURSIVE recursive_parents (id, name, type, parent_id) AS " +
								"(SELECT id, name, type, parent_id FROM el_area " +
									"WHERE id in (" + String.join(",", idsAsStrings) + ") AND company_id = " + String.valueOf(companyID) + " " +
									"UNION ALL " +
								"SELECT p.id, p.name, p.type, p.parent_id FROM el_area p INNER JOIN recursive_parents " +
									"ON p.id = recursive_parents.parent_id" +
							") SELECT * FROM recursive_parents GROUP BY id";

		Query query = em.createNativeQuery(nativeQuery, "AreaToParentMapping.findByIDin");
		@SuppressWarnings("unchecked")
		List<Area.SimpleAreaMapping> result = query.getResultList();

		HashMap<Integer, Area.SimpleAreaMapping> finalResult = new HashMap<>();
		for(Area.SimpleAreaMapping row : result) {
			finalResult.put(row.getAreaID(), row);
		}

		return finalResult;
	}

	public static Area findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<Area> query = em.createNamedQuery("Area.findByID", Area.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<Area> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<Area> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, Area.class, params, companyID, "name");
	}

	public static Map<Integer, Area> findChildren(EntityManager em, Set<Integer> areaIDs, int companyID)
	{
		//Not the nicest and most efficient algorithm.  This is where the Hibernate Object Oriented approach falls-apart.
		Map<Integer, Area> results = new HashMap<Integer, Area>();
		for(Integer areaID : areaIDs)
		{
			TypedQuery<Area> query = em.createNamedQuery("Area.referenceArea", Area.class);
			query.setParameter("id", areaID);			
			List<Area> childAreas = query.getResultList();
			Set<Integer> tempAreas = new TreeSet<Integer>();
			for(Area childArea: childAreas)
			{
				tempAreas.add(childArea.getId());				
				results.putAll(findChildren(em, tempAreas, companyID));
			}
			results.put(areaID, findByID(em, areaID, companyID));
		}
		return results;
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Area.class, params, companyID, "name");
		return query.getSingleResult();
	}
	

	public static boolean referencesArea(EntityManager em, int areaID)
	{
		TypedQuery<Area> query = em.createNamedQuery("Area.referenceArea", Area.class);
		query.setParameter("id", areaID);
		query.setMaxResults(1);
		List<Area> results = query.getResultList();
		return results != null && results.size() > 0;
	}
		
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// MRD
	//
	// /////////////////////////////////
	public static void loadMRD(EntityManager em, int companyID, Session session) throws RuleCheckException
	{
		Permission.loadMRD(em, MAY_ADD, session);
		Permission.loadMRD(em, MAY_UPDATE, session);
		Permission.loadMRD(em, MAY_DELETE, session);
		Permission.loadMRD(em, MAY_VIEW, session);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public void persist(EntityManager em, Area previous, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(previous);
		QueryBuilder.persist(em, previous, this, session, AuditEntry.TYPE_AREA, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		QueryBuilder.remove(em, this, session, AuditEntry.TYPE_AREA, auditEntryContext);
	}

	@Override
	public void validate(Area previous) throws RuleCheckException
	{
		RuleCheck.validate(this);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private boolean contains(List<? extends hxc.ecds.protocol.rest.Cell> cells, hxc.ecds.protocol.rest.Cell cell)
	{
		for (hxc.ecds.protocol.rest.Cell perm : cells)
		{
			if (perm.getId() == cell.getId())
				return true;
		}
		return false;
	}







}
