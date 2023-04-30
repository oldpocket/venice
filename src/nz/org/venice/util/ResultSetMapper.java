package nz.org.venice.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nz.org.venice.quote.Symbol;
import nz.org.venice.quote.SymbolFormatException;
import nz.org.venice.quote.SymbolMetadata.SymbolType;

public class ResultSetMapper {

	/**
     * Method help to convert SQL request data to your custom DTO Java class object.   
     * Requirements: fields of your Java class should have Type: String and have the same name as in sql table
     *
     * @param resultSet     - sql-request result
     * @param clazz - Your DTO Class for mapping
     * @return <T> List <T> - List of converted DTO java class objects
     */
    public static <T> List <T> convertSQLResultSetToObject(ResultSet resultSet, Class<T> clazz) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

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
                    Class type = field.getType();
                    
                    if (type == Boolean.class) {
                    	Boolean b = !value.equals("0");
                    	field.set(dto, b);
                    } else if (value == null){
                    	field.set(dto, null);
                    } else if(field.getType().isEnum()) {
                    	field.set(dto, Enum.valueOf((Class<Enum>) field.getType(), value.toUpperCase()));
                    } else {
                    	//field.set(dto, field.getType().getConstructor(String.class).newInstance(value));
                    	field.set(dto, field.getType().getConstructor(field.getType()).newInstance(value));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            list.add(dto);

        }
        return list;
    }

	
}
