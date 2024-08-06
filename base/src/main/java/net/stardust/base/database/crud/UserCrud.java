package net.stardust.base.database.crud;

import java.util.List;
import java.util.UUID;

import net.stardust.base.database.NotFoundException;
import net.stardust.base.model.user.User;

public final class UserCrud extends Crud<UUID, User> {

    public UserCrud() {
        super(User.class);
    }
    
    public User byNameOrNull(String name) {
    	try {
    		return byNameOrThrow(name);
    	} catch(NotFoundException e) {
    		return null;
    	}
    }
    
    public User byNameOrThrow(String name) {
    	List<User> all = getAll();
    	for(User user : all) {
    		if(name.equals(user.getName())) {
    			return user;
    		}
    	}
    	throw new NotFoundException("User not found: " + name, name);
    }

}
