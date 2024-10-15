package net.stardust.repository;

import br.sergio.comlib.Communication;
import br.sergio.comlib.ConnectionException;
import br.sergio.comlib.MethodMapper;
import br.sergio.comlib.RequestListener;
import jakarta.persistence.EntityManagerFactory;
import net.stardust.base.BasePlugin;
import net.stardust.base.database.*;
import net.stardust.base.database.crud.Crud;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.Throwables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RepositoryPlugin extends BasePlugin {

	private List<RequestListener> requestListeners;
	private List<Repository<?, ?>> repositories;

	@Override
	public void onLoad() {
		super.onLoad();
	}

		@Override
	public void onEnable() {
		super.onEnable();
		Logger log = getLogger();
		saveDefaultConfig();

		log.info("Criando repositórios");

		try {
			createRepositories();
		} catch (RepositoryException e) {
			log.severe("Erro durante a criação dos repositórios");
			Throwables.sendAndThrow(e);
		}

		log.info("Criando request listeners");

		createRequestListeners();

		log.info("Iniciando request listeners");

		requestListeners.forEach(RequestListener::start);

		log.info("Repositório online");
	}

	@Override
	public void onDisable() {
		super.onDisable();
		Logger log = getLogger();
		boolean exc = false;

		log.info("Fechando request listeners");

		for (var requestListener : requestListeners) {
			try {
				requestListener.close();
			} catch (IOException e) {
				log.log(Level.SEVERE, "Falha ao fechar um request listener durante o onDisable",
						Throwables.send(getId(), e));
				exc = true;
			}
		}
		if (exc) {
			log.info("Request listeners fechados com problemas");
		} else {
			log.info("Request listeners fechados com sucesso");
		}

		exc = false;

		log.info("Fechando repositórios");

		for (var repository : repositories) {
			try {
				repository.close();
			} catch (Exception e) {
				log.log(Level.SEVERE,
						"Falha ao fechar um repositório durante o onDisable. KeyClass: "
								+ repository.getKeyClass().getSimpleName() + ". ValueClass: "
								+ repository.getValueClass().getSimpleName());
				exc = true;
			}
		}
		if (exc) {
			log.info("Repositórios fechados com problemas");
		} else {
			log.info("Repositórios fechados com sucesso");
		}

		log.info("Fechando EntityManagerFactory");

		try {
			getEntityManagerFactory().close();
			log.info("EntityManagerFactory fechado com sucesso");
		} catch (Exception e) {
			log.log(Level.SEVERE, "Falha ao fechar EntityManagerFactory durante o onDisable",
					Throwables.send(getId(), e));
		}

		log.info("Repositório offline");
	}

	@Override
	public EntityManagerFactory createEntityManagerFactory() {
		return JPA.entityManagerFactory(getConfig());
	}
	
	@SuppressWarnings("unchecked")
	private <K, V extends StardustEntity<K>> void createRepositories() throws RepositoryException {
		var entities = getStardustEntitiesReflections().getTypesAnnotatedWith(BaseEntity.class);
		repositories = new ArrayList<>(entities.size());
		for (var entity : entities) {
			var valueClass = (Class<V>) entity;
			var keyClass = (Class<K>) valueClass.getAnnotation(BaseEntity.class).value();
			var repository = RepositoryFactory.getRepository(this, keyClass, valueClass);
			repositories.add(repository);
		}
	}

	private void createRequestListeners() {
		requestListeners = new ArrayList<>(repositories.size());
		for (var repository : repositories) {
			var controller = new RepositoryController<>(this, repository);
			var id = Crud.idFor(repository.getValueClass());
			try {
				var mapper = new MethodMapper(controller, true);
				var listener = Communication.newRequestListener(id, mapper);
				requestListeners.add(listener);
			} catch (ConnectionException e) {
				getLogger().log(Level.SEVERE, "Falha ao ligar o request listener para \"" + id + "\"",
						Throwables.send(getId(), e));
			}
		}
	}

}
