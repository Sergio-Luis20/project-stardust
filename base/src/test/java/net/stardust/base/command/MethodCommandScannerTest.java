package net.stardust.base.command;

import static org.junit.jupiter.api.Assertions.*;

import br.sergio.utils.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class MethodCommandScannerTest {

    private MethodCommandScanner scanner;

    public MethodCommandScannerTest() {
        scanner = new MethodCommandScanner(TestSubject.class);
        scanner.scan();
    }

    @Test
    @DisplayName("Should scan only the methods a, b, c and d")
    public void scanningTest() {
        List<String> names = scanner.getMethods().stream().map(Method::getName).toList();
        assertEquals(4, names.size());
        assertTrue(names.containsAll(Arrays.asList("a", "b", "c", "d")));
    }

    @Test
    @DisplayName("Should return method a when passing empty arg array")
    public void aTest() {
        String[] input = new String[0];
        Pair<Method, Object[]> result = scanner.find(input);
        assertNotNull(result);
        Method method = result.getMale();
        Object[] args = result.getFemale();
        assertNotNull(method);
        assertNotNull(args);
        assertEquals("a", method.getName());
        assertEquals(0, args.length);
    }

    @Test
    @DisplayName("Should return method b when passing matching arguments")
    public void bTest() {
        String[] input = {"5", "true", "good morning"};
        Pair<Method, Object[]> result = scanner.find(input);
        assertNotNull(result);
        Method method = result.getMale();
        Object[] args = result.getFemale();
        assertNotNull(method);
        assertNotNull(args);
        assertEquals("b", method.getName());
        assertEquals(3, args.length);
        assertEquals(5, args[0]);
        assertEquals(true, args[1]);
        assertEquals("good morning", args[2]);
    }

    @Test
    @DisplayName("Should return method c when passing matching subcommands")
    public void cTest() {
        String[] input = {"sub1", "sub2"};
        Pair<Method, Object[]> result = scanner.find(input);
        assertNotNull(result);
        Method method = result.getMale();
        Object[] args = result.getFemale();
        assertNotNull(method);
        assertNotNull(args);
        assertEquals("c", method.getName());
        assertEquals(0, args.length);
    }

    @Test
    @DisplayName("Should return method d when passing matching subcommands and arguments")
    public void dTest() {
        String[] input = {"sub1", "sub2", "sub3", "hello", "546"};
        Pair<Method, Object[]> result = scanner.find(input);
        assertNotNull(result);
        Method method = result.getMale();
        Object[] args = result.getFemale();
        assertNotNull(method);
        assertNotNull(args);
        assertEquals("d", method.getName());
        assertEquals(2, args.length);
        assertEquals("hello", args[0]);
        assertEquals(546L, args[1]);
    }

    private static class TestSubject {

        // empty method
        @CommandEntry
        public void a() {

        }

        // argument-only method
        @CommandEntry
        public void b(int arg1, boolean arg2, String arg3) {

        }

        // subcommand-only method
        @CommandEntry("sub1 sub2")
        public void c() {

        }

        // subcommand and argument method
        @CommandEntry("sub1 sub2 sub3")
        public void d(String arg1, long arg2) {

        }

        // this method should not be scanned
        @SuppressWarnings("unused")
        public void nonAnnotatedMethod() {

        }

    }

}
