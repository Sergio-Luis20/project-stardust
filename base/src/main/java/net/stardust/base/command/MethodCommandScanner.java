package net.stardust.base.command;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.sergio.utils.Pair;
import lombok.Getter;
import lombok.experimental.StandardException;

@Getter
public class MethodCommandScanner {

    public static final Set<Class<?>> ALLOWED_TYPES = Set.of(byte.class, Byte.class, 
            short.class, Short.class, int.class, Integer.class, long.class, Long.class, 
            float.class, Float.class, double.class, Double.class, char.class, Character.class, 
            boolean.class, Boolean.class, String.class, BigInteger.class, BigDecimal.class); // and enums
    public static final Set<Class<?>> DECIMALS = Set.of(float.class, Float.class, 
            double.class, Double.class, BigDecimal.class);

    private Class<?> commandClass;
    private List<Method> methods;

    public MethodCommandScanner(Class<?> commandClass) {
        this.commandClass = Objects.requireNonNull(commandClass, "commandClass");
    }

    public List<Method> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    public void scan() {
        List<Method> methods = Arrays.asList(commandClass.getMethods()).stream()
                .filter(method -> method.isAnnotationPresent(CommandEntry.class))
                .collect(Collectors.toList());
        for(Method method : methods) {
            for(Class<?> param : method.getParameterTypes()) {
                if(!ALLOWED_TYPES.contains(param) && !param.isEnum()) {
                    String allowedTypesToString = Arrays.toString(ALLOWED_TYPES.stream().map(Class::getName).toArray());
                    throw new MethodScanException("Method " + methodToString(method) + " in class " + commandClass.getName()
                            + " has not allowed parameter types. Allowed types: " + allowedTypesToString + " and enums");
                }
            }
        }
        int size = methods.size();

        // Check for duplicate methods
        for(int i = 0; i < size - 1; i++) {
            Method method = methods.get(i);
            Class<?>[] params = method.getParameterTypes();
            for(int j = i + 1; j < size; j++) {
                Method method2 = methods.get(j);
                if(methodEquals(method, method2)) {
                    throw new MethodScanException("Methods " + methodToString(method) + " and " 
                            + methodToString(method2) + " are equals in class " + commandClass.getName());
                }
                if(normSubs(method).equals(normSubs(method2))) {
                    Class<?>[] params2 = method2.getParameterTypes();
                    /*
                     * Here we conclude that our parameter arrays are not empty
                     * because if subcommands were equals and params were empty,
                     * then it would fall into our methodEquals verification
                     */
                    if(params.length == params2.length) {
                        int len = params.length;
                        if(params[len - 1] == String.class && params2[len - 1] == String.class) {
                            throw new MethodScanException("Methods " + methodToString(method) + " and "
                                    + methodToString(method2) + " have same subcommands, same number of parameters "
                                    + "and both have String as last parameter in class " + commandClass.getName());
                        }
                    }
                }
            }
        }

        Collections.sort(methods, new Comparator<Method>() {

            @Override
            public int compare(Method m1, Method m2) {
                String val1 = normSubs(m1);
                String val2 = normSubs(m2);

                // Checking subcommand amount
                int len1 = val1.split(" ").length;
                int len2 = val2.split(" ").length;
                if(len1 > len2) return 1;
                if(len2 > len1) return -1;

                if(!val1.equals(val2)) {
                    return val1.compareTo(val2);
                }

                // val1 is equals to val2, then we need to compare parameters
                Class<?>[] params1 = m1.getParameterTypes();
                Class<?>[] params2 = m2.getParameterTypes();
                int pLen1 = params1.length;
                int pLen2 = params2.length;
                if(pLen1 > pLen2) return 1;
                if(pLen2 > pLen1) return -1;

                int len = pLen1;

                // Same amount of parameters, then we need to classificate them
                Class<?> lastParam1 = params1[len - 1];
                Class<?> lastParam2 = params2[len - 1];

                /*
                 * The one with String as last parameter is smaller because
                 * the player can send a entire String with spaces (which is our
                 * subcommand separator) as an argument. This is a reliable
                 * strategy of ordering because there is no method with same
                 * parameters for same subcommands
                 */
                if(lastParam1 == String.class && lastParam2 != String.class) return -1;
                if(lastParam2 == String.class && lastParam1 != String.class) return 1;
                if(lastParam1 == String.class && lastParam2 == String.class) {
                    boolean oneString1 = m1.getAnnotation(CommandEntry.class).oneWordFinalString();
                    boolean oneString2 = m2.getAnnotation(CommandEntry.class).oneWordFinalString();
                    if(oneString1 && !oneString2) return 1;
                    if(oneString2 && !oneString1) return -1;
                    /*
                     * At this point they have the save configuration for final
                     * String, so whatever, we need to order by other attributes
                     */
                }

                // If we are at this level of similarity, then let's appeal
                String classNames1 = String.join(" ", Arrays.asList(Arrays.copyOf(params1, len - 1))
                        .stream().map(Class::getName).toList());
                String classNames2 = String.join(" ", Arrays.asList(Arrays.copyOf(params2, len - 1))
                        .stream().map(Class::getName).toList());
                return classNames1.compareTo(classNames2);
            }
            
        }.reversed());

        this.methods = methods;
    }

    public Pair<Method, Object[]> find(final String[] args) {
        // Here we are filtrating the possibilities
        List<Method> filter = new ArrayList<>(methods);

        for(int i = 0, j = filter.size(); i < j; i++) {
            Method method = filter.get(i);
            Class<?>[] params = method.getParameterTypes();
            int len = params.length;
            String subs = normSubs(method);
            int sum = (subs.isEmpty() ? 0 : subs.split(" ").length) + len;
            if(sum < args.length && (len == 0 || params[len - 1] != String.class) || sum > args.length) {
                filter.remove(i);
                i--;
                j--;
            }
        }
        List<String> forComparison = Arrays.asList(args).stream().map(String::toLowerCase).toList();
        Map<Integer, List<Method>> byMatchingSubcommandsAmount = new HashMap<>();
        for(Method method : filter) {
            String subs = normSubs(method);
            String[] array = subs.isEmpty() ? new String[0] : subs.split(" ");
            int count = 0;
            /*
             * The amount of subcommands in the method is always
             * less or equals to the player's command line, so
             * this loop is safe.
             */
            boolean equals = true;
            for(int i = 0; i < array.length; i++) {
                if(!array[i].equals(forComparison.get(i))) {
                    equals = false;
                    break;
                }
                count++;
            }
            if(equals) {
                List<Method> list = byMatchingSubcommandsAmount.getOrDefault(count, new ArrayList<>());
                list.add(method);
                byMatchingSubcommandsAmount.putIfAbsent(count, list);
            }
        }
        int max = 0;
        for(int size : byMatchingSubcommandsAmount.keySet()) {
            max = Math.max(max, size);
        }
        filter = byMatchingSubcommandsAmount.get(max);
        // Now we have only the most matching subcommands, all methods with same values for them
        if(filter == null || filter.isEmpty()) {
            // No method found for this input
            return null;
        }
        
        if(filter.size() == 1) {
            Method method = filter.get(0);
            String subs = normSubs(method);
            int cutIndex = subs.isEmpty() ? 0 : subs.split(" ").length;
            String[] newArgs = Arrays.copyOfRange(args, cutIndex, args.length);

            Pair<Method, Object[]> pair;
            Class<?>[] params = method.getParameterTypes();
            int len = params.length;
            int argsLen = newArgs.length;
            if(len == 0) {
                if(argsLen == 0) {
                    pair = new Pair<>(method, new Object[0]);
                } else {
                    pair = null;
                }
            } else if(len == argsLen) {
                /*
                 * Even if the last parameter is a String, it doesn't
                 * matter because once we have the same length for both
                 * params and args, oneWordFinalString is meaningless here
                 */
                pair = nonLastParamString(method, newArgs);
            } else if(len < argsLen && params[len - 1] == String.class) {
                pair = lastParamString(method, newArgs);
            } else {
                pair = null;
            }
            return pair;
        } else {
            List<Method> nonLastParamString = new ArrayList<>();
            List<Method> lastParamString = new ArrayList<>();
            filter.forEach(method -> {
                Class<?>[] params = method.getParameterTypes();
                if(params.length == 0 || params[params.length - 1] != String.class) {
                    nonLastParamString.add(method);
                } else {
                    lastParamString.add(method);
                }
            });

            BiFunction<Method, BiFunction<Method, String[], Pair<Method, Object[]>>, Pair<Method, Object[]>> function = (method, bifun) -> {
                String subs = normSubs(method);
                int cutIndex = subs.isEmpty() ? 0 : subs.split(" ").length;
                String[] newArgs = Arrays.copyOfRange(args, cutIndex, args.length);
                return bifun.apply(method, newArgs);
            };

            for(Method method : nonLastParamString) {
                Pair<Method, Object[]> pair = function.apply(method, this::nonLastParamString);
                if(pair == null) continue;
                return pair;
            }

            for(Method method : lastParamString) {
                Pair<Method, Object[]> pair = function.apply(method, this::lastParamString);
                if(pair == null) continue;
                return pair;
            }
        }

        return null;
    }

    private Pair<Method, Object[]> nonLastParamString(Method method, String[] args) {
        Class<?>[] params = method.getParameterTypes();
        if(params.length == args.length) {
            int len = params.length;
            Object[] values = new Object[len];
            if(!parseArguments(params, args, values, len)) {
                return null;
            }
            return new Pair<>(method, values);
        }
        return null;
    }

    private Pair<Method, Object[]> lastParamString(Method method, String[] args) {
        Class<?>[] params = method.getParameterTypes();
        int len = params.length;
        if(len < args.length && method.getAnnotation(CommandEntry.class).oneWordFinalString()) {
            return null;
        }
        if(params.length <= args.length) {
            Object[] values = new Object[len];
            if(!parseArguments(params, args, values, len - 1)) {
                return null;
            }
            // last as String
            values[len - 1] = String.join(" ", Arrays.copyOfRange(args, len - 1, args.length));
            return new Pair<>(method, values);
        }
        return null;
    }

    private boolean parseArguments(Class<?>[] params, String[] args, Object[] values, int limit) {
        for(int i = 0; i < limit; i++) {
            Class<?> param = params[i];
            String arg = args[i];
            if(DECIMALS.contains(param)) {
                arg = arg.replace(',', '.');
            } else if(param.isEnum()) {
                arg = arg.toUpperCase();
            }
            try {
                values[i] = parseArgument(param, arg);
            } catch(RuntimeException e) {
                return false;
            }
        }
        return true;
    }

    private Object parseArgument(Class<?> param, String arg) throws RuntimeException {
        if(param == byte.class || param == Byte.class) return Byte.parseByte(arg);
        if(param == short.class || param == Short.class) return Short.parseShort(arg);
        if(param == int.class || param == Integer.class) return Integer.parseInt(arg);
        if(param == long.class || param == Long.class) return Long.parseLong(arg);
        if(param == float.class || param == Float.class) return Float.parseFloat(arg);
        if(param == double.class || param == Double.class) return Double.parseDouble(arg);
        if(param == char.class || param == Character.class) {
            if(arg.length() != 1) throw new IllegalArgumentException("String length != 1 for char parsing");
            return arg.charAt(0);
        }
        if(param == boolean.class || param == Boolean.class) {
            return switch(arg.toLowerCase()) {
                case "true" -> true;
                case "false" -> false;
                default -> throw new IllegalArgumentException("Not a boolean value");
            };
        }
        if(param == BigInteger.class) return new BigInteger(arg);
        if(param == BigDecimal.class) return new BigDecimal(arg);
        if(param.isEnum()) {
            try {
                return param.getMethod("valueOf", String.class).invoke(null, arg);
            } catch(Exception e) {
                if(e instanceof RuntimeException re) throw re;
                throw new RuntimeException(e);
            }
        }
        if(param == String.class) return arg;
        // Should never get here
        throw new Error("Param class is not one of the presets");
    }

    private boolean methodEquals(Method m1, Method m2) {
        if(!normSubs(m1).equals(normSubs(m2))) {
            return false;
        }
        if(!Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes())) {
            return false;
        }
        return true;
    }

    private String normSubs(Method method) {
        CommandEntry entry = method.getAnnotation(CommandEntry.class);
        return entry.value().trim().toLowerCase();
    }

    public static String methodToString(Method method) {
        String params = String.join(", ", Stream.of(method.getParameterTypes())
                .map(Class::getSimpleName).toList());
        return method.getName() + "(" + params + ")";
    }

    @StandardException
    public static class MethodScanException extends RuntimeException {}

}
