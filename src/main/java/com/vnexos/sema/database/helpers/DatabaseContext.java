package com.vnexos.sema.database.helpers;

import com.vnexos.sema.loader.annotations.AutoWired;

/**
 * Defines all necessary database command for CRUD (create, read, update,
 * delete). Just need to create another interface that inherit this interface,
 * then use {@link AutoWired @AutoWired} for getting it instance, finally just
 * need to call the function and the system will automatically generate the SQL
 * command and run, then give back the result.
 * <p>
 * For example:
 * 
 * <pre>
 * public interface ILanguageRepository extends DatabaseContext<Language, UUID> {
 * }
 * </pre>
 * 
 * This code is totally find but if you want to customize it, just need to add
 * some more function to the interface follow the rules. For example:
 * 
 * <pre>
 * public Language get(String code);
 * </pre>
 * 
 * The created function will generate an SQL command and get the language that
 * have the same code with the given code. The "Language" entity should also
 * have the column "code", the name of the parameter is important.
 * 
 * <p>
 * To use the repository, you just need to create one field at the class you
 * want to use. For example:
 * 
 * <pre>
 * &#064;AutoWired
 * private ILanguageRepository languageRepository;
 * </pre>
 * 
 * The system will assign the instance for {@code languageRepository} that can
 * be used easily in the class.
 * 
 * @author Trần Việt Đăng Quang
 * @see com.vnexos.sema.loader.annotations.AutoWired
 * 
 */
public interface DatabaseContext<T, ID> {
  /**
   * Gets all records of entity.
   * 
   * @return a list of entities
   */
  public T[] getAll();

  /**
   * Gets the record of entity that have the same {@code id} with the given
   * {@code id}.
   * 
   * @param id the id of the entity you want to get
   * @return the entity
   */
  public T get(ID id);

  /**
   * Gets the number of records in table
   * 
   * @return the number of entities
   */
  public int count();

  /**
   * Inserts an entity to the table
   * 
   * @param t entity for inserting
   * @return the inserted entity
   */
  public T create(T t);

  /**
   * Updates an entity
   * 
   * @param t the entity with given {@link T#id t.id}
   * @return the entity before editing
   */
  public T update(T t);

  /**
   * Deletes an entity that have the same {@code id} with the given {@code id}.
   * 
   * @param id the id of entity for deleting
   * @return the deleted entity
   */
  public T delete(ID id);

  /**
   * Runs the custom query
   * 
   * @param cmd the query command to run
   * @return the JSON string after called
   * @deprecated custom query is not supported
   */
  @Deprecated
  public String customQuery(String cmd);
}
