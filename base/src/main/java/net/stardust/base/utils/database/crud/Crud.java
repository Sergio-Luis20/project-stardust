package net.stardust.base.utils.database.crud;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.sergio.comlib.Communication;
import br.sergio.comlib.Request;
import br.sergio.comlib.RequestMethod;
import br.sergio.comlib.Response;
import br.sergio.comlib.ResponseStatus;
import net.stardust.base.Communicable;
import net.stardust.base.utils.database.NotFoundException;

public class Crud<T extends Serializable, U extends Serializable> implements Communicable {

    public static final String PREFIX = "repository";
    
    protected String sender, receiver;

    public Crud(String sender, String receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public Crud(String sender, Class<U> entityClass) {
        this(sender, idFor(entityClass));
    }

    public Crud(Class<U> entityClass) {
        sender = getId();
        receiver = idFor(entityClass);
    }

    public List<U> getAll() {
        try {
            Request<T> request = Request.newRequest(sender, receiver, RequestMethod.GET, null);
            Response<ArrayList<U>> response = Communication.send(request);
            return response.getContent().get();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<U> getAll(List<T> list) {
        try {
            Serializable body = list instanceof Serializable ser ? ser : new ArrayList<>(list);
            Request<Serializable> request = Request.newRequest(sender, receiver, RequestMethod.GET, body);
            Response<ArrayList<U>> response = Communication.send(request);
            return response.getContent().get();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public U getOrNull(T obj) {
        try {
            return getOrThrow(obj);
        } catch(NotFoundException e) {
            return null;
        }
    }

    public U getOrThrow(T obj) {
        try {
            Request<T> request = Request.newRequest(sender, receiver, RequestMethod.GET, obj);
            Response<U> response = Communication.send(request);
            Optional<U> content = response.getContent();
            if(content.isPresent()) {
                return content.get();
            } else {
                throw new NotFoundException(obj);
            }
        } catch(Exception e) {
            if(e instanceof NotFoundException ex) {
                throw ex;
            } else {
                throw new NotFoundException(e, obj);
            }
        }
    }

    public boolean create(U obj) {
        return genericRequest(RequestMethod.POST, obj, ResponseStatus.CREATED);
    }

    public boolean update(U obj) {
        return genericRequest(RequestMethod.PUT, obj, ResponseStatus.CREATED);
    }

    public boolean updateAll(List<U> list) {
        Serializable body = list instanceof Serializable ser ? ser : new ArrayList<>(list);
        return genericRequest(RequestMethod.PUT, body, ResponseStatus.CREATED);
    }

    public boolean delete(T obj) {
        return genericRequest(RequestMethod.DELETE, obj, ResponseStatus.NO_CONTENT);
    }

    private boolean genericRequest(RequestMethod method, Serializable obj, int expectedStatus) {
        try {
            Request<Serializable> request = Request.newRequest(sender, receiver, method, obj);
            return Communication.send(request).getStatus() == expectedStatus;
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String idFor(Class<?> clazz) {
        return PREFIX + "/" + clazz.getName();
    }

}
