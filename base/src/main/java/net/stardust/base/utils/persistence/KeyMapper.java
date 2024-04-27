package net.stardust.base.utils.persistence;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.NamespacedKey;

/*
 * Esta interface serve para definir um contrato
 * no qual a classe que a implementa deve retornar
 * um mapa contendo as definições de tipo para
 * cada chave.
 * 
 * Por exemplo: se está definido um KeyMapper para
 * o tipo Pessoa, a qual é representada pelos dados
 * 
 * Integer idade
 * String nome
 * Float altura
 * 
 * então o mapper de uma Pessoa deverá retornar um objeto
 * Map que possua entradas como estas:
 * 
 * stardust:person_age -> Integer.class
 * stardust:person_name -> String.class
 * stardust:person_height -> Float.class
 * 
 * Observe que os tipos int.class e Integer.class, por exemplo,
 * diferem na natureza da classe, e isso potencialmente impactará
 * nos resultados.
 */
@FunctionalInterface
public interface KeyMapper {
    
    Map<NamespacedKey, Class<?>> getKeyMap();
    
    default Map<String, Class<?>> getKeyMapAsString() {
        Map<NamespacedKey, Class<?>> map = getKeyMap();
        if(map == null) return null;
        Map<String, Class<?>> newMap = new HashMap<>();
        map.forEach((nsk, c) -> newMap.put(nsk == null ? null : nsk.asString(), c));
        return newMap;
    }

}
