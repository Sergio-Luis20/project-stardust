package net.stardust.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.persistence.EntityManagerFactory;
import net.stardust.base.model.user.User;
import net.stardust.base.utils.PasswordEncryption;
import net.stardust.repository.Repository.SaveResult;
import net.stardust.repository.repositories.JpaRepository;

public class JpaRepositoryTest {

    @Mock
    private RepositoryPlugin plugin;

    @Mock
    private Logger logger;

    private EntityManagerFactory entityManagerFactory;
    private JpaRepository<UUID, User> userRepository;
    private List<User> randomUsers;

    private int userAmount = 20;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        entityManagerFactory = JPA.inMemory(RepositoryPlugin.buildReflections());

        when(plugin.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getId()).thenReturn(RepositoryPlugin.class.getSimpleName());

        userRepository = new JpaRepository<>(plugin, UUID.class, User.class);

        randomUsers = new ArrayList<>(userAmount);
        for (int i = 0; i < userAmount; i++) {
            try {
                User user = createRandomUser();
                randomUsers.add(user);
                userRepository.save(user);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
        userRepository.getEntityManager().clear();
    }

    @AfterEach
    void closeRepository() {
        userRepository.close();
        entityManagerFactory.close();
    }
    
    @Test
    @DisplayName("Should successfully create and verify if an entity exists in the database")
    void test1() {
        User user = getRandomUser();
        UUID id = user.getEntityId();

        assertTrue(userRepository.existsById(id));
    }

    @Test
    @DisplayName("Should successfully retrieve an user from the database")
    void test2() {
        User user = getRandomUser();
        UUID id = user.getEntityId();

        assertEquals(user, userRepository.findById(id));
    }

    @Test
    @DisplayName("Should successfully update an attribute of an user in the database")
    void test3() {
        User user = getRandomUser();
        UUID id = user.getEntityId();

        User retrieved = userRepository.findById(id);

        assertEquals(user, retrieved);

        retrieved.setName(randomBase64String(32));
        userRepository.save(retrieved, true);
        retrieved = userRepository.findById(id);

        assertNotEquals(user, retrieved);
    }
    
    @Test
    @DisplayName("Should not allow a duplicate")
    void test4() {
        User user = getRandomUser();

        assertEquals(SaveResult.DUPLICATE, userRepository.save(user));
    }

    @Test
    @DisplayName("Should successfully delete an entity from database")
    void test5() {
        User user = getRandomUser();
        UUID id = user.getEntityId();

        assertTrue(userRepository.delete(id));
        assertFalse(userRepository.existsById(id));
    }

    @Test
    @DisplayName("Should successfully retrieve a list of entities from the database")
    void test6() {
        List<UUID> ids = getRandomUserList().stream().map(User::getEntityId).collect(Collectors.toList());
        List<User> users = userRepository.findAll(ids);
        for (User user : users) {
            UUID id = user.getId();
            assertTrue(ids.contains(id));
            ids.remove(id);
        }
    }

    @Test
    @DisplayName("Should successfully retrieve all users from the database")
    void test7() {
        List<UUID> ids = randomUsers.stream().map(User::getEntityId).collect(Collectors.toList());
        List<User> users = userRepository.findAll();
        for (User user : users) {
            UUID id = user.getId();
            assertTrue(ids.contains(id));
            ids.remove(id);
        }
    }

    @Test
    @DisplayName("Should successfully save all entities of a list")
    void test8() {
        int amount = 5;
        List<User> users = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            try {
                users.add(createRandomUser());
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
                fail();
                return;
            }
        }

        assertEquals(SaveResult.SUCCESS, userRepository.saveAll(users));
        assertEquals(SaveResult.DUPLICATE, userRepository.saveAll(users));

        userRepository.getEntityManager().clear();

        List<UUID> ids = users.stream().map(User::getEntityId).toList();
        List<User> retrievedUsers = userRepository.findAll(ids);

        Comparator<User> comparator = (u1, u2) -> u1.getId().compareTo(u2.getId());

        users.sort(comparator);
        retrievedUsers.sort(comparator);

        assertEquals(users, retrievedUsers);
    }

    @Test
    @DisplayName("Should successfully update all entities of a list")
    void test9() {
        int amount = 5;
        List<User> users = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            User user = getRandomUser();
            user.setName(randomBase64String(32));
            users.add(user);
        }

        assertEquals(SaveResult.DUPLICATE, userRepository.saveAll(users, false));
        assertEquals(SaveResult.SUCCESS, userRepository.saveAll(users, true));
    }

    private List<User> getRandomUserList() {
        int amount = 5;
        List<User> users = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            users.add(getRandomUser());
        }
        return users;
    }
    
    private User getRandomUser() {
        Random random = new Random();
        int index = random.nextInt(randomUsers.size());
        return randomUsers.get(index);
    }
    
    private User createRandomUser() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String password = randomBase64String(16);
        byte[] salt = PasswordEncryption.generateSalt();
        byte[] encryptedPassword = PasswordEncryption.generateHash(password, salt);
        UUID id = UUID.randomUUID();
        User user = new User(id, ThreadLocalRandom.current().nextLong(), randomBase64String(32),
                randomBase64String(32), salt, encryptedPassword);
        return user;
    }
    
    private String randomBase64String(int length) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_";
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }
    
}