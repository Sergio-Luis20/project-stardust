package net.stardust.base.model.inventory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PersistentTag extends PersistentObject {

    private TagType type;

    @JsonCreator
    public PersistentTag(@JsonProperty(value = "namespacedKey", required = true) NamespacedKey namespacedKey,
                         @JsonProperty(value = "type", required = true) TagType type,
                         @JsonProperty(value = "value", required = true) Object value) {
        super(namespacedKey, processValue(type, value));
        this.type = type;
    }

    private static Object processValue(TagType type, Object value) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(value, "value");

        if (!TagType.anyMatch(value)) {
            throw new IllegalArgumentException("No TagType found.");
        }

        return switch (type) {
            case BYTE -> switch (value) {
                case Byte b -> b;
                case Short s when s >= Byte.MIN_VALUE && s <= Byte.MAX_VALUE -> s.byteValue();
                case Integer i when i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE -> i.byteValue();
                case Long l when l >= Byte.MIN_VALUE && l <= Byte.MAX_VALUE -> l.byteValue();
                default -> throw new IllegalArgumentException("Value (" + value.getClass() + ") doesn't match type " + type);
            };
            case SHORT -> switch (value) {
                case Byte b -> b.shortValue();
                case Short s -> s;
                case Integer i when i >= Short.MIN_VALUE && i <= Short.MAX_VALUE -> i.shortValue();
                case Long l when l >= Short.MIN_VALUE && l <= Short.MAX_VALUE -> l.shortValue();
                default -> throw new IllegalArgumentException("Value (" + value.getClass() + ") doesn't match type " + type);
            };
            case INTEGER -> switch (value) {
                case Byte b -> b.intValue();
                case Short s -> s.intValue();
                case Integer i -> i;
                case Long l when l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE -> l.intValue();
                default -> throw new IllegalArgumentException("Value (" + value.getClass() + ") doesn't match type " + type);
            };
            case LONG -> switch (value) {
                case Byte b -> b.longValue();
                case Short s -> s.longValue();
                case Integer i -> i.longValue();
                case Long l -> l;
                default -> throw new IllegalArgumentException("Value (" + value.getClass() + ") doesn't match type " + type);
            };
            case FLOAT -> switch (value) {
                case Float f -> f;
                case Double d when d >= Float.MIN_VALUE && d <= Float.MAX_VALUE -> d.floatValue();
                default -> throw new IllegalArgumentException("Value (" + value.getClass() + ") doesn't match type " + type);
            };
            case DOUBLE -> switch (value) {
                case Float f -> f.doubleValue();
                case Double d -> d;
                default -> throw new IllegalArgumentException("Value (" + value.getClass() + ") doesn't match type " + type);
            };
            case BYTE_ARRAY -> switch (value) {
                case byte[] b -> b;
                case int[] i -> {
                    byte[] b = new byte[i.length];
                    for (int index = 0; index < b.length; index++) {
                        int x = i[index];
                        if (x < Byte.MIN_VALUE || x > Byte.MAX_VALUE) {
                            throw new IllegalArgumentException("Value (" + value.getClass() + ") doesn't match type " + type);
                        }
                        b[index] = (byte) x;
                    }
                    yield b;
                }
                case long[] l -> {
                    byte[] b = new byte[l.length];
                    for (int index = 0; index < b.length; index++) {
                        long x = l[index];
                        if (x < Byte.MIN_VALUE || x > Byte.MAX_VALUE) {
                            throw new IllegalArgumentException("Value (" + value.getClass() + ") doesn't match type " + type);
                        }
                        b[index] = (byte) x;
                    }
                    yield b;
                }
                default -> throw new IllegalArgumentException("Value (" + value.getClass() + ") doesn't match type " + type);
            };
            case INTEGER_ARRAY -> switch (value) {
                case byte[] b -> {
                    int[] i = new int[b.length];
                    for (int index = 0; index < i.length; index++) {
                        i[index] = b[index];
                    }
                    yield i;
                }
                case int[] i -> i;
                case long[] l -> {
                    int[] i = new int[l.length];
                    for (int index = 0; index < i.length; index++) {
                        long x = l[index];
                        if (x < Integer.MIN_VALUE || x > Integer.MAX_VALUE) {
                            throw new IllegalArgumentException("Value (" + value.getClass() + ") doesn't match type " + type);
                        }
                        i[index] = (byte) x;
                    }
                    yield i;
                }
                default -> throw new IllegalArgumentException("Value (" + value.getClass() + ") doesn't match type " + type);
            };
            case LONG_ARRAY -> switch (value) {
                case byte[] b -> {
                    long[] l= new long[b.length];
                    for (int index = 0; index < l.length; index++) {
                        l[index] = b[index];
                    }
                    yield l;
                }
                case int[] i -> {
                    long[] l= new long[i.length];
                    for (int index = 0; index < l.length; index++) {
                        l[index] = i[index];
                    }
                    yield l;
                }
                case long[] l -> l;
                default -> throw new IllegalArgumentException("Value (" + value.getClass() + ") doesn't match type " + type);
            };
            default -> {
                if (!type.getComponentType().isInstance(value)) {
                    throw new IllegalArgumentException("Value (" + value.getClass() + ") doesn't match type " + type);
                }
                yield value;
            }
        };
    }

    public PersistentTag(NamespacedKey namespacedKey, Object value) {
        this(namespacedKey, TagType.getByComponentType(value.getClass()), value);
    }

    @Override
    public PersistentTag clone() {
        return new PersistentTag(getNamespacedKey(), type, getValue());
    }

    @Getter
    public enum TagType {

        BYTE(PersistentDataType.BYTE, str -> testNumber(str, Byte::parseByte), Byte::parseByte),
        SHORT(PersistentDataType.SHORT, str -> testNumber(str, Short::parseShort), Short::parseShort),
        INTEGER(PersistentDataType.INTEGER, str -> testNumber(str, Integer::parseInt), Integer::parseInt),
        LONG(PersistentDataType.LONG, str -> testNumber(str, Long::parseLong), Long::parseLong),
        FLOAT(PersistentDataType.FLOAT, str -> testNumber(str, Float::parseFloat), Float::parseFloat),
        DOUBLE(PersistentDataType.DOUBLE, str -> testNumber(str, Double::parseDouble), Double::parseDouble),
        BYTE_ARRAY(PersistentDataType.BYTE_ARRAY, str -> testNumberArray(str, "byte"), str -> convertToArray(str, "byte")),
        INTEGER_ARRAY(PersistentDataType.INTEGER_ARRAY, str -> testNumberArray(str, "integer"), str -> convertToArray(str, "integer")),
        LONG_ARRAY(PersistentDataType.LONG_ARRAY, str -> testNumberArray(str, "long"), str -> convertToArray(str, "long")),
        BOOLEAN(PersistentDataType.BOOLEAN, TagType::testBoolean, TagType::convertToBoolean),
        STRING(PersistentDataType.STRING, Objects::nonNull, str -> str);

        private final PersistentDataType<?, ?> dataType;
        private final Predicate<String> validator;
        private final Function<String, Object> converter;

        TagType(PersistentDataType<?, ?> dataType,
                Predicate<String> validator,
                Function<String, Object> converter) {
            this.dataType = dataType;
            this.validator = validator;
            this.converter = converter;
        }

        public boolean matches(String str) {
            return validator.test(str);
        }

        public Object convertString(String str) {
            return converter.apply(str);
        }

        public Class<?> getComponentType() {
            return dataType.getComplexType();
        }

        public static TagType getByComponentType(Class<?> componentType) {
            for (TagType type : values()) {
                if (type.getComponentType().equals(componentType)) {
                    return type;
                }
            }
            return null;
        }

        public static boolean anyMatch(Object obj) {
            for (TagType type : values()) {
                if (type.getComponentType().isInstance(obj)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean testNumber(String str, Consumer<String> conversor) {
            try {
                conversor.accept(str);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private static boolean testNumberArray(String str, String arrayType) {
            try {
                convertToArray(str, arrayType);
                return true;
            } catch (NullPointerException | IllegalArgumentException e) {
                return false;
            }
        }

        private static Object convertToArray(String str, String arrayType) {
            if (str == null) {
                throw new NullPointerException("string value cannot be null");
            }
            if (!str.matches("\\A\\[[+-]?\\d+]|\\[([+-]?\\d+, )+[+-]?\\d+]\\z")) {
                throw new IllegalArgumentException("not a" + (arrayType.equals("integer") ? "n" : "") + " " + arrayType + " array");
            }
            // empty array, only brackets []
            if (str.length() == 2) {
                return switch (arrayType) {
                    case "byte" -> new byte[0];
                    case "integer" -> new int[0];
                    case "long" -> new long[0];
                    default -> throw new AssertionError("arrayType should be 'byte', 'integer' or 'long'");
                };
            }
            str = str.substring(1, str.length() - 1);
            String[] numbers = str.split(", ");
            return switch (arrayType) {
                case "byte" -> convertToByteArray(numbers);
                case "integer" -> convertToIntegerArray(numbers);
                case "long" -> convertToLongArray(numbers);
                default -> throw new AssertionError("arrayType should be 'byte', 'integer' or 'long'");
            };
        }

        private static boolean testBoolean(String str) {
            try {
                convertToBoolean(str);
                return true;
            } catch (NullPointerException | IllegalArgumentException e) {
                return false;
            }
        }

        private static boolean convertToBoolean(String str) {
            if (str == null) {
                throw new NullPointerException("string value cannot be null");
            }
            return switch (str.toLowerCase()) {
                case "true" -> true;
                case "false" -> false;
                default -> throw new IllegalArgumentException("string value must be, ignoring case, 'true' or 'false'");
            };
        }

        /*
         * Below there are 3 same methods because can't do type generics on primitives
         * and I prefer not use reflection due to performance.
         */

        private static byte[] convertToByteArray(String[] numbers) {
            byte[] array = new byte[numbers.length];
            for (int i = 0; i < numbers.length; i++) {
                array[i] = Byte.parseByte(numbers[i]);
            }
            return array;
        }

        private static int[] convertToIntegerArray(String[] numbers) {
            int[] array = new int[numbers.length];
            for (int i = 0; i < numbers.length; i++) {
                array[i] = Integer.parseInt(numbers[i]);
            }
            return array;
        }

        private static long[] convertToLongArray(String[] numbers) {
            long[] array = new long[numbers.length];
            for (int i = 0; i < numbers.length; i++) {
                array[i] = Long.parseLong(numbers[i]);
            }
            return array;
        }

    }

}
