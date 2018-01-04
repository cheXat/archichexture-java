package at.chex.archichexture.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 03.01.18
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {

  /**
   * Is this an aspect, that can be filtered? Can i ask the webservice for entities with this attribute?
   */
  boolean filterable() default true;

  /**
   * Will this attribute be included in the returned DTO? Set this to #false to keep it inside your application and away from nosy clients.
   */
  boolean exported() default true;

  /**
   * Can this attribute be modified by an update query?
   */
  boolean modifieable() default true;

  /**
   * When filtering, should this be an 'equals' or a 'like' query? E.g. do you want to be able to use '%' and search case-insensitive -> leave it #false
   */
  boolean strict() default false;
}
