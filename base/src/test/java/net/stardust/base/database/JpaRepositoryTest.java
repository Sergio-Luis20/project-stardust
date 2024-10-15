package net.stardust.base.database;

import br.sergio.utils.math.Point;
import jakarta.persistence.EntityManagerFactory;
import net.stardust.base.BasePlugin;
import net.stardust.base.database.Repository.SaveResult;
import net.stardust.base.database.repositories.JpaRepository;
import net.stardust.base.model.gameplay.Rank;
import net.stardust.base.model.rpg.*;
import net.stardust.base.model.user.User;
import net.stardust.base.utils.security.PasswordException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class JpaRepositoryTest {

    @Mock
    private BasePlugin plugin;

    @Mock
    private Logger logger;

    private EntityManagerFactory entityManagerFactory;
    private JpaRepository<UUID, User> userRepository;
    private List<User> randomUsers;

    private JpaRepository<UUID, RPGPlayer> rpgPlayerRepository;

    private int userAmount = 20;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        entityManagerFactory = JPA.inMemory();

        when(plugin.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getId()).thenReturn(BasePlugin.class.getSimpleName());

        userRepository = new JpaRepository<>(plugin, UUID.class, User.class);

        randomUsers = new ArrayList<>(userAmount);
        for (int i = 0; i < userAmount; i++) {
            try {
                User user = createRandomUser();
                randomUsers.add(user);
                userRepository.save(user);
            } catch (PasswordException e) {
                e.printStackTrace();
            }
        }
        userRepository.getEntityManager().clear();

        rpgPlayerRepository = new JpaRepository<>(plugin, UUID.class, RPGPlayer.class);
    }

    @AfterEach
    void closeRepository() {
        userRepository.close();
        rpgPlayerRepository.close();
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
            } catch (PasswordException e) {
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

        Comparator<User> comparator = Comparator.comparing(User::getId);

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

    @Test
    @DisplayName("Should successfully save and retrieve a RPGPlayer from the database")
    void test10() {
        UUID id = UUID.randomUUID();
        RPGPlayer player = new RPGPlayer(id);
        player.setRank(Rank.C);
        Level level = new Level(LevelFunctions.exponential(1, new Point(5, 3)), 2);
        NumberComposition composition = new NumberComposition();
        composition.layers = new Multiplier[1][];
        composition.layers[0] = new Multiplier[] {new Multiplier(0.4f),new Multiplier(0.03f)};
        PlayerAttribute attribute = new PlayerAttribute(randomBase64String(10), level, 2.5f, composition);
        player.getAttributes().put(attribute.getName(), attribute);
        Skill skill = new Skill(randomBase64String(8));
        player.getSkills().put(skill.getName(), skill);

        assertEquals(SaveResult.SUCCESS, rpgPlayerRepository.save(player));

        rpgPlayerRepository.getEntityManager().clear();
        RPGPlayer retrieved = rpgPlayerRepository.findById(id);

        assertNotNull(retrieved);

        assertEquals(player.getEntityId(), retrieved.getEntityId());
        assertEquals(player.getRank(), retrieved.getRank());
        
        PlayerAttribute retrievedAttribute = retrieved.getAttributes().get(attribute.getName());

        assertNotNull(retrievedAttribute);
        assertEquals(attribute.getBaseFactor(), retrievedAttribute.getBaseFactor());
        
        Level retrievedLevel = retrievedAttribute.getLevel();

        assertNotNull(retrievedLevel);
        assertEquals(level.getValue(), retrievedLevel.getValue());
        assertNotNull(retrievedLevel.getFunction());

        NumberComposition retrievedComposition = retrievedAttribute.getMultipliers();
        
        assertNotNull(retrievedComposition);
        assertNotNull(retrievedComposition.layers);
        assertTrue(Arrays.deepEquals(composition.layers, retrievedComposition.layers));

        Skill retrievedSkill = retrieved.getSkills().get(skill.getName());

        assertNotNull(retrievedSkill);
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
    
    private User createRandomUser() throws PasswordException {
        return User.builder()
                .id(UUID.randomUUID())
                .registered(ThreadLocalRandom.current().nextLong())
                .name(randomBase64String(32))
                .email(randomBase64String(32))
                .password(randomBase64String(16))
                .build();
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
