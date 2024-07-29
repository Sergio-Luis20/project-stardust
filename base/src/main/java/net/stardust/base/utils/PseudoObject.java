package net.stardust.base.utils;

/**
 * This interface defines that the implementation
 * "is" an object that represents another which is
 * a type of other class. The utility of this interface
 * relies on the proposal of representing a complex
 * object in a simpler way, being able to reconstruct
 * the original object by calling the {@link #toOriginal()}
 * method.
 * 
 * An example of use is when it is needed to represent
 * an inventory, with all its complexity, by only
 * referencing its name, size and items by a material enum
 * and quantity. With these information, you can reconstruct the
 * original inventory only with the lightweight information
 * you stored in this "mask" class.
 */
public interface PseudoObject<T> {
    
    /**
     * This method must return an instance of the original
     * object represented by this class based on all the
     * information stored in it.
     * 
     * @return an instance of the original class.
     */
    T toOriginal();

}
