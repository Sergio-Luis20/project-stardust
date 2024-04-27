package net.stardust.base.utils.database.crud;

import java.util.List;
import java.util.UUID;

import net.stardust.base.model.user.User;
import net.stardust.base.utils.database.NotFoundException;

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
    	int hashCode = name.hashCode();
    	for(User user : all) {
    		String username = user.getName();
    		if(hashCode == username.hashCode() && name.equals(username)) {
    			return user;
    		}
    	}
    	throw new NotFoundException("User not found: " + name, name);
    }

}
