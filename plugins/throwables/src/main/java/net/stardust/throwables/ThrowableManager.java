package net.stardust.throwables;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.sergio.comlib.Communication;
import br.sergio.comlib.MappingException;
import br.sergio.comlib.Request;
import br.sergio.comlib.RequestListener;
import br.sergio.comlib.RequestMapper;
import br.sergio.comlib.Response;
import br.sergio.comlib.ResponseStatus;
import net.stardust.base.BasePlugin;

public class ThrowableManager extends BasePlugin implements RequestMapper {

    private RequestListener listener;
    private Logger logger;
    private String id;

    @Override
    public void onEnable() {
        try {
            super.onEnable();
            saveDefaultConfig();
            logger = getLogger();
            id = getConfig().getString("id");

            logger.info("Conectando ao servidor ServerSocket");
            listener = Communication.newRequestListener(getConfig().getString("id"), this);

            logger.info("Iniciando thread listener");
            listener.start();

            logger.info("Thread listener iniciada");
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        try {
            super.onDisable();
            logger.info("Fechando listener");
            if(listener != null) {
                listener.close();
            }
            logger.info("Listener fechado");
        } catch(IOException e) {
            logger.log(Level.SEVERE, "Erro ao fechar o listener", e);
        }
    }

    @Override
    public Response<?> handle(Request<?> request) throws MappingException {
        Optional<?> content = request.getContent();
        if(content.isEmpty()) {
            return Response.emptyResponse(ResponseStatus.NO_CONTENT);
        }
        Object obj = content.get();
        if(obj instanceof Throwable t) {
            saveThrowable(request.getSender(), t);
        } else {
            throw new MappingException(obj.getClass() + " is not a subtype of " + Throwable.class);
        }
        return Response.emptyResponse();
    }
    
    private void saveThrowable(String id, Throwable t) {
        File throwablesFolder = new File(getConfig().getString("throwables-folder"));
        if(!throwablesFolder.exists()) {
            throwablesFolder.mkdirs();
        }
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss-SSS");
        String fileName = format.format(new Date()) + " " 
            + t.getClass().getSimpleName() + " " + id.replace('/', '_') + ".twb";
        File file = new File(throwablesFolder, fileName);
        try(FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8, true); 
            PrintWriter pw = new PrintWriter(fw)) {
            pw.println("id=" + id);
            pw.println();
            t.printStackTrace(pw);
            pw.flush();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    protected boolean shutdownExecutorService(ExecutorService executorService) {
        try {
            executorService.shutdownNow();
            return true;
        } catch(Exception e) {
            saveThrowable(id, e);
            return false;
        }
    }

}
