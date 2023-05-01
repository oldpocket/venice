package nz.org.venice.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nz.org.venice.prefs.PreferencesManager;

public class DatabaseHelper {
	
	public static DatabaseManager getDatabaseManager() {
		DatabaseManager dbm = null;
		
		if (PreferencesManager.getQuoteSource() == PreferencesManager.INTERNAL) {
			
			String fileName = PreferencesManager.getInternalFileName();
			dbm = new DatabaseManager(fileName);
		}

		if (PreferencesManager.getQuoteSource() == PreferencesManager.DATABASE) {
		
			PreferencesManager.DatabasePreferences prefs = PreferencesManager.getDatabaseSettings();
			String password = DatabaseAccessManager.getInstance().getPassword();
			
			dbm = new DatabaseManager(
					prefs.software, 
					prefs.driver, 
					prefs.host, 
					prefs.port, 
					prefs.database, 
					prefs.username, 
					password);
		}
		return dbm;
	}

	/**
     * Method help to convert SQL request data to your custom DTO Java class object.   
     * Requirements: fields of your Java class should have Type: String and have the same name as in sql table
     *
     * @param resultSet     - sql-request result
     * @param clazz - Your DTO Class for mapping
     * @return <T> List <T> - List of converted DTO java class objects
     */
    public static <T> List <T> convertSQLResultSetToObject(ResultSet resultSet, Class<T> clazz) 
    	throws 
    		SQLException, 
    		NoSuchMethodException,
    		InvocationTargetException, 
    		InstantiationException, 
    		IllegalAccessException {

        List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
        for(Field field: fields) {
            field.setAccessible(true);
        }

        List<T> list = new ArrayList<>();
        while(resultSet.next()) {

            T dto = clazz.getConstructor().newInstance();

            for(Field field: fields) {
                String name = field.getName();
                
                try {
                    String value = resultSet.getString(name);
                    if (value == null) {
                    	field.set(dto, null);
                    } else if(field.getType().isEnum()) {
                    	field.set(dto, Enum.valueOf((Class<Enum>) field.getType(), value.toUpperCase()));
                    } else {
                       	field.set(dto, convertInstanceOfObject(value, field.getType()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            list.add(dto);
        }
        return list;
    }
	
    private static <T> T convertInstanceOfObject(Object o, Class<T> clazz) {
        try {
        	if (clazz == Boolean.class)
        		return clazz.cast(convertToBoolean(o.toString()));
            return clazz.cast(o);
        } catch(ClassCastException e) {
            return null;
        }
    }
    
    private static boolean convertToBoolean(String value) {
        boolean returnValue = false;
        if ("1".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || 
            "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value))
            returnValue = true;
        return returnValue;
    }
    
}
