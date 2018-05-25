package pw.eit.junit5.mysql.repository;

import pw.eit.junit5.mysql.domain.MySQLTestObj;
import org.springframework.data.repository.CrudRepository;

public interface MySQLTestObjRepository extends CrudRepository<MySQLTestObj, Long>
{
}
