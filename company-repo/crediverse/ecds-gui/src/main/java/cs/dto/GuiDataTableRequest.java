package cs.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class GuiDataTableRequest
{
	private Integer draw;
	private List<Column> columns;
	private List<Order> order;
	private Search search;
	private Integer start;
	private Integer length;

	@Getter
	@Setter
	@ToString
	public static class Search {
		String value;
		String regex;

		public Search()
		{

		}

		public Search( String value, String regex )
		{
			this.value = value;
			this.regex = regex;
		}
	}

	@Getter
	@Setter
	@ToString
	public static class Column {
		int index;
		String data;
		String name;
		boolean searchable;
		boolean orderable;
		Search search;

		public Column()
		{

		}

		public Column( Map<String, String> params, int index )
		{
			this.index = index;

			String fnData = "columns[" + this.index + "][data]";
			if ( params.containsKey( fnData ) )
				this.data = params.get( fnData );

			String fnName = "columns[" + this.index + "][name]";
			if ( params.containsKey( fnName ) )
				this.name = params.get( fnName );

			String fnSearchable = "columns[" + this.index + "][searchable]";
			if ( params.containsKey( fnSearchable ) )
				this.searchable = params.get( fnSearchable ).equals( "true" );

			String fnOrderable = "columns[" + this.index + "][orderable]";
			if ( params.containsKey( fnOrderable ) )
				this.orderable = params.get( fnOrderable ).equals( "true" );

			if ( params.containsKey( "column[" + this.index + "][search][value]" ) && params.containsKey( "column[" + this.index + "][search][regex]" ) )
				this.search = new GuiDataTableRequest.Search( params.get( "column[" + this.index + "][search][value]" ), params.get( "column[" + this.index + "][search][regex]" ) );
		}
	}

	@Getter
	@Setter
	@ToString
	public static class Order {
		int index;
		Column column;
		String direction;

		public Order()
		{

		}

		public Order( Map<String, String> params, int index, List<Column> columns )
		{
			this.index = index;
			String fnColumn = "order[" + this.index + "][column]";
			if ( params.containsKey( fnColumn ) )
			{
				Integer columnIndex = Integer.parseInt( params.get( fnColumn ) );
				this.column = columns.get( columnIndex );
			}

			String fnDir = "order[" + this.index + "][dir]";
			if ( params.containsKey( fnDir ) )
				this.direction = params.get( fnDir );
		}

		public boolean isAscending()
		{
			return ( direction != null ) && direction.equals( "asc" );
		}

		public boolean isDescending()
		{
			return ( direction != null ) && direction.equals( "desc" );
		}
	}

	public GuiDataTableRequest()
	{
		this.columns = new ArrayList<Column>();
		this.order = new ArrayList<Order>();
	}

	public GuiDataTableRequest(Map<String, String> params) throws java.io.UnsupportedEncodingException
	{
		this.columns = new ArrayList<Column>();
		this.order = new ArrayList<Order>();

		if ( params.containsKey( "start" ) && params.get( "start" ).length() > 0)
			this.start = Integer.parseInt( params.get( "start" ) );

		if ( params.containsKey( "length" ) && params.get( "length" ).length() > 0)
		{
			try
			{
				this.length = Integer.parseInt( params.get( "length" ) );
			}
			catch(Exception ex)
			{
				this.length = 10;
			}
		}

		if ( params.containsKey( "draw" ) )
			this.draw = Integer.parseInt( params.get( "draw" ) );

		if ( params.containsKey( "search[value]" ) && params.containsKey( "search[regex]" ) )
			this.search = new GuiDataTableRequest.Search( java.net.URLDecoder.decode(params.get( "search[value]" ), "UTF-8").trim(), params.get( "search[regex]" ) );

		int cix = 0;
		while ( params.containsKey( "columns[" + cix + "][data]" ) )
		{
			this.columns.add( new GuiDataTableRequest.Column( params, cix++ ) );
		}

		int oix = 0;
		while ( params.containsKey( "order[" + oix + "][column]" ) )
		{
			this.order.add( new GuiDataTableRequest.Order( params, oix++, this.columns ) );
		}
	}
}
