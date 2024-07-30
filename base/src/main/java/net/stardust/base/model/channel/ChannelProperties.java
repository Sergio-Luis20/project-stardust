package net.stardust.base.model.channel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChannelProperties {
    
    /**
     * This array represents the places where a channel
     * can be activated or deactivated.
     * 
     * The symbolic "place" that represents all of the
     * others is the one called {@code status}. For other
     * specific places that are activated, it can be
     * deactivated by this general one if it is deactivated.
     * 
     * That means a channel can be deactivated in a place
     * in two ways: {@code status} activated and specific
     * deactivated or {@code status} deactivated. For the
     * second one, even if the specific place is activated,
     * it will be ignored.
     * 
     * If an annotated Channel class doesn't contain {@code status},
     * then at any other place that is not specified in this array
     * it will be permanently deactivated.
     * 
     * @return the array containing the places where the Channel
     * annotated class can be specific deactivated.
     */
    String[] value() default "status";

}
