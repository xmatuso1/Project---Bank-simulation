/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.fi.muni.pv168.bank;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class Main {

    final static Logger log = LoggerFactory.getLogger(Main.class);

    public static DataSource createMemoryDatabase() {
        BasicDataSource bds = new BasicDataSource();
        //set JDBC driver and URL
        bds.setDriverClassName("org.apache.derby.jdbc.ClientDriver");
        bds.setUrl("jdbc:derby://localhost:1527/bankDb");
        //populate db with tables and data
        

        return bds;
    }

    public static void main(String[] args)  {

        log.info("zaciname");

        DataSource dataSource = createMemoryDatabase();
        PersonManager bookManager = new PersonManagerImpl(dataSource);
    }
}
