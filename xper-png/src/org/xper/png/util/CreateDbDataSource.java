package org.xper.png.util;

import java.beans.PropertyVetoException;
import javax.sql.DataSource;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class CreateDbDataSource {

	DataSource dataSource;
	
	public CreateDbDataSource() {
		
		dataSource = makeDataSource();
	}
	
	public DataSource makeDataSource() {
				
		String jdbcUrl = "jdbc.url=jdbc:mysql://172.30.6.80/alexandriya_180218_test?useSSL=false";
		
		ComboPooledDataSource source = new ComboPooledDataSource();
		try {
			source.setDriverClass("com.mysql.jdbc.Driver");
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		source.setJdbcUrl("jdbc:mysql://172.30.6.80/alexandriya_180218_test?useSSL=false");
		source.setUser("xper_rw");
		source.setPassword("up2nite");
		
		return source;
		
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	
}
