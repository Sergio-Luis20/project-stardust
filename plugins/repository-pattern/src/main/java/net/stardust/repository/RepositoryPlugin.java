package net.stardust.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import br.sergio.comlib.Communication;
import br.sergio.comlib.ConnectionException;
import br.sergio.comlib.MethodMapper;
import br.sergio.comlib.RequestListener;
import net.stardust.base.BasePlugin;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.database.BaseEntity;
import net.stardust.base.utils.database.crud.Crud;

public class RepositoryPlugin extends BasePlugin {

	private static final String ENTITIES_PACKAGE = "net.stardust.base.model";

	private List<RequestListener> requestListeners;

	@Override
	public void onLoad() {
		super.onLoad();
	}

	@Override
	public void onEnable() {
		super.onEnable();
		requestListeners = new ArrayList<>();
		Logger log = getLogger();
		saveDefaultConfig();

		log.info("Criando request listeners");

		createRequestListeners();
		
		log.info("Iniciando request listeners");
		
		requestListeners.forEach(RequestListener::start);

		log.info("Request listeners criados com sucesso");
		log.info("Repositório online");
	}

	@Override
	public void onDisable() {
		super.onDisable();
		Logger log = getLogger();
		boolean exc = false;

		log.info("Fechando request listeners");

		for(var requestListener : requestListeners) {
			try {
				requestListener.close();
			} catch(IOException e) {
				log.log(Level.SEVERE, "Falha ao fechar um request listener durante o onDisable", Throwables.send(getId(), e));
				exc = true;
			}
		}
		if(exc) {
			log.info("Request listeners fechados com problemas");
		} else {
			log.info("Request listeners fechados com sucesso");
		}

		log.info("Repositório offline");
	}

	@SuppressWarnings("unchecked")
	private <K, V extends StardustEntity<K>> void createRequestListeners() {
		var reflections = new Reflections(new ConfigurationBuilder()
			.forPackages(ENTITIES_PACKAGE)
			.setScanners(Scanners.TypesAnnotated));
		var entities = reflections.getTypesAnnotatedWith(BaseEntity.class);
		for(var entity : entities) {
			var valueClass = (Class<V>) entity;
			var keyClass = (Class<K>) valueClass.getAnnotation(BaseEntity.class).value();
			var controller = new RepositoryController<>(this, keyClass, valueClass);
			var id = Crud.idFor(valueClass);
			try {
				var mapper = new MethodMapper(controller, true);
				var listener = Communication.newRequestListener(id, mapper);
				requestListeners.add(listener);
			} catch(ConnectionException e) {
				getLogger().log(Level.SEVERE, "Falha ao ligar o request listener para \"" + id + "\"", Throwables.send(getId(), e));
			}
		}
	}

}