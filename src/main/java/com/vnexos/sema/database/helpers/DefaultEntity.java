package com.vnexos.sema.database.helpers;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vnexos.sema.database.HelperType;
import com.vnexos.sema.database.IdType;
import com.vnexos.sema.database.annotations.Column;
import com.vnexos.sema.database.annotations.DataHelper;
import com.vnexos.sema.database.annotations.Identity;

/**
 * Provides the recommended entity types, this entity is using UUID type for
 * {@code id} column for the most safe. More over this entity also provides the
 * way to handle create time and update time
 * 
 * @author Trần Việt Đăng Quang
 */
public class DefaultEntity {
  @Identity(type = IdType.UUID)
  @Column(type = "uniqueidentifier")
  private UUID id;

  @Column
  @DataHelper(type = HelperType.CREATED_AT)
  private LocalDateTime createdAt;
  @Column
  @DataHelper(type = HelperType.UPDATED_AT)
  private LocalDateTime updatedAt;

  /**
   * Gets the ID of the entity
   * 
   * @return the entity ID
   */
  public UUID getId() {
    return id;
  }

  /**
   * Sets the ID of the entity
   * 
   * @param id the ID to set
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   * Gets the create time of the entity
   * 
   * @return the entity created time
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * Sets the create time of the entity
   * 
   * @param createdAt the created time to set
   */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * Gets the update time of the entity
   * 
   * @return the entity update time
   */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  /**
   * Sets the update time of the entity
   * 
   * @param updatedAt the updated time to set
   */
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
