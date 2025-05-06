package com.vnexos.sema.loader;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vnexos.sema.Constants;
import com.vnexos.sema.database.Database;
import com.vnexos.sema.database.DatabaseEngine;
import com.vnexos.sema.database.HelperType;
import com.vnexos.sema.database.IdType;
import com.vnexos.sema.database.annotations.DataHelper;
import com.vnexos.sema.database.annotations.Entity;
import com.vnexos.sema.database.annotations.Identity;
import com.vnexos.sema.database.helpers.DatabaseContext;
import com.vnexos.sema.loader.annotations.repository.ContextId;
import com.vnexos.sema.util.ClassUtils;
import com.vnexos.sema.util.Mapper;
import com.vnexos.sema.util.PrivateServiceConstructor;
import com.vnexos.sema.util.StringUtils;
import com.vnexos.sema.util.UUIDUtil;
import com.vnexos.sema.util.format.FormatException;
import com.vnexos.sema.util.format.Formatter;
import com.vnexos.sema.util.logger.Logger;
import com.vnexos.sema.util.logger.LoggerFormatDriver;

/**
 * Handle called method from context
 * 
 * @author Trần Việt Đăng Quang
 */
public class ContextHandler {
  /**
   * Private constructor to avoid create object of this class
   */
  private ContextHandler() {
  }

  /**
   * Log the sql command in {@link com.vnexos.sema.util.logger.LoggerType#SQL
   * LoggerType.SQL} level to the screen.
   * 
   * @param sql The sql command to log
   */
  private static void log(String sql, long ms) {
    StringBuilder sb = new StringBuilder();
    sb.append(sql);

    sb.append("$fg(15) (");
    sb.append("$fg(12)");
    sb.append(System.currentTimeMillis() - ms);
    sb.append(" $fg(15)ms)");
    Formatter formatter = new Formatter();
    try {
      StackTraceElement[] elements = Thread.currentThread().getStackTrace();
      LoggerFormatDriver.setClassPath(elements[2]);
      PrivateServiceConstructor.invokeFunction(
          Logger.class, "sql", Constants.context.getLogger(),
          PrivateServiceConstructor.createClassTypes(String.class),
          PrivateServiceConstructor.createObjects(formatter.format(sb.toString())));
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException
        | FormatException e) {
      Constants.context.log("SQL: " + sql);
    }
  }

  /**
   * Handles calling create method of the context
   * 
   * @param tableName    the name of the table
   * @param entityType   the type of entity
   * @param idType       the type of entity ID
   * @param entityObject the object of entity to create
   * @return the created entity
   * @throws SQLException           if the SQL statement cannot be generated
   * @throws IllegalAccessException if the entity ID field cannot be get or set
   */
  private static Object invokeCreateFunction(String tableName, Class<?> entityType, Class<?> idType,
      Object entityObject) throws SQLException, IllegalAccessException {
    long time = System.currentTimeMillis();
    Field idField = ClassUtils.findAnnotatedField(entityType, Identity.class);
    Identity id = idField.getAnnotation(Identity.class);

    if (id.type() == IdType.UUID && idType == UUID.class && idField.getType() == UUID.class) {
      idField.setAccessible(true);
      if (idField.get(entityObject) == null)
        idField.set(entityObject, UUIDUtil.v6());
    }

    List<Field> fields = ClassUtils.getAllFields(entityType);
    for (Field field : fields) {
      DataHelper dataHelper = field.getAnnotation(DataHelper.class);
      if (dataHelper != null && dataHelper.type() == HelperType.CREATED_AT) {
        field.setAccessible(true);
        field.set(entityObject, LocalDateTime.now());
      }
    }

    String sql = Database.getEngine().generateInsert(tableName, entityObject);

    Connection conn = Database.getConnection();
    Statement stm = conn.createStatement();
    stm.executeUpdate(sql);

    log(sql, time);
    conn.close();

    return entityObject;
  }

  /**
   * Handles calling get method of the context
   * 
   * @param method        the called method itself
   * @param tableName     the name of the table
   * @param entityType    the type of entity
   * @param entityObjects the passed parameters of the method
   * @return the object get from executing SQL staatement (can be array)
   * @throws SQLException     if the statement cannot be generated
   * @throws ContextException if the called method is invalid
   */
  private static Object invokeGetFunction(Method method, String tableName, Class<?> entityType, Object[] entityObjects)
      throws SQLException, ContextException {
    long time = System.currentTimeMillis();
    DatabaseEngine engine = Database.getEngine();
    String methodName = method.getName().replace("get", "");
    String sql = buildSqlQuery(engine, method, tableName, methodName, entityObjects);

    Connection conn = Database.getConnection();
    Statement stm = conn.createStatement();
    ResultSet rs = stm.executeQuery(sql);
    JsonArray jsonArray = Mapper.serializeResultSet(rs);

    log(sql, time);
    conn.close();

    return mapResultsToReturnType(method, entityType, jsonArray);
  }

  /**
   * Builds an SQL query to get from table
   * 
   * @param engine       the current used engine of database
   * @param method       the called method
   * @param tableName    the name of the table
   * @param methodName   the name of method
   * @param entityObject the objects passed from parameters
   * @return the SQL statement
   * @throws ContextException if the method is invalid
   * @throws SQLException     if the SQL statement cannot be generated
   */
  private static String buildSqlQuery(DatabaseEngine engine, Method method, String tableName,
      String methodName, Object[] entityObject) throws ContextException, SQLException {
    if (isGetAllQuery(methodName, entityObject)) {
      return engine.generateGet(tableName, null, null);
    }

    String[] columns = extractColumns(methodName);
    String condition = buildWhereCondition(engine, method, entityObject);

    String sql = engine.generateGet(tableName, columns, condition);
    if (sql == null) {
      throw new ContextException("Invalid get function `" + method.getName() + "`!");
    }
    return sql;
  }

  /**
   * Checks if the current method is getting all or not
   * 
   * @param methodName   the name of method to analyze
   * @param entityObject the object passed through parameters
   * @return true if the method is a getAll method, false otherwise
   */
  private static boolean isGetAllQuery(String methodName, Object[] entityObject) {
    return (methodName.equals("All") || methodName.equals("all")) && entityObject.length == 0;
  }

  /**
   * Extracts the column to get in the method name
   * 
   * @param methodName the name of method
   * @return the list of column to get
   */
  private static String[] extractColumns(String methodName) {
    if (methodName.isEmpty()) {
      return new String[0];
    }
    String[] columns = methodName.split("And");
    for (int i = 0; i < columns.length; i++) {
      columns[i] = StringUtils.convertCamelToSnake(columns[i]);
    }
    return columns;
  }

  /**
   * Builds where condition for SQL
   * 
   * @param engine       the current used engine of database
   * @param method       the called method to handle
   * @param entityObject the list of object passed through parameters
   * @return the where statement
   */
  private static String buildWhereCondition(DatabaseEngine engine, Method method, Object[] entityObject) {
    if (method.getParameters().length == 0) {
      return "";
    }

    StringBuilder condition = new StringBuilder("WHERE ");
    Parameter[] parameters = method.getParameters();

    for (int i = 0; i < parameters.length; i++) {
      appendParameterCondition(engine, condition, parameters[i], entityObject[i]);

      if (i != parameters.length - 1) {
        condition.append(" AND ");
      }
    }

    return condition.toString();
  }

  /**
   * Appends parameter condition to the string builder
   * 
   * @param engine    the current used engine of database
   * @param condition the string builder to append to
   * @param parameter the parameter to analyze
   * @param value     the value to check with param
   */
  private static void appendParameterCondition(DatabaseEngine engine, StringBuilder condition,
      Parameter parameter, Object value) {
    condition.append(engine.getNameOpening())
        .append(StringUtils.convertCamelToSnake(parameter.getName()))
        .append(engine.getNameEnding());

    if (value instanceof String && ((String) value).contains("*")) {
      String likePattern = ((String) value).replace("*", "%");
      condition.append(" LIKE ")
          .append(engine.getValueOpening())
          .append(likePattern)
          .append(engine.getValueEnding());
    } else {
      condition.append('=')
          .append(engine.getValueOpening())
          .append(value)
          .append(engine.getValueEnding());
    }
  }

  /**
   * Maps the result get from executing SQL statement
   * 
   * @param method     the called method to handle
   * @param entityType type of the entity
   * @param jsonArray  the value from result set
   * @return the mapped object
   */
  private static Object mapResultsToReturnType(Method method, Class<?> entityType, JsonArray jsonArray) {
    if (jsonArray.size() == 0) {
      return null;
    }

    Class<?> returnType = method.getReturnType() == Object.class ? entityType : method.getReturnType();

    if (returnType.isArray()) {
      return createArrayResult(returnType, entityType, jsonArray);
    }

    JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
    return Mapper.map(jsonObject, returnType);
  }

  /**
   * Creates array result for SQL statements
   * 
   * @param returnType the return type of method
   * @param entityType the type of entity
   * @param jsonArray  the value to map
   * @return the mapped array object
   */
  private static Object createArrayResult(Class<?> returnType, Class<?> entityType, JsonArray jsonArray) {
    Class<?> componentType = returnType.getComponentType() == Object.class ? entityType : returnType.getComponentType();
    Object array = Array.newInstance(componentType, jsonArray.size());

    for (int i = 0; i < jsonArray.size(); i++) {
      JsonObject obj = jsonArray.get(i).getAsJsonObject();
      Object mapped = Mapper.map(obj, componentType);
      Array.set(array, i, mapped);
    }

    return array;
  }

  /**
   * Counts the number of records matches given condition.
   * 
   * @param method       the called method to handle
   * @param tableName    the name of table
   * @param entityType   the type of entity
   * @param entityObject the array of objects passed through parameters
   * @return the number of records
   * @throws ContextException if the method is invalid
   * @throws SQLException     if the SQL statement cannot be generated
   */
  private static Object invokeCountFunction(Method method, String tableName, Class<?> entityType, Object[] entityObject)
      throws ContextException, SQLException {
    long time = System.currentTimeMillis();
    DatabaseEngine engine = Database.getEngine();

    String condition = buildWhereCondition(engine, method, entityObject);

    String sql = engine.generateCount(tableName, condition.toString());

    if (sql == null)
      throw new ContextException("Invalid count function for `" + tableName + "`!");

    Connection conn = Database.getConnection();
    Statement stm = conn.createStatement();
    ResultSet rs = stm.executeQuery(sql);
    log(sql, time);

    if (rs.next()) {
      return rs.getInt(1);
    } else {
      return 0;
    }
  }

  /**
   * Finds the id parameter which annotated by {@code &#64;ContextId} in the given
   * parameters list.
   * 
   * @param parameters the list of parameters
   * @return the index of ID parameter
   */
  private static int checkUpdateParameters(Parameter[] parameters) {
    for (int i = 0; i < parameters.length; i++) {
      if (parameters[i].getAnnotation(ContextId.class) != null)
        return i;
    }
    return -1;
  }

  /**
   * Finds the parameter that have the same name with the given field
   * 
   * @param parameters the list of parameters
   * @param field      the field to check
   * @return the index of parameter in array
   */
  private static int checkFieldMatchParameters(Parameter[] parameters, Field field) {
    for (int i = 0; i < parameters.length; i++) {
      if (parameters[i].getName().equals(field.getName()))
        return i;
    }
    return -1;
  }

  /**
   * Updates an entity by the id of the entity
   * <p>
   * This method can handle:
   * <ol>
   * <li>Entity-based updates (single parameter of entity type)
   * <li>Field-based updates (multiple parameters matching entity type)
   * </ol>
   * 
   * 
   * @param <T>        the type of entity
   * @param <ID>       the type of id
   * @param proxy      the instance of proxy itself
   * @param method     the called method for handle
   * @param tableName  the name of table
   * @param entityType the type of entity
   * @param idType     the type of id
   * @param objects    the array of objects passed through parameters
   * @return the updated entity
   * @throws ContextException       if the method is invalid
   * @throws SQLException           if the SQL statement cannot be generated
   * @throws IllegalAccessException if the entity field is not able to set
   */
  @SuppressWarnings("unchecked")
  private static <T, ID> Object invokeUpdateFunction(Object proxy, Method method, String tableName, Class<T> entityType,
      Class<ID> idType, Object[] objects)
      throws ContextException, SQLException, IllegalAccessException {
    long time = System.currentTimeMillis();
    Parameter[] parameters = method.getParameters();
    DatabaseEngine engine = Database.getEngine();

    Object entityObject;
    String sql = null;

    if (parameters.length == 0)
      throw new ContextException("Invalid update function.");
    else if (parameters.length == 1) {
      Parameter entityParam = parameters[0];
      if (entityParam.getType() != entityType)
        throw new ContextException("Invalid update function.");

      entityObject = objects[0];
      Field idField = ClassUtils.findAnnotatedField(entityType, Identity.class);

      if (idField == null) {
        throw new ContextException("No @Identity field found in " + entityType.getSimpleName());
      }

      idField.setAccessible(true);
      Object idValue;
      try {
        idValue = idField.get(entityObject);
      } catch (IllegalAccessException e) {
        throw new ContextException("Failed to access identity field", e);
      }

      if (idValue == null) {
        throw new ContextException("Cannot update: identity field value is null");
      }

      StringBuilder where = new StringBuilder();
      where
          .append(engine.getNameOpening())
          .append(StringUtils.convertCamelToSnake(idField.getName()))
          .append(engine.getNameEnding())
          .append('=')
          .append(engine.getValueOpening())
          .append(idValue)
          .append(engine.getValueEnding());

      List<Field> fields = ClassUtils.getAllFields(entityType);
      for (Field field : fields) {
        DataHelper dataHelper = field.getAnnotation(DataHelper.class);
        if (dataHelper != null && dataHelper.type() == HelperType.CREATED_AT) {
          field.setAccessible(true);
          field.set(entityObject, LocalDateTime.now());
        }
      }
      sql = engine.generateUpdate(tableName, entityObject, where.toString());

    } else {
      int idParamPosition;
      if ((idParamPosition = checkUpdateParameters(parameters)) < 0)
        throw new ContextException("The update function must have @ContextId parameter.");

      List<Field> fields = ClassUtils.getAllFields(entityType);
      Map<String, Object> data = new HashMap<>();
      StringBuilder where = new StringBuilder();

      for (int i = 0; i < fields.size(); i++) {
        int paramPos;
        if ((paramPos = checkFieldMatchParameters(parameters, fields.get(i))) >= 0) {
          if (fields.get(i).getAnnotation(Identity.class) != null) {
            where
                .append(engine.getNameOpening())
                .append(StringUtils.convertCamelToSnake(fields.get(i).getName()))
                .append(engine.getNameEnding())
                .append('=')
                .append(engine.getValueOpening())
                .append(objects[idParamPosition])
                .append(engine.getValueEnding());
            continue;
          }
          data.put(parameters[paramPos].getName(), objects[paramPos]);
        }
      }

      for (Field field : fields) {
        DataHelper dataHelper = field.getAnnotation(DataHelper.class);
        if (dataHelper != null && dataHelper.type() == HelperType.UPDATED_AT) {
          data.put(field.getName(), LocalDateTime.now());
        }
      }
      sql = engine.generateUpdate(tableName, data, where.toString());
      entityObject = ((DatabaseContext<T, ID>) proxy).get(Mapper.map(objects[idParamPosition].toString(), idType));
    }
    Connection conn = Database.getConnection();
    Statement stm = conn.createStatement();

    stm.executeUpdate(sql);
    log(sql, time);
    conn.close();

    return entityObject;
  }

  /**
   * Deletes an entity by the id of the entity
   * 
   * @param <T>        generic type of entity
   * @param <ID>       generic type of ID
   * @param proxy      the instance of proxy
   * @param tableName  the name of table
   * @param entityType the type of entity
   * @param idType     the type of Id
   * @param object     the parameter
   * @return the deleted entity
   * @throws ContextException if the called method is invalid
   * @throws SQLException     if the SQL statement cannot be generated
   */
  @SuppressWarnings("unchecked")
  private static <T, ID> Object invokeDeleteFunction(Object proxy, String tableName, Class<T> entityType,
      Class<ID> idType, Object object) throws ContextException, SQLException {
    long time = System.currentTimeMillis();
    DatabaseEngine engine = Database.getEngine();
    String sql;
    Object entityObject;
    System.out.println(object.getClass());
    if (object.getClass() == idType) {
      Field idField = ClassUtils.findAnnotatedField(entityType, Identity.class);

      StringBuilder where = new StringBuilder();
      where
          .append(engine.getNameOpening())
          .append(StringUtils.convertCamelToSnake(idField.getName()))
          .append(engine.getNameEnding())
          .append('=')
          .append(engine.getValueOpening())
          .append(object)
          .append(engine.getValueEnding());
      sql = engine.generateDelete(tableName, where.toString());
      entityObject = ((DatabaseContext<T, ID>) proxy).get(Mapper.map(object, idType));
    } else if (object.getClass() == entityType) {
      Field idField = ClassUtils.findAnnotatedField(entityType, Identity.class);

      if (idField == null) {
        throw new ContextException("No @Identity field found in " + entityType.getSimpleName());
      }

      idField.setAccessible(true);
      Object idValue;
      try {
        idValue = idField.get(object);
      } catch (IllegalAccessException e) {
        throw new ContextException("Failed to access identity field", e);
      }

      if (idValue == null) {
        throw new ContextException("Cannot update: identity field value is null");
      }

      StringBuilder where = new StringBuilder();
      where
          .append(engine.getNameOpening())
          .append(StringUtils.convertCamelToSnake(idField.getName()))
          .append(engine.getNameEnding())
          .append('=')
          .append(engine.getValueOpening())
          .append(idValue)
          .append(engine.getValueEnding());
      sql = engine.generateDelete(tableName, where.toString());
      entityObject = object;
    } else
      throw new ContextException("Invalid delete function");

    Connection conn = Database.getConnection();
    Statement stm = conn.createStatement();

    stm.executeUpdate(sql);
    log(sql, time);
    conn.close();

    return entityObject;
  }

  /**
   * Handles all calling from the proxy of database context
   * 
   * @param proxy     the instance of the proxy itself
   * @param method    the called method
   * @param args      the args of the method
   * @param arguments the generic types, the first element for Entity type, and
   *                  the second element is for ID type
   * @return the associated object
   * @throws ContextException if the called method is invalid
   */
  public static Object invoke(Object proxy, Method method, Object[] args, Type[] arguments) throws ContextException {
    Class<?> entityType = (Class<?>) arguments[0];
    Class<?> idType = (Class<?>) arguments[1];
    try {
      Entity entity = entityType.getAnnotation(Entity.class);
      if (entity == null)
        throw new ContextException("The given type `" + entityType.getName() + "` is not an entity.");
      String table = entity.tableName();

      if (method.getName().equals("create"))
        return invokeCreateFunction(table, entityType, idType, args[0]);
      else if (method.getName().startsWith("get"))
        return invokeGetFunction(method, table, entityType, args);
      else if (method.getName().equals("count") && isInteger(method.getReturnType()))
        return invokeCountFunction(method, table, entityType, args);
      else if (method.getName().equals("update"))
        return invokeUpdateFunction(proxy, method, table, entityType, idType, args);
      else if (method.getName().equals("delete"))
        return invokeDeleteFunction(proxy, table, entityType, idType, args[0]);
      else if (method.getName().equals("toString"))
        return method.toString();
      else
        Constants.context.log("You called `" + method.getName() + "` function.");
    } catch (ContextException ce) {
      Constants.context.log(ce);
    } catch (Exception ex) {
      throw new ContextException("Error occured while calling context method `" + method.getName() + "`.", ex);
    }

    return null;
  }

  /**
   * Checks if the type is integer or not
   * 
   * @param clazz the type to check
   * @return true if the type is boolean, false otherwise
   */
  private static boolean isInteger(Class<?> clazz) {
    return clazz == byte.class || clazz == Byte.class
        || clazz == short.class || clazz == Short.class
        || clazz == int.class || clazz == Integer.class
        || clazz == long.class || clazz == Long.class;
  }
}
