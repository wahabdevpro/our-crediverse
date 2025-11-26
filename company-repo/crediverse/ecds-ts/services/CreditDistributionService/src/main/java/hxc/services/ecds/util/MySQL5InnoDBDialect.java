package hxc.services.ecds.util;

import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class MySQL5InnoDBDialect extends org.hibernate.dialect.MySQL5InnoDBDialect
{
    public MySQL5InnoDBDialect()
    {
        super();
        registerFunction("useindex", new StandardSQLFunction("useindex", StandardBasicTypes.BOOLEAN));
    }
}
