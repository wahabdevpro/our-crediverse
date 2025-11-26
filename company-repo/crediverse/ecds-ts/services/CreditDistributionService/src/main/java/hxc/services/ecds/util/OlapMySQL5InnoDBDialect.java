package hxc.services.ecds.util;

import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class OlapMySQL5InnoDBDialect extends org.hibernate.dialect.MySQL5InnoDBDialect
{
    public OlapMySQL5InnoDBDialect()
    {
        super();
        registerFunction("useindex_wpr", new StandardSQLFunction("useindex_wpr", StandardBasicTypes.BOOLEAN));
        registerFunction("useindex_rpr", new StandardSQLFunction("useindex_rpr", StandardBasicTypes.BOOLEAN));
        registerFunction("useindex_ssr", new StandardSQLFunction("useindex_ssr", StandardBasicTypes.BOOLEAN));
        registerFunction("useindex_gsr", new StandardSQLFunction("useindex_gsr", StandardBasicTypes.BOOLEAN));
        registerFunction("useindex_rproa", new StandardSQLFunction("useindex_rproa", StandardBasicTypes.BOOLEAN));
        registerFunction("useindex_wproa", new StandardSQLFunction("useindex_wproa", StandardBasicTypes.BOOLEAN));
        registerFunction("useindex_wprob", new StandardSQLFunction("useindex_wprob", StandardBasicTypes.BOOLEAN));
    }
}
